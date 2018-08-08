package eu.fbk.dh.utils.iprase;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import eu.fbk.dh.utils.iprase.annotations.CatAnnotations;
import eu.fbk.dh.utils.iprase.annotations.Task;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;
import eu.fbk.utils.core.PropertiesUtils;

import java.util.*;

public class MainAnnotator implements Annotator {

    Properties properties;

    public MainAnnotator(String annotatorName, Properties prop) {
        properties = PropertiesUtils.dotConvertedProperties(prop, annotatorName);
    }

    @Override
    public void annotate(Annotation annotation) {
        try {
            Class<?> thisClass = Class.forName(this.properties.getProperty("class"));
            if (CatAnnotator.class.isAssignableFrom(thisClass)) {
                Task task = new Task(PropertiesUtils.getInteger(this.properties.getProperty("id"), 0), this.properties.getProperty("name"), this.properties.getProperty("class"));
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
