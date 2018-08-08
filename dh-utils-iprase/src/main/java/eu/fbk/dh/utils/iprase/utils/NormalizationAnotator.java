package eu.fbk.dh.utils.iprase.utils;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.utils.iprase.annotations.CatAnnotations;
import eu.fbk.dh.utils.iprase.annotations.NormalizedSentence;

import java.text.Normalizer;
import java.util.*;

public class NormalizationAnotator implements Annotator {

    @Override
    public void annotate(Annotation annotation) {
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

            StringBuffer buffer = new StringBuffer();
//            LinkedHashMap<Integer, Integer> tokenToOffset = new LinkedHashMap<>();
//            LinkedHashMap<Integer, Integer> offsetToToken = new LinkedHashMap<>();
            List<String> tokens = new ArrayList<>();
            List<Integer> tokenIDs = new ArrayList<>();
            List<Integer> offsets = new ArrayList<>();

            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                int offset = buffer.length();
                String tokenText = token.get(CoreAnnotations.TextAnnotation.class);
                tokenText = normalize(tokenText);
                if (tokenText.length() == 0) {
                    continue;
                }

                buffer.append(tokenText).append(" ");
//                tokenToOffset.put(token.index(), offset);
//                offsetToToken.put(offset, token.index());
                tokenIDs.add(token.index());
                offsets.add(offset);
                tokens.add(tokenText);
            }

            NormalizedSentence normalizedSentence = new NormalizedSentence(tokenIDs, offsets, tokens, buffer.toString().trim());
            sentence.set(CatAnnotations.NormalizedSentenceAnnotation.class, normalizedSentence);

        }

    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CatAnnotations.NormalizedSentenceAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.singleton(CoreAnnotations.SentencesAnnotation.class);
    }

    public static String normalize(String text) {
        return normalize(text, false);
    }

    public static String normalize(String text, boolean preserveSpaces) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        text = text.toLowerCase();
        if (preserveSpaces) {
            text = text.replaceAll("[^a-z\\s]", "");
            text = text.replaceAll("\\s+", " ");
        } else {
            text = text.replaceAll("[^a-z]", "");
        }
        return text;
    }
}
