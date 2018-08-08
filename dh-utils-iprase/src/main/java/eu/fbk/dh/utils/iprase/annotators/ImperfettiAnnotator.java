package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.verb.VerbAnnotations;
import eu.fbk.dh.tint.verb.VerbMultiToken;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;
import eu.fbk.dh.utils.iprase.utils.EventUtils;

import java.util.ArrayList;
import java.util.List;

public class ImperfettiAnnotator extends CatAnnotator {

    @Override
    public void load() {

    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            List<VerbMultiToken> verbs = sentence.get(VerbAnnotations.VerbsAnnotation.class);
            Integer sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            for (VerbMultiToken verb : verbs) {
                String mood = verb.getMood();
                String tense = verb.getTense();
                if (mood == null) {
                    continue;
                }
                if (tense == null) {
                    continue;
                }

                if (mood.equals("Ind") && tense.equals("Imp")) {
                    ret.add(EventUtils.getEventFromVerb(verb, sentenceID, tense));
                }
//                if (mood.equals("Ind") && (tense.equals("Imp") || tense.equals("TrPast"))) {
//                    ret.add(EventUtils.getEventFromVerb(verb, sentenceID, tense));
//                }
            }

        }

        return ret;
    }

}
