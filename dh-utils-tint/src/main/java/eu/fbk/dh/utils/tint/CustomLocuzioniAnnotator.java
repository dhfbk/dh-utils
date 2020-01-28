package eu.fbk.dh.utils.tint;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CustomLocuzioniAnnotator extends CatAnnotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLocuzioniAnnotator.class);

    private Map<String, String> locuzioniMap = new HashMap<>();
    //    private Set<String> locuzioniSet = new HashSet<>();
    private Set<String> posList = new HashSet<>();
//    private StatisticsEvent statisticsEvent = new StatisticsEvent();

//    protected Set<String> locuzioniToCount = new HashSet<>();
//    protected Set<String> locuzioniToCollect = new HashSet<>();

//    protected Set<String> locuzioniToSkipLemma = new HashSet<>();
//    protected Set<String> locuzioniToSkipAdjectives = new HashSet<>();

//    protected String fileName = null;
//    protected boolean loadCount = false;
//    protected boolean loadCollect = false;

//    protected boolean skipAdverbs = false;
//    protected boolean skipAdjectives = false;

    public String toTheUpperCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; i++) {
            sb.append(Character.toUpperCase(arr[i].charAt(0)))
                    .append(arr[i].substring(1)).append(" ");
        }
        return sb.toString().trim();
    }


    private Set<String> loadFile(String fileName) throws IOException {
        return loadFile(fileName, false);
    }

    private Set<String> loadFile(String fileName, boolean ucWords) throws IOException {
        Set<String> ret = new HashSet<>();

        URL list = Resources.getResource(fileName);
        for (String line : Resources.readLines(list, Charsets.UTF_8)) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (ucWords) {
                line = toTheUpperCase(line.toLowerCase());
            }
            ret.add(line);
        }

        return ret;
    }

    @Override
    public void load() {
        try {
            Set<String> per = loadFile("per", true);
            Set<String> gpe = loadFile("gpe");
            for (String p : per) {
                locuzioniMap.putIfAbsent(p, "PER");
            }
            for (String p : gpe) {
                locuzioniMap.putIfAbsent(p, "GPE");
            }

//            locuzioniSet.addAll(locuzioniToCollect);
//            locuzioniSet.addAll(locuzioniToCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        Map<Integer, StringBuffer> buffers = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> begins = new HashMap<>();

        int tokenIndex = -1;
        Set<Integer> realBegins = new HashSet<>();
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            tokenIndex++;

            for (int value = 0; value < 2; value++) {
                boolean useLemma = (value == 1);

                if (!buffers.containsKey(value)) {
                    buffers.put(value, new StringBuffer());
                    buffers.get(value).append(" ");
                    begins.put(value, new HashMap<>());
                }

                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                String txt = token.originalText().toLowerCase();
                String txt = token.originalText();

                if (useLemma && !posList.contains(pos)) {
//                    txt = token.lemma().toLowerCase();
                    txt = token.lemma();
                }

                begins.get(value).put(buffers.get(value).length(), tokenIndex);
                buffers.get(value).append(txt).append(" ");
            }
        }

//        System.out.println(buffers);
//        System.out.println(begins);
//        System.out.println(realBegins);

        for (String locuzione : locuzioniMap.keySet()) {
            for (Integer value : buffers.keySet()) {
                searchEvents(locuzione, buffers.get(value), begins.get(value), annotation,
                        ret, realBegins, locuzioniMap.get(locuzione));
            }
        }

        return ret;
    }

    private void searchEvents(String locuzione, StringBuffer buffer, Map<Integer, Integer> begins, Annotation
            annotation, List<GenericEvent> ret, Set<Integer> realBegins, String description) {
        String text = buffer.toString();
        int lastIndex = 0;
        while (lastIndex != -1) {
            lastIndex = text.indexOf(locuzione, lastIndex);
            if (lastIndex != -1) {

//                System.out.println(lastIndex);
//                System.out.println(begins);

//                Integer firstTokenIndex = begins.get(lastIndex + 1); // +1 because of the space
                lastIndex += 1; // this must be somewhere, before any "continue"
                Integer firstTokenIndex = begins.get(lastIndex - 1);

                if (firstTokenIndex == null) {
                    LOGGER.error("Offset not found");
                    continue;
                }

                List<Integer> tokenIDs = new ArrayList<>();
                CoreLabel firstToken = annotation.get(CoreAnnotations.TokensAnnotation.class).get(firstTokenIndex);
                tokenIDs.add(firstToken.index());

                Integer begin = firstToken.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                if (realBegins.contains(begin)) {
                    continue;
                }

                Integer end = firstToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                Integer sentenceID = firstToken.sentIndex();
                Integer lastSentenceID = sentenceID;
                int words = locuzione.trim().split("\\s+").length;
                for (int i = 1; i < words; i++) {
                    CoreLabel thisToken = annotation.get(CoreAnnotations.TokensAnnotation.class).get(firstTokenIndex + i);
                    tokenIDs.add(thisToken.index());
                    lastSentenceID = thisToken.sentIndex();
                    end = thisToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                    thisToken.set(CoreAnnotations.NamedEntityTagAnnotation.class, description);
//                    thisToken.set(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class, description);
                }
                if (!lastSentenceID.equals(sentenceID)) {
                    LOGGER.error("Sentence IDs are different");
                    continue;
                }

                realBegins.add(begin);

                String eventText = annotation.get(CoreAnnotations.TextAnnotation.class).substring(begin, end);
                AnnotationEvent event = new AnnotationEvent(sentenceID, tokenIDs, eventText, description, begin, end);
                ret.add(event);

//                System.out.println(event.getText());
            }
        }
    }

}
