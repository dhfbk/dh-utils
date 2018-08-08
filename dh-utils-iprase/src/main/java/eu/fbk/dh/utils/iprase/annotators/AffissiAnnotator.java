package eu.fbk.dh.utils.iprase.annotators;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.derived.Derivation;
import eu.fbk.dh.tint.derived.DerivationAnnotations;
import eu.fbk.dh.tint.derived.DerivedAffixation;
import eu.fbk.dh.tint.derived.DerivedPhase;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.GenericEvent;
import eu.fbk.dh.utils.iprase.annotators.abstracts.CatAnnotator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AffissiAnnotator extends CatAnnotator {

    private static final Set<String> starts = new HashSet<>();
    private static final Set<String> ends = new HashSet<>();
    private static final Set<String> manualEnds = new HashSet<>();

    @Override
    public void load() {
        starts.add("anti");
        starts.add("dopo");
        starts.add("trans");
        starts.add("iper");
        starts.add("super");
        ends.add("ista");
        ends.add("tore");
        ends.add("zione");
        ends.add("mento");
        ends.add("tura");
        ends.add("aggio");
        ends.add("ità");
        ends.add("ismo");
        ends.add("izzare");
        ends.add("ale");
        ends.add("iano");
        ends.add("istico");
        ends.add("ato");
        manualEnds.add("izzazione");
    }

    @Override
    public List<GenericEvent> annotate(Annotation annotation) {
        List<GenericEvent> ret = new ArrayList<>();

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lToken = token.originalText().toLowerCase();

                String prefix = null;
                String suffix = null;

                for (String start : starts) {
                    if (lToken.startsWith(start) || lToken.startsWith(start + "-")) {
                        if (lToken.length() > start.length() + 1) {
                            prefix = start;
                        }
                    }
                }

                Derivation derivation = token.get(DerivationAnnotations.DerivationAnnotation.class);
                if (derivation != null) {
                    int size = derivation.getPhases().size();
                    if (size > 0) {
                        DerivedPhase lastPhase = derivation.getPhases().get(size - 1);
                        if (lastPhase instanceof DerivedAffixation) {
                            String affix = ((DerivedAffixation) lastPhase).getAffix();
                            if (ends.contains(affix)) {
                                suffix = affix;
                            }
                        }
                    }
                }

                if (suffix == null) {
                    for (String end : manualEnds) {
                        if (lToken.endsWith(end)) {
                            suffix = end;
                        }
                    }
                }

                if (prefix != null || suffix != null) {

                    String label;
                    String notes;
                    if (prefix == null) {
                        label = "Suffisso";
                        notes = suffix;
                    } else if (suffix == null) {
                        label = "Prefisso";
                        notes = prefix;
                    } else {
                        label = "Prefisso_e_Suffisso";
                        notes = String.format("%s, %s", prefix, suffix);
                    }
                    int begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                    int end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                    AnnotationEvent event = new AnnotationEvent(token.sentIndex(), token.index(), token.originalText(), label, begin, end);
                    event.setNotes(notes);
                    ret.add(event);
                }
            }
        }

        return ret;
    }

}
