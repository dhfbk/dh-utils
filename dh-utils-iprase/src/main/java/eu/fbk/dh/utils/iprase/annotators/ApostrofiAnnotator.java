package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotations.StatisticsEvent;
import eu.fbk.dh.utils.iprase.utils.CatAnnotator;
import eu.fbk.utils.corenlp.CustomAnnotations;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApostrofiAnnotator extends CatAnnotator {

    private StatisticsEvent statisticsEvent = new StatisticsEvent();
    private Pattern vowelPattern = Pattern.compile("^[aeiouh]");

    @Override public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

            Integer find = null;
            boolean apostrophe = false;
            boolean findNow = false;

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String form = token.originalText().toLowerCase();
                form = Normalizer.normalize(form, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (form.equals("un") || form.equals("un'")) {
                    find = token.index();
                    if (form.endsWith("'")) {
                        apostrophe = true;
                    }
                    findNow = true;
                    continue;
                }

                if (findNow) {
                    Matcher matcher = vowelPattern.matcher(form);
                    if (apostrophe && !matcher.find()) {
                        statisticsEvent.add("incorrect");
                        find = null;
                        apostrophe = false;
                        findNow = false;
                        continue;
                    }
                    if (form.startsWith("h")) {
                        statisticsEvent.add("start-h");
                        find = null;
                        apostrophe = false;
                        findNow = false;
                        continue;
                    }
                    findNow = false;
                }

                if (pos.startsWith("A")) {
                    continue;
                }

                if (pos.startsWith("B")) {
                    continue;
                }

                if (find != null) {
                    Map<String, Collection<String>> features = token.get(CustomAnnotations.FeaturesAnnotation.class);
                    Collection<String> genders = features.get("Gender");
                    if (genders != null) {
                        boolean f = false;
                        for (String gender : genders) {
                            if (gender.equals("Fem")) {
                                f = true;
                            }
                        }
                        if ((apostrophe && !f) || (!apostrophe && f)) {
                            statisticsEvent.add("incorrect");
                            if (!apostrophe) {
                                statisticsEvent.add("missing");
                            }
                        } else {
                            statisticsEvent.add("correct");
                        }
                    }
                    else {
                        statisticsEvent.add("no-gender");
                    }
                    find = null;
                }
            }

        }

        ret.add(statisticsEvent);

        return ret;
    }

    @Override public void load() {

    }
}
