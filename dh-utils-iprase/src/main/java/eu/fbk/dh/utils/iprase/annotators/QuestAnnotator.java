package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.WordsAnnotator;

public class QuestAnnotator extends WordsAnnotator {

    @Override
    public void load() {
        wordsToCollect.add("questo");
        wordsToCollect.add("questi");
        wordsToCollect.add("queste");
        wordsToCollect.add("questa");
        posList.add("PD");
        super.load();
    }
}
