package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.annotators.abstracts.LocuzioniAnnotator;

public class PoliticamenteCorrettoAnnotator extends LocuzioniAnnotator {

    @Override
    public void load() {
        fileName = "politicamente_corretto";
        loadCount = true;
        loadCollect = true;
        super.load();
    }
}
