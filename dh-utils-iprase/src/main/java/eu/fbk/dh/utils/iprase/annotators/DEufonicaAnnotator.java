package eu.fbk.dh.utils.iprase.annotators;

import com.google.common.collect.HashMultimap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import eu.fbk.dh.utils.iprase.utils.CatAnnotator;
import eu.fbk.dh.utils.iprase.utils.LocuzioniAnnotator;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class DEufonicaAnnotator extends CatAnnotator {

    private CasiSpecialiAnnotator casiSpecialiAnnotator = new CasiSpecialiAnnotator();
    private StatisticsEvent statisticsEvent = new StatisticsEvent();

    private class CasiSpecialiAnnotator extends LocuzioniAnnotator {

        @Override public void load() {
            fileName = "d_eufonica";
            loadCollect = true;
            super.load();
        }
    }

    @Override public List<GenericEvent> annotate(Annotation annotation) {
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
                    } else {
                        if (form.charAt(0) == lastD) {
                            text = "correct";
                        } else if (form.charAt(0) == 'h') {
                            text = "start-h";
                        }
                    }

                    statisticsEvent.add(text);
                }

                lastD = null;
            }
        }

        ret.add(statisticsEvent);
        return ret;
    }

    @Override public void load() {
        casiSpecialiAnnotator.load();
    }
}
