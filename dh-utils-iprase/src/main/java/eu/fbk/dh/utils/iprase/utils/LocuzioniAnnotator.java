package eu.fbk.dh.utils.iprase.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.*;

public abstract class LocuzioniAnnotator extends CatAnnotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocuzioniAnnotator.class);

    private Set<String> locuzioniSet = new HashSet<>();
    private Set<String> determiners = new HashSet<>();
    private Set<String> posList = new HashSet<>();
    private StatisticsEvent statisticsEvent = new StatisticsEvent();

    protected Set<String> locuzioniToCount = new HashSet<>();
    protected Set<String> locuzioniToCollect = new HashSet<>();

    protected Set<String> locuzioniToSkipLemma = new HashSet<>();
    protected Set<String> locuzioniToSkipAdjectives = new HashSet<>();

    protected String fileName = null;
    protected boolean loadCount = false;
    protected boolean loadCollect = false;

    protected boolean skipAdverbs = false;
    protected boolean skipAdjectives = false;

    @Override
    public void load() {

        determiners.add("");
        determiners.add("l");
        determiners.add("llo");
        determiners.add("lla");
        determiners.add("i");
        determiners.add("gli");
        determiners.add("lle");
        determiners.add("ll'");

        posList.add("E+RD");
        posList.add("E");
        posList.add("RD");

        try {
            if (fileName != null) {
                URL list = Resources.getResource(fileName);
                for (String line : Resources.readLines(list, Charsets.UTF_8)) {
                    line = line.trim().toLowerCase();

                    boolean addToSkipAdjectives = false;
                    boolean addToSkipLemma = false;

                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.startsWith("#")) {
                        continue;
                    }
                    if (line.startsWith("%")) {
                        line = line.substring(1);
                        addToSkipAdjectives = true;
                    }
                    if (line.startsWith("@")) {
                        line = line.substring(1);
                        addToSkipLemma = true;
                    }

                    String[] parts = line.toLowerCase().split("\\s+");
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(" ");
                    for (String part : parts) {
                        buffer.append(part).append(" ");
                    }
                    String finalString = buffer.toString();
                    int index = finalString.indexOf("[art]");
                    if (index == -1) {
                        if (loadCollect) {
                            locuzioniToCollect.add(finalString);
                        }
                        if (loadCount) {
                            locuzioniToCount.add(finalString);
                        }
                        if (addToSkipLemma) {
                            locuzioniToSkipLemma.add(finalString);
                        }
                        if (addToSkipAdjectives) {
                            locuzioniToSkipAdjectives.add(finalString);
                        }
//                        locuzioniSet.add(finalString);
                    } else {
                        for (String determiner : determiners) {
                            String s = finalString.replaceAll("\\[art\\]", determiner);
                            if (loadCollect) {
                                locuzioniToCollect.add(s);
                            }
                            if (loadCount) {
                                locuzioniToCount.add(s);
                            }
                            if (addToSkipLemma) {
                                locuzioniToSkipLemma.add(s);
                            }
                            if (addToSkipAdjectives) {
                                locuzioniToSkipAdjectives.add(s);
                            }
//                            locuzioniSet.add(finalString.replaceAll("\\[art\\]", determiner));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        locuzioniSet.addAll(locuzioniToCollect);
        locuzioniSet.addAll(locuzioniToCount);
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        Map<Integer, StringBuffer> buffers = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> begins = new HashMap<>();
        Map<Integer, Set<Integer>> jumps = new HashMap<>();

        int tokenIndex = -1;
        Set<Integer> realBegins = new HashSet<>();
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            tokenIndex++;

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    for (int k = 0; k < 2; k++) {
                        boolean useLemma = (i == 1);
                        boolean skipAdj = (j == 1);
                        boolean skipAdv = (k == 1);

                        if (skipAdj && !skipAdjectives) {
                            continue;
                        }
                        if (skipAdv && !skipAdverbs) {
                            continue;
                        }

                        int value = i + (j * 2) + (k * 4);
                        if (!buffers.containsKey(value)) {
                            buffers.put(value, new StringBuffer());
                            buffers.get(value).append(" ");
                            begins.put(value, new HashMap<>());
                            jumps.put(value, new HashSet<>());
                        }

                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        String txt = token.originalText().toLowerCase();

                        if (skipAdj && (pos.startsWith("A") || pos.startsWith("D"))) {
                            jumps.get(value).add(tokenIndex);
                            continue;
                        }
                        if (skipAdv && pos.startsWith("B")) {
                            jumps.get(value).add(tokenIndex);
                            continue;
                        }
                        if (useLemma && !posList.contains(pos)) {
                            txt = token.lemma().toLowerCase();
                        }

                        begins.get(value).put(buffers.get(value).length(), tokenIndex);
                        buffers.get(value).append(txt).append(" ");
                    }
                }
            }
        }

        for (String locuzione : locuzioniSet) {
            boolean skipThisAdj = false;
            boolean skipThisLemma = false;
            if (locuzioniToSkipAdjectives.contains(locuzione)) {
                skipThisAdj = true;
            }
            if (locuzioniToSkipLemma.contains(locuzione)) {
                skipThisLemma = true;
            }
            for (Integer value : buffers.keySet()) {
                if (skipThisLemma && (value == 1 || value == 3 || value == 5 || value == 7)) {
                    continue;
                }
                if (skipThisAdj && (value == 2 || value == 3 || value == 6 || value == 7)) {
                    continue;
                }
                searchEvents(locuzione, buffers.get(value), begins.get(value), annotation,
                        ret, realBegins, jumps.get(value), locuzioniToCount.contains(locuzione), locuzioniToCollect.contains(locuzione));
            }
        }

        ret.add(statisticsEvent);
        return ret;
    }

    private void searchEvents(String locuzione, StringBuffer buffer, Map<Integer, Integer> begins, Annotation annotation,
                              List<GenericEvent> ret, Set<Integer> realBegins, Set<Integer> jumps, boolean count, boolean collect) {
        String text = buffer.toString();
        int lastIndex = 0;
        while (lastIndex != -1) {
            lastIndex = text.indexOf(locuzione, lastIndex);
            if (lastIndex != -1) {

                Integer firstTokenIndex = begins.get(lastIndex + 1); // +1 because of the space
                lastIndex += 1; // this must be somewhere, before any "continue"

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
                    if (jumps.contains(firstTokenIndex + i)) {
                        words++;
                        continue;
                    }
                    CoreLabel thisToken = annotation.get(CoreAnnotations.TokensAnnotation.class).get(firstTokenIndex + i);
                    tokenIDs.add(thisToken.index());
                    lastSentenceID = thisToken.sentIndex();
                    end = thisToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                }
                if (!lastSentenceID.equals(sentenceID)) {
                    LOGGER.error("Sentence IDs are different");
                    continue;
                }

                realBegins.add(begin);

                if (collect) {
                    String eventText = annotation.get(CoreAnnotations.TextAnnotation.class).substring(begin, end);
                    AnnotationEvent event = new AnnotationEvent(sentenceID, tokenIDs, eventText, "-", begin, end);
                    ret.add(event);
                }
                if (count) {
                    statisticsEvent.add(locuzione.trim());
                }
            }
        }
    }

}
