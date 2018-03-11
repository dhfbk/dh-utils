package eu.fbk.dh.utils.iprase.annotations;

import java.util.ArrayList;
import java.util.List;

public class AnnotationEvent extends GenericEvent {

    private List<Integer> tokenIDs = new ArrayList<>();
    private int sentenceID;
    private String text;
    private String description;
    private String notes;
    private int begin;
    private int end;
    private boolean skip = false;

    public AnnotationEvent(int sentenceID, int tokenID, String text, String description, int begin, int end) {
        this.tokenIDs.add(tokenID);
        this.sentenceID = sentenceID;
        this.text = text;
        this.description = description;
        this.begin = begin;
        this.end = end;
    }

    public AnnotationEvent(int sentenceID, List<Integer> tokenIDs, String text, String description, int begin, int end) {
        this.tokenIDs = tokenIDs;
        this.sentenceID = sentenceID;
        this.text = text;
        this.description = description;
        this.begin = begin;
        this.end = end;
    }

    public int getBegin() {
        return begin;
    }

    public int getEnd() {
        return end;
    }

    public int getSentenceID() {
        return sentenceID;
    }

    public List<Integer> getTokenIDs() {
        return tokenIDs;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
