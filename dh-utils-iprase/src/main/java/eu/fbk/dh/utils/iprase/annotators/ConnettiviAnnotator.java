package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.WordsAnnotator;

public class ConnettiviAnnotator extends WordsAnnotator {

    @Override
    public void load() {
        wordsToCollect.add("che");
        wordsToCollect.add("dove");
        wordsToCollect.add("infatti");
        wordsToCollect.add("cioè");
        wordsToCollect.add("cioé");
        wordsToCollect.add("allora");
        wordsToCollect.add("dunque");
        wordsToCollect.add("quindi");
        wordsToCollect.add("siccome");
        super.load();
    }
}
