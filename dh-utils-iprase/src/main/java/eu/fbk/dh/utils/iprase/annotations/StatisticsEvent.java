package eu.fbk.dh.utils.iprase.annotations;

import eu.fbk.utils.core.FrequencyHashSet;

public class StatisticsEvent extends GenericEvent {

    FrequencyHashSet<String> frequencies = new FrequencyHashSet<>();

    public void add(String s) {
        frequencies.add(s);
    }

    public void add(String s, int val) {
        frequencies.add(s, val);
    }
}
