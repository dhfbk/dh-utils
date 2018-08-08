package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.annotators.abstracts.WordsAnnotator;

public class LiAnnotator extends WordsAnnotator {

    @Override
    public void load() {
        wordsToCollect.add("li");
        wordsToCollect.add("lì");
        wordsToCollect.add("li'");
        super.load();
    }
}
