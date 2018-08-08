package eu.fbk.dh.utils.iprase.annotators.abstracts;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class WordsBeginAnnotator extends CatAnnotator {

    private Set<String> words = new HashSet<>();
//    protected boolean collect = false;
//    protected boolean count = false;

    protected Set<String> wordsToCount = new HashSet<>();
    protected Set<String> wordsToCollect = new HashSet<>();

    private StatisticsEvent statisticsEvent = new StatisticsEvent();

    @Override
    public void load() {
        words.addAll(wordsToCount);
        words.addAll(wordsToCollect);
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Integer sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            CoreLabel firstToken = null;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                if (token.get(CoreAnnotations.PartOfSpeechAnnotation.class).startsWith("F")) {
                    continue;
                }
                firstToken = token;
                break;
            }

            if (firstToken == null) {
                break;
            }

            String word = firstToken.originalText();
            int begin = firstToken.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int end = firstToken.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
            int tokenID = firstToken.index();

            String lWord = word.toLowerCase();

            if (words.contains(lWord)) {
                if (wordsToCollect.contains(lWord)) {
                    AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, word, "-", begin, end);
                    ret.add(event);
                }
                if (wordsToCount.contains(lWord)) {
                    statisticsEvent.add(lWord);
                }
            }
        }

//        System.out.println(statisticsEvent);
        ret.add(statisticsEvent);
        return ret;
    }

}
