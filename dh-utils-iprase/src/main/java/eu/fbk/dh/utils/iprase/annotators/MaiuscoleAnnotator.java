package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.digimorph.DigiMorph;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaiuscoleAnnotator extends CatAnnotator {

    private Set<String> temporals = new HashSet<>();
    private static final Pattern FIRST_MAIUSCOLO = Pattern.compile("^[A-Z]");
    private static final Pattern MINUSCOLO = Pattern.compile("[a-z]");
    DigiMorph digiMorph = new DigiMorph();

    @Override
    public void load() {
        temporals.add("lunedì");
        temporals.add("lunedi");
        temporals.add("martedì");
        temporals.add("martedi");
        temporals.add("mercoledì");
        temporals.add("mercoledi");
        temporals.add("giovedì");
        temporals.add("giovedi");
        temporals.add("venerdì");
        temporals.add("venerdi");
        temporals.add("sabato");
        temporals.add("domenica");
        temporals.add("gennaio");
        temporals.add("febbraio");
        temporals.add("marzo");
        temporals.add("aprile");
        temporals.add("maggio");
        temporals.add("giugno");
        temporals.add("luglio");
        temporals.add("agosto");
        temporals.add("settembre");
        temporals.add("ottobre");
        temporals.add("novembre");
        temporals.add("dicembre");
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        String last;
        String lastTmp = "";
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            boolean first = true;
            Integer sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);
            last = lastTmp;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.originalText();
                int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                int tokenID = token.index();

                String lWord = word.toLowerCase();
                lastTmp = word;
                boolean isInFormario = digiMorph.getMap().containsKey(lWord);
                Matcher matcher;

                matcher = FIRST_MAIUSCOLO.matcher(word);
                if (!matcher.find()) {
                    continue;
                }

                matcher = MINUSCOLO.matcher(word);
                if (!matcher.find()) {
                    continue;
                }

                if (!isInFormario) {
                    continue;
                }

                if (first) {
                    first = false;
                    if (!last.equals(":")) {
                        continue;
                    }
                }

                String label = "-";
                if (temporals.contains(lWord)) {
                    label = "Nome_mese_o_giorno";
                }
                AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, word, label, begin, end);
                ret.add(event);
            }
        }

        return ret;
    }

}
