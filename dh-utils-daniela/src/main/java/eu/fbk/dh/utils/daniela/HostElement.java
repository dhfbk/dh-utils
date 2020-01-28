package eu.fbk.dh.utils.daniela;

public class HostElement {
    private String start, end;
    private double startTime, endTime;

    public HostElement(String start, String end, double startTime, double endTime) {
        this.start = start;
        this.end = end;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String get(int index) {
        if (index == 0) {
            return start;
        }
        else {
            return end;
        }
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }
}
