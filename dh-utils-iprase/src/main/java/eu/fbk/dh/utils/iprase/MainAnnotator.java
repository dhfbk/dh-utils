package eu.fbk.dh.utils.iprase;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import eu.fbk.dh.utils.iprase.annotations.CatAnnotations;
import eu.fbk.dh.utils.iprase.annotations.Task;
import eu.fbk.dh.utils.iprase.utils.CatAnnotator;
import eu.fbk.utils.core.PropertiesUtils;

import java.util.*;

public class MainAnnotator implements Annotator {

    private Task task = null;

    public MainAnnotator(String annotatorName, Properties prop) {
        Properties properties = PropertiesUtils.dotConvertedProperties(prop, annotatorName);
        try {
            Class<?> thisClass = Class.forName(properties.getProperty("class"));
            if (CatAnnotator.class.isAssignableFrom(thisClass)) {
                task = new Task(PropertiesUtils.getInteger(properties.getProperty("id"), 0), properties.getProperty("name"), properties.getProperty("class"));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void annotate(Annotation annotation) {
        try {
            if (task != null) {
                task.run(annotation);
                List<Task> tasks = annotation.get(CatAnnotations.CatTasksAnnotation.class);
                if (tasks == null) {
                    tasks = new ArrayList<>();
                }
                tasks.add(task);
                annotation.set(CatAnnotations.CatTasksAnnotation.class, tasks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requirementsSatisfied() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Set<Class<? extends CoreAnnotation>> requires() {
        return Collections.EMPTY_SET;
    }
}
