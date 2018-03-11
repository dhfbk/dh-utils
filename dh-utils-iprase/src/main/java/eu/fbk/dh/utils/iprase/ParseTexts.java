package eu.fbk.dh.utils.iprase;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ParseTexts {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseTexts.class);

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./parse-texts")
                    .withHeader(
                            "Parse texts with Tint")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withOption("n", "num-files", "Number of files to parse (default: all)", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true,
                            false, false)
                    .withOption("w", "skip-overwrite", "Skip files if they exist in output folder")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            Integer numFiles = cmd.getOptionValue("num-files", Integer.class, 0);
            boolean overwrite = !cmd.hasOption("skip-overwrite");

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();

            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ita_verb, ita_derivatario, readability, " +
                    "monosillabi, apostrofi, maiuscole, articoli, loro, gli, quest, trivial, imperfetti, gerundi, ind_pres, " +
                    "stare_andare, affissi, nominali, connettivi, congiunzioni, punteggiatura, perche_quando, gergale, anglicismi, " +
                    "politicamente_corretto, polirematiche, plastismi, frasi_scisse, li, d_eufonica, " +
//                    "perche_quando" +
                    "");

            pipeline.setProperty("customAnnotatorClass.ita_derivatario", "eu.fbk.dh.tint.derived.DerivationAnnotator");

            pipeline.addProperties(IpraseProperties.properties);

            pipeline.load();

            int i = 0;

            File[] files = inputFolder.listFiles();

            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }

                if (!file.getName().endsWith(".txt")) {
                    continue;
                }

                if (++i > numFiles && numFiles > 0) {
                    break;
                }

                LOGGER.info(String.format("%s - %d/%d", file.getName(), i, files.length));
                String jsonFileName = outputFolder.getAbsolutePath() + File.separator + file.getName() + ".json";
                File outputFile = new File(jsonFileName);

                if (outputFile.exists() && outputFile.length() > 0 && !overwrite) {
                    LOGGER.info("Skipping file {}", outputFile.getName());
                    continue;
                }
                String text = Files.toString(file, Charsets.UTF_8);
                text = text.replaceAll("\\.{3,}", "...");

//                text = "Gianni non prestava molta attenzione ad alcune cose.";
                Annotation annotation = pipeline.runRaw(text);
                String json = JSONOutputter.jsonPrint(annotation);

                Files.write(json, outputFile, Charsets.UTF_8);

                if (i == 1) {
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        System.out.println(sentence.get(CoreAnnotations.TextAnnotation.class));
                    }

//                    LOGGER.debug(json);
                }
            }
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
