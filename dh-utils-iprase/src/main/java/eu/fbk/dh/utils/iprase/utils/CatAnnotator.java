package eu.fbk.dh.utils.iprase.utils;

import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;

import java.util.List;

public abstract class CatAnnotator {

    public abstract List<GenericEvent> annotate(Annotation annotation);

    public abstract void load();
}
