package eu.fbk.dh.utils.iprase;

import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TintTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TintTest.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./parse-text")
                    .withHeader("Parse a text with Tint")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();

            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ita_verb, ita_derivatario, " +
//                    "monosillabi, apostrofi, maiuscole, articoli, loro, gli, li, quest, imperfetti, gerundi, ind_pres, " +
//                    "nominali, connettivi, punteggiatura, perche_quando, affissi, frasi_scisse, trivial, congiunzioni, " +
//                    "plastismi, gergale, politicamente_corretto, anglicismi, polirematiche, stare_andare, d_eufonica, " +
//                    "polirematiche" +
                    "");

            pipeline.setProperty("customAnnotatorClass.ita_derivatario", "eu.fbk.dh.tint.derived.DerivationAnnotator");
            pipeline.addProperties(IpraseProperties.properties);
            pipeline.load();

            String text = "c";
            Annotation annotation = pipeline.runRaw(text);
            String json = JSONOutputter.jsonPrint(annotation);
            System.out.println(json);

//            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//                System.out.println(sentence.get(CoreAnnotations.TextAnnotation.class));
//            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
