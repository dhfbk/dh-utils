package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.LocuzioniAnnotator;

public class GergaleAnnotator extends LocuzioniAnnotator {
    @Override
    public void load() {
        fileName = "gergale";
        loadCollect = true;
        super.load();
    }
}