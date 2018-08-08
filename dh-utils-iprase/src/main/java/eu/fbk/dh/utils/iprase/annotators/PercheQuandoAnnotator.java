package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.annotators.abstracts.WordsBeginAnnotator;

public class PercheQuandoAnnotator extends WordsBeginAnnotator {

    @Override
    public void load() {
        wordsToCollect.add("perché");
        wordsToCollect.add("perchè");
        wordsToCollect.add("quando");
        wordsToCount.add("perché");
        wordsToCount.add("perchè");
        wordsToCount.add("quando");
        super.load();
    }
}
