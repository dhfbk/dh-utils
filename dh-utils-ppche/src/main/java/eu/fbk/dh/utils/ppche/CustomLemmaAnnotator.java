package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import eu.fbk.utils.core.FrequencyHashSet;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class CustomLemmaAnnotator implements Annotator {

    Map<String, FrequencyHashSet<String>> lemmaMap = new HashMap<>();

    public CustomLemmaAnnotator(String annotatorName, Properties prop) {
        URL resource = this.getClass().getClassLoader().getResource("all-lemma-pos.tsv");
        try {
            List<String> lines = Resources.readLines(resource, Charsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }

                String key = parts[0].trim().toLowerCase() + "|" + parts[1].trim();
                String value = parts[2].trim();

                lemmaMap.putIfAbsent(key, new FrequencyHashSet<>());
                lemmaMap.get(key).add(value);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void annotate(Annotation annotation) {
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String text = token.originalText();

            String key = text.trim().toLowerCase() + "|" + pos.trim();
            FrequencyHashSet<String> set = lemmaMap.get(key);
            if (set == null) {
                continue;
            }

            token.set(CustomEnglishAnnotations.OriginalLemma.class, token.get(CoreAnnotations.LemmaAnnotation.class));
            token.set(CoreAnnotations.LemmaAnnotation.class, set.mostFrequent());
        }

    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(CoreAnnotations.LemmaAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class
        )));
    }
}
