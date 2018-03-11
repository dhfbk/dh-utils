package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import eu.fbk.dh.utils.iprase.utils.CatAnnotator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArticleAnnotator extends CatAnnotator {

    private StatisticsEvent statisticsEvent = new StatisticsEvent();
    private Pattern startPattern = Pattern.compile("^(w|j|pn|ps)");

    @Override public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            boolean found = false;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String form = token.originalText().toLowerCase();
                if (form.equals("il")) {
                    found = true;
                    continue;
                }

                if (found) {
                    Matcher matcher = startPattern.matcher(form);
                    if (matcher.find()) {
                        statisticsEvent.add(matcher.group(1));
                    }
                }

                found = false;
            }
        }

        ret.add(statisticsEvent);
        return ret;
    }

    @Override public void load() {

    }
}
