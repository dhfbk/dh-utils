package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.LocuzioniAnnotator;

public class PolirematicheAnnotator extends LocuzioniAnnotator {

    @Override
    public void load() {
        fileName = "polirematiche_11-03.tsv";
        loadCollect = true;
        skipAdjectives = true;
        skipAdverbs = true;
        super.load();
    }
}
