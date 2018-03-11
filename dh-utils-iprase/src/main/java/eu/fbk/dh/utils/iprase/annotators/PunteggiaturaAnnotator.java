package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import eu.fbk.dh.utils.iprase.utils.WordsAnnotator;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunteggiaturaAnnotator extends WordsAnnotator {

    private Pattern weirdMatcher = Pattern.compile("^[\\?!]{2,}?");

    @Override public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> events = super.annotate(annotation);
        int last = events.size() - 1;
        GenericEvent genericEvent = events.get(last);
        if (genericEvent instanceof StatisticsEvent) {
            for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
                String form = token.originalText();
                Matcher matcher = weirdMatcher.matcher(form);
                if (matcher.find()) {
                    int qm = 0;
                    int em = 0;
                    for (int i = 0; i < form.length(); i++) {
                        char c = form.charAt(i);
                        if (c == '?') {
                            qm++;
                        }
                        if (c == '!') {
                            em++;
                        }
                    }
                    if (qm == 0) {
                        ((StatisticsEvent) genericEvent).add("multiple-!");
                    }
                    if (em == 0) {
                        ((StatisticsEvent) genericEvent).add("multiple-?");
                    }
                    if (em * qm != 0) {
                        ((StatisticsEvent) genericEvent).add("mix-!?");
                    }
                }
            }
        } else {
            System.err.println("Last event is not a StatisticsEvent");
        }

        return events;
    }

    @Override
    public void load() {
        wordsToCount.add(".");
        wordsToCount.add(",");
        wordsToCount.add(";");
        wordsToCount.add(":");
        wordsToCount.add("!");
        wordsToCount.add("?");
        wordsToCount.add("\"");
        wordsToCount.add("-");
        wordsToCount.add("...");
        wordsToCount.add("....");
        wordsToCount.add(".....");
        wordsToCount.add("......");
        wordsToCount.add("«");
        wordsToCount.add("»");
        wordsToCollect.addAll(wordsToCount);
        super.load();
    }
}
