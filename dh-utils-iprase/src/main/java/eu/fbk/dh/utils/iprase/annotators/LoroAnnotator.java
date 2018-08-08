package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.annotators.abstracts.WordsAnnotator;

public class LoroAnnotator extends WordsAnnotator {

    @Override
    public void load() {
        wordsToCollect.add("loro");
        wordsToCount.add("egli");
        wordsToCount.add("ella");
        wordsToCount.add("esso");
        wordsToCount.add("essa");
        wordsToCount.add("essi");
        wordsToCount.add("esse");
        wordsToCount.add("lei");
        wordsToCount.add("lui");
        wordsToCount.add("loro");
        super.load();
    }
}
