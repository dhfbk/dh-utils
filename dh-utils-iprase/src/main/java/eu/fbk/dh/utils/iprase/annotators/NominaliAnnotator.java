package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.verb.VerbAnnotations;
import eu.fbk.dh.tint.verb.VerbMultiToken;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NominaliAnnotator extends CatAnnotator {

    private StatisticsEvent statisticsEvent = new StatisticsEvent();
    Pattern twoNpattern = Pattern.compile("\n[\r]?\n");

    @Override
    public void load() {

    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        int words = 0;
        int sentences = 0;
        int clauses = 0;

        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            if (pos.startsWith("F")) {
                continue;
            }
            words++;
        }

        String allText = annotation.get(CoreAnnotations.TextAnnotation.class);
        Matcher matcher = twoNpattern.matcher(allText);
        int doubleN = -1;
        if (matcher.find()) {
            doubleN = matcher.start();
        }
//        System.out.println("Found double N on " + doubleN);

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            sentences++;
            List<VerbMultiToken> verbs = sentence.get(VerbAnnotations.VerbsAnnotation.class);
            int verbNo = verbs.size();
            clauses += Math.max(1, verbNo);
            Integer sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            if (verbNo == 0) {
                List<Integer> tokens = new ArrayList<>();
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    tokens.add(token.index());
                }
                int begin = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int end = sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                String text = sentence.get(CoreAnnotations.TextAnnotation.class);

                AnnotationEvent event = new AnnotationEvent(sentenceID, tokens, text, "-", begin, end);
                ret.add(event);
                if (begin > doubleN) {
                    statisticsEvent.add("frasi_nominali");
                }
            }
        }

        statisticsEvent.add("words", words);
        statisticsEvent.add("sentences", sentences);
        statisticsEvent.add("clauses", clauses);

        ret.add(statisticsEvent);
        return ret;
    }

}
