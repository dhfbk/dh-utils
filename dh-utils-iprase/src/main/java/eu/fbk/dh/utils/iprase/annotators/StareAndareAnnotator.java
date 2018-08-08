package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;
import eu.fbk.utils.corenlp.CustomAnnotations;

import java.util.*;

public class StareAndareAnnotator extends CatAnnotator {

    private Set<String> possibleWords = new HashSet<>();
    private StatisticsEvent statisticsEvent = new StatisticsEvent();

    @Override public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for (int i = 0; i < tokens.size(); i++) {
                CoreLabel token = tokens.get(i);
                String lemma = token.lemma().toLowerCase();
                if (lemma.equals("stare") || lemma.equals("andare")) {
                    boolean trivial = false;

                    if (tokens.size() < i + 3) {
                        trivial = true;
                    }

                    if (!trivial) {
                        CoreLabel nextToken = tokens.get(i + 1);
                        if (!possibleWords.contains(nextToken.originalText().toLowerCase())) {
                            trivial = true;
                        }
                    }

                    if (!trivial) {
                        CoreLabel nextToken = tokens.get(i + 2);
                        String pos = nextToken.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        Map<String, Collection<String>> features = nextToken.get(CustomAnnotations.FeaturesAnnotation.class);
                        boolean isInf = false;
                        Collection<String> verbForm = features.get("VerbForm");
                        if (verbForm != null) {
                            for (String form : verbForm) {
                                if (form.equals("Inf")) {
                                    isInf = true;
                                }
                            }
                        }

                        if (!pos.startsWith("V") || !isInf) {
                            trivial = true;
                        }
                    }

                    if (trivial) {
                        statisticsEvent.add(lemma);
                    } else {
                        statisticsEvent.add(lemma + "+inf");
                    }
                }
            }
        }

        ret.add(statisticsEvent);
        return ret;
    }

    @Override public void load() {
        possibleWords.add("per");
        possibleWords.add("a");
    }
}
