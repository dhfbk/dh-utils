package eu.fbk.dh.utils.iprase.annotators;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.itextpdf.layout.hyphenation.Hyphenation;
import com.itextpdf.layout.hyphenation.Hyphenator;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;
import eu.fbk.dh.utils.iprase.annotators.abstracts.LocuzioniAnnotator;
import eu.fbk.utils.core.FrequencyHashSet;

import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonosillabiAnnotator extends CatAnnotator {

    private class SeStessoAnnotator extends LocuzioniAnnotator {

        @Override public void load() {
            this.locuzioniToCount.add(" se stesso ");
            this.locuzioniToCount.add(" sé stesso ");
            this.locuzioniToCount.add(" sè stesso ");
            this.locuzioniToCount.add(" se' stesso ");
            this.locuzioniToCount.add(" se stessi ");
            this.locuzioniToCount.add(" sé stessi ");
            this.locuzioniToCount.add(" sè stessi ");
            this.locuzioniToCount.add(" se' stessi ");
            super.load();
        }
    }

    private static Pattern ENDS_WITH_CONSAPO = Pattern.compile("[bcdfghlmnpqrstvzwjkyx]['’]$");
    private static Pattern ONLY_CONSO = Pattern.compile("^[bcdfghlmnpqrstvzwjkyx]+$");
    private static Pattern NO_LETTERS = Pattern.compile("[a-z]");

    private Set<String> monosillabiSet = new HashSet<>();
    private FrequencyHashSet<String> monosillabiSetNormalized = new FrequencyHashSet<>();
    Hyphenator hyphenator = null;
    private SeStessoAnnotator seStessoAnnotator = new SeStessoAnnotator();

    @Override
    public void load() {
        try {
            seStessoAnnotator.load();
            hyphenator = new Hyphenator("it", "it", 1, 1);

            URL monosillabi = Resources.getResource("monosillabi");
            for (String line : Resources.readLines(monosillabi, Charsets.UTF_8)) {
                line = line.trim().toLowerCase();
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                monosillabiSet.add(line);
            }

            for (String word : monosillabiSet) {
                String normalized = Normalizer.normalize(word, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
                normalized = normalized.replaceAll("[^a-zA-Z]", "");
                monosillabiSetNormalized.add(normalized);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String tokenOriginalText = token.originalText();
                int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                String tokenText = tokenOriginalText.toLowerCase();
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                // punctuation
                if (pos.startsWith("F")) {
                    continue;
                }

                // numbers
                if (pos.equals("N")) {
                    continue;
                }

                String normalized = Normalizer.normalize(tokenText, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
                normalized = normalized.replaceAll("[^a-zA-Z]", "");
                Hyphenation hyphenation = hyphenator.hyphenate(normalized);
                int length = 1;
                if (hyphenation != null) {
                    length = hyphenation.length() + 1;
                }

                if (length != 1) {
                    continue;
                }

                Matcher matcher;

                matcher = ENDS_WITH_CONSAPO.matcher(tokenText);
                if (matcher.find()) {
                    continue;
                }

                matcher = ONLY_CONSO.matcher(tokenText);
                if (matcher.find()) {
                    continue;
                }

                matcher = NO_LETTERS.matcher(tokenText);
                if (!matcher.find()) {
                    continue;
                }

                Integer frequency = monosillabiSetNormalized.get(normalized);
                int tokenID = token.index();
                Integer sentenceID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);

                if (frequency == null) {
                    AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, tokenOriginalText, "Verify", begin, end);
                    ret.add(event);
                } else {
                    if (frequency == 1) {
                        if (monosillabiSet.contains(tokenText)) {
                            AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, tokenOriginalText, "Correct", begin, end);
                            event.setSkip(true);
                            ret.add(event);
                        } else {
                            AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, tokenOriginalText, "Wrong", begin, end);
                            ret.add(event);
                        }
                    } else {
                        AnnotationEvent event = new AnnotationEvent(sentenceID, tokenID, tokenOriginalText, "Ambiguous", begin, end);
                        ret.add(event);
                    }
                }
            }
        }

        List<GenericEvent> events = seStessoAnnotator.annotate(annotation);
        ret.addAll(events);

        return ret;
    }

}
