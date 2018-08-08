package eu.fbk.dh.utils.iprase.annotators;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;
import eu.fbk.dh.utils.iprase.annotators.abstracts.LocuzioniAnnotator;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DEufonicaAnnotator extends CatAnnotator {

    private CasiSpecialiAnnotator casiSpecialiAnnotator = new CasiSpecialiAnnotator();
    private StatisticsEvent statisticsEvent = new StatisticsEvent();
    private static Set<Character> vowels = new HashSet<>();

    static {
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');
        vowels.add('j');
        vowels.add('y');
        vowels.add('h');
    }

    private class CasiSpecialiAnnotator extends LocuzioniAnnotator {

        @Override
        public void load() {
            fileName = "d_eufonica";
            loadCollect = true;
            super.load();
        }
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        HashMultimap<Integer, Integer> skip = HashMultimap.create();

        List<GenericEvent> events = casiSpecialiAnnotator.annotate(annotation);
        for (GenericEvent event : events) {
            if (event instanceof AnnotationEvent) {
                skip.putAll(((AnnotationEvent) event).getSentenceID(), ((AnnotationEvent) event).getTokenIDs());
            }
        }

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Character lastD = null;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String form = token.originalText().toLowerCase();
                form = Normalizer.normalize(form, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (lastD == null) {
                    if (pos.startsWith("S")) {
                        continue;
                    }
                    if (form.equals("ad") || form.equals("ed") || form.equals("od")) {
                        lastD = form.charAt(0);
                        continue;
                    }
                }

                if (lastD != null) {
                    String text = "incorrect";

                    if (skip.containsEntry(token.sentIndex(), token.index())) {
                        text = "use";
                        statisticsEvent.add(text);
                    } else {
                        if (form.length() > 0 && vowels.contains(form.charAt(0))) {
                            if (form.charAt(0) == lastD) {
                                text = "correct";
                            } else if (form.charAt(0) == 'h') {
                                text = "start-h";
                            }
                            statisticsEvent.add(text);
                        }
                    }
                }

                lastD = null;
            }

            Character lastNotD = null;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String originalForm = token.originalText().toLowerCase();
                String form = Normalizer.normalize(originalForm, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (lastNotD == null) {
                    if (pos.startsWith("S")) {
                        continue;
                    }
                    if ((form.equals("a") || form.equals("e") || form.equals("o")) && !originalForm.equals("è") && !originalForm.equals("é")) {
                        lastNotD = form.charAt(0);
                        continue;
                    }
                }

                if (lastNotD != null) {
                    if (form.length() > 0 && vowels.contains(form.charAt(0))) {
                        String text = "n-incorrect";
                        if (form.charAt(0) == 'h') {
                            text = "n-start-h";
                        } else if (form.charAt(0) != lastNotD) {
                            text = "n-correct";
                        }
                        statisticsEvent.add(text);
                    }
                }

                lastNotD = null;
            }
        }

        ret.add(statisticsEvent);
        return ret;
    }

    @Override
    public void load() {
        casiSpecialiAnnotator.load();
    }
}
