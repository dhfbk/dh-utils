package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import eu.fbk.dh.kd.annotator.DigiKDAnnotations;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class StanfordPosAnnotator implements Annotator {

    Map<String, String> posMap = new HashMap<>();

    public StanfordPosAnnotator(String annotatorName, Properties prop) {
        URL resource = this.getClass().getClassLoader().getResource("tagset.tsv");
        try {
            List<String> lines = Resources.readLines(resource, Charsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }
                posMap.put(parts[0].trim().toLowerCase(), parts[1].trim().toLowerCase());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void annotate(Annotation annotation) {
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class).toLowerCase();
            token.set(CustomEnglishAnnotations.StanfordPos.class, posMap.getOrDefault(pos, pos).toUpperCase());
        }

    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.singleton(DigiKDAnnotations.KeyphrasesAnnotation.class);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.unmodifiableSet(new ArraySet<>(Arrays.asList(
                CoreAnnotations.PartOfSpeechAnnotation.class,
                CoreAnnotations.TokensAnnotation.class
        )));
    }
}
