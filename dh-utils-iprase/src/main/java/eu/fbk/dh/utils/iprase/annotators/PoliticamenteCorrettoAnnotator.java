package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.LocuzioniAnnotator;

public class PoliticamenteCorrettoAnnotator extends LocuzioniAnnotator {

    @Override
    public void load() {
        fileName = "politicamente_corretto";
        loadCount = true;
        super.load();
    }
}
