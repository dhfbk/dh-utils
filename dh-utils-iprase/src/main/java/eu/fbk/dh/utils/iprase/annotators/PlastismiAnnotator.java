package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.annotators.abstracts.LocuzioniAnnotator;

public class PlastismiAnnotator extends LocuzioniAnnotator {
    @Override
    public void load() {
        fileName = "plastismi";
        loadCollect = true;
        super.load();
    }
}
