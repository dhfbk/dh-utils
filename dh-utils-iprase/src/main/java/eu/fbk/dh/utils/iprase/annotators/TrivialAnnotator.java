package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.annotators.abstracts.WordsAnnotator;

public class TrivialAnnotator extends WordsAnnotator {

    @Override public void load() {
        wordsToCount.add("bello");
        wordsToCount.add("brutto");
        wordsToCount.add("grande");
        wordsToCount.add("piccolo");
        wordsToCount.add("buono");
        wordsToCount.add("cattivo");
        wordsToCount.add("vecchio");
        wordsToCount.add("nuovo");
        wordsToCount.add("cosa");
        wordsToCount.add("fare");
        wordsToCount.add("dire");
        alsoLemma = true;
        super.load();
    }
}
