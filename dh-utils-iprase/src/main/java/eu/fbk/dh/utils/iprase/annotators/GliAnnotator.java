package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.WordsAnnotator;

public class GliAnnotator extends WordsAnnotator {

    @Override
    public void load() {
        wordsToCollect.add("gli");
        super.load();
    }
}
