package eu.fbk.dh.utils.iprase.annotators;

import eu.fbk.dh.utils.iprase.utils.WordsAnnotator;

public class CongiunzioniAnnotator extends WordsAnnotator {

    @Override public void load() {
        wordsToCount.add("qualora");
        wordsToCount.add("nondimeno");
        wordsToCount.add("sebbene");
        wordsToCount.add("quantunque");

        wordsToCount.add("affinché");
        wordsToCount.add("affinchè");
        wordsToCount.add("affinche");
        wordsToCount.add("affinche'");
        
        wordsToCount.add("giacché");
        wordsToCount.add("giacchè");
//        wordsToCount.add("giacche");
        wordsToCount.add("giacche'");

        wordsToCount.add("sicché");
        wordsToCount.add("sicchè");
        wordsToCount.add("sicche");
        wordsToCount.add("sicche'");

        wordsToCount.add("talché");
        wordsToCount.add("talchè");
        wordsToCount.add("talche");
        wordsToCount.add("talche'");
        
        super.load();
    }
}
