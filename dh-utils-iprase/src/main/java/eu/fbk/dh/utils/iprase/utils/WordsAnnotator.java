package eu.fbk.dh.utils.iprase.utils;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;

import java.util.*;

public abstract class WordsAnnotator extends CatAnnotator {

    protected Set<String> posList = new HashSet<>();
    protected Map<String, String> wordsLabel = new HashMap<>();

    protected boolean alsoLemma = false;

    protected Set<String> wordsToCount = new HashSet<>();
    protected Set<String> wordsToCollect = new HashSet<>();

    private StatisticsEvent statisticsEvent = new StatisticsEvent();

    @Override
    public void load() {
        for (String word : wordsToCount) {
            wordsLabel.putIfAbsent(word, "-");
        }
        for (String word : wordsToCollect) {
            wordsLabel.putIfAbsent(word, "-");
        }
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            Integer sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.originalText();
                int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                int tokenID = token.index();

                if (posList.size() > 0) {
                    String pos = token.getString(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if (!posList.contains(pos)) {
                        continue;
                    }
                }

                String lWord = word.toLowerCase();
                String lLemma = token.lemma().toLowerCase();

                if (wordsLabel.keySet().contains(lWord)) {
                    if (wordsToCollect.contains(lWord)) {
                        AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, word, wordsLabel.get(lWord), begin, end);
                        ret.add(event);
                    }
                    if (wordsToCount.contains(lWord)) {
                        String refWord = lWord;
                        if (alsoLemma) {
                            refWord = lLemma;
                        }
                        statisticsEvent.add(refWord);
                    }
                } else {
                    if (alsoLemma && wordsLabel.keySet().contains(lLemma)) {
                        if (wordsToCollect.contains(lLemma)) {
                            AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, word, wordsLabel.get(lLemma), begin, end);
                            ret.add(event);
                        }
                        if (wordsToCount.contains(lLemma)) {
                            statisticsEvent.add(lLemma);
                        }
                    }
                }
            }
        }

        ret.add(statisticsEvent);
        return ret;
    }

}
