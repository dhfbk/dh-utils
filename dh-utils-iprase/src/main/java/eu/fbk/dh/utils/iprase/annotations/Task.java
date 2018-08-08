package eu.fbk.dh.utils.iprase.annotations;

import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;

import java.util.ArrayList;
import java.util.List;

public class Task {

    private Integer taskID;
    private String taskName;
    private String className;
    private List<AnnotationEvent> events = new ArrayList<>();
    private StatisticsEvent statistics = null;

    public Task(Integer taskID, String taskName, String className) {
        this.taskID = taskID;
        this.taskName = taskName;
        this.className = className;
    }

    public void run(Annotation annotation) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        events = new ArrayList<>();
        Class<? extends CatAnnotator> myClass = (Class<? extends CatAnnotator>) Class.forName(className);
        CatAnnotator catAnnotator = myClass.newInstance();
        catAnnotator.load();
        List<GenericEvent> events = catAnnotator.annotate(annotation);
        for (GenericEvent event : events) {
            if (event instanceof AnnotationEvent) {
                addEvent((AnnotationEvent) event);
            }
            if (event instanceof StatisticsEvent) {
                statistics = (StatisticsEvent) event;
            }
        }
    }

    private void setEvents(List<AnnotationEvent> events) {
        this.events = events;
    }

    public void addEvent(AnnotationEvent event) {
        events.add(event);
    }

    public Integer getTaskID() {
        return taskID;
    }

    public String getTaskName() {
        return taskName;
    }

    public List<AnnotationEvent> getEvents() {
        return events;
    }
}
