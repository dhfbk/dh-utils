package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.utils.CatAnnotator;
import eu.fbk.utils.corenlp.CustomAnnotations;

import java.util.*;

public class FrasiScisseAnnotator extends CatAnnotator {

    private static final Set<String> allowedTags = new HashSet<>();
//    private static final Set<String> skipLastPos = new HashSet<>();

    @Override
    public void load() {
        allowedTags.add("S");
        allowedTags.add("SP");
        allowedTags.add("E");
        allowedTags.add("A");
        allowedTags.add("AP");
        allowedTags.add("B");
        allowedTags.add("BN");
        allowedTags.add("DD");
        allowedTags.add("DE");
        allowedTags.add("DI");
        allowedTags.add("DQ");
        allowedTags.add("DR");
        allowedTags.add("RD");
        allowedTags.add("E+RD");
        allowedTags.add("RI");
        allowedTags.add("N");
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            boolean isEssere = false;

            List<Integer> tokens = new ArrayList<>();
            int beginEvent = -1;
            int count = 0;
            boolean noun = false;
            String lastPos = "";

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.lemma();
                String tokenText = token.originalText().toLowerCase();

                int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (isEssere) {
                    if (tokenText.equals("che")) {
                        if (count > 0 && noun) {
                            for (int i = tokens.get(0) + 1; i <= token.index(); i++) {
                                tokens.add(i);
                            }
                            int endEvent = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                            String text = annotation.get(CoreAnnotations.TextAnnotation.class).substring(beginEvent, endEvent);
                            AnnotationEvent event = new AnnotationEvent(token.sentIndex(), tokens, text, "-", beginEvent, endEvent);
                            ret.add(event);
                        }
                        tokens = new ArrayList<>();
                        isEssere = false;
                        count = 0;
                        noun = false;

                        lastPos = pos;
                        continue;
                    }
                    if (!allowedTags.contains(pos)) {
                        tokens = new ArrayList<>();
                        isEssere = false;
                        count = 0;
                        noun = false;
                        lastPos = pos;
                        continue;
                    }

                    if (pos.startsWith("S")) {
                        noun = true;
                    }

                    count++;
                }

                if (!isEssere && lemma != null && lemma.toLowerCase().equals("essere") && !allowedTags.contains(lastPos)) {
                    Map<String, Collection<String>> features = token.get(CustomAnnotations.FeaturesAnnotation.class);
                    Collection<String> person = features.get("Person");
                    if (person != null && person.contains("3")) {
                        isEssere = true;
                        beginEvent = begin;
                        tokens.add(token.index());
                    }
                }
                lastPos = pos;
            }

        }

        return ret;
    }

}
