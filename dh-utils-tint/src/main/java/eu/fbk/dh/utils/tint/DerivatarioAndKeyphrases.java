package eu.fbk.dh.utils.tint;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.kd.annotator.DigiKDAnnotations;
import eu.fbk.dh.tint.derived.Derivation;
import eu.fbk.dh.tint.derived.DerivationAnnotations;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.dh.tint.runner.TintPipeline;

import java.io.IOException;

public class DerivatarioAndKeyphrases {
    public static void main(String[] args) {
        TintPipeline pipelineTint = new TintPipeline();

        // Load the default properties
        try {
            pipelineTint.loadDefaultProperties();
            System.out.println("loaded default properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        pipelineTint.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, keyphrase, readability, derivatario");
        pipelineTint.setProperty("customAnnotatorClass.derivatario", "eu.fbk.dh.tint.derived.DerivationAnnotator");
        pipelineTint.setProperty("customAnnotatorClass.keyphrase", "eu.fbk.dh.kd.annotator.DigiKDAnnotator");
        pipelineTint.setProperty("keyphrase.language", "ITALIAN");
        pipelineTint.setProperty("keyphrase.prefer_specific_concept", "MEDIUM");
        pipelineTint.setProperty("keyphrase.local_frequency_threshold", "2");
        pipelineTint.load();

        String textItalian = "La polizia francese si è scontrata con i vigili del fuoco che protestano a Parigi per " +
                "chiedere salari più alti e contro le loro condizioni di lavoro. Migliaia i pompieri che hanno " +
                "partecipato al corteo. La polizia ha sparato gas lacrimogeni e ha colpito alcuni manifestanti con i " +
                "manganelli.";

        Annotation annotationTint = pipelineTint.runRaw(textItalian);

        // Keywords
        System.out.println(annotationTint.get(DigiKDAnnotations.KeyphrasesAnnotation.class));

        // Gulpease
        Readability readability = annotationTint.get(ReadabilityAnnotations.ReadabilityAnnotation.class);
        System.out.println(readability.getMeasures().get("main"));

        for (CoreMap sentence : annotationTint.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {

                // Token
                System.out.println(token.originalText());

                // POS
                System.out.println(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));

                // Derivatario
                Derivation derivation = token.get(DerivationAnnotations.DerivationAnnotation.class);
                if (derivation != null) {
                    System.out.println(derivation.getBaseLemma());
                    System.out.println(derivation.getBaseType());
                    System.out.println(derivation.getPhases());
                }
            }
        }

    }
}
