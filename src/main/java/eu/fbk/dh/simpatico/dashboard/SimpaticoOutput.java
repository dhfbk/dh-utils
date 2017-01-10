package eu.fbk.dh.simpatico.dashboard;

import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dkm.pikes.twm.LinkingTag;
import org.languagetool.rules.RuleMatch;

import java.util.List;

/**
 * Created by alessio on 10/01/17.
 */

public class SimpaticoOutput {

    List<RuleMatch> languagetool;
    Readability readability;
    String docDate;
    String timings;
    List<LinkingTag> linkings;
    List<LexensteinAnnotator.Simplification> simplifications;
    List<String> sentenceTexts;
}
