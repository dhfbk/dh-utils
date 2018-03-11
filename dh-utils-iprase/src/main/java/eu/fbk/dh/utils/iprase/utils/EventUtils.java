package eu.fbk.dh.utils.iprase.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.dh.tint.verb.VerbMultiToken;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;

import java.util.ArrayList;
import java.util.List;

public class EventUtils {

    public static AnnotationEvent getEventFromVerb(VerbMultiToken verb, int sentenceID, String label) {
        StringBuffer buffer = new StringBuffer();
        List<Integer> tokens = new ArrayList<>();
        for (CoreLabel token : verb.getTokens()) {
            tokens.add(token.index());
            buffer.append(token.originalText()).append(" ");
        }
        int begin = verb.getTokens().get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
        int end = verb.getTokens().get(verb.getTokens().size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

        AnnotationEvent event = new AnnotationEvent(sentenceID, tokens, buffer.toString().trim(), label, begin, end);
        return event;

    }
}
