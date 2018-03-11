package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.WordsBeginAnnotator;

public class PercheQuandoAnnotator extends WordsBeginAnnotator {

    @Override
    public void load() {
        wordsToCollect.add("perché");
        wordsToCollect.add("perchè");
        wordsToCollect.add("quando");
        super.load();
    }
}
