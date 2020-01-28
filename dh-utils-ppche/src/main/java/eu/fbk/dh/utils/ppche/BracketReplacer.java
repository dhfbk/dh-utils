package eu.fbk.dh.utils.ppche;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;

import java.util.Collections;
import java.util.Set;

public class BracketReplacer implements Annotator {
    @Override
    public void annotate(Annotation annotation) {
        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        text = text.replace("(", "-");
        text = text.replace(")", "-");
        text = text.replace("[", "-");
        text = text.replace("]", "-");
        text = text.replace("{", "-");
        text = text.replace("}", "-");
        annotation.set(CoreAnnotations.TextAnnotation.class, text);
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.emptySet();
    }
}
