package eu.fbk.dh.utils.iprase;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.utils.iprase.utils.IpraseProperties;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParseTexts {

    static class ParseSingle implements Runnable {

        private Properties pipelineProperties;
        private File inputFile;
        private File outputFile;

        public ParseSingle(Properties pipelineProperties, File inputFile, File outputFile) {
            this.pipelineProperties = pipelineProperties;
            this.inputFile = inputFile;
            this.outputFile = outputFile;
        }

        @Override
        public void run() {
            try {
                LOGGER.info(inputFile.getName());
                String text = Files.toString(inputFile, Charsets.UTF_8);

                TintPipeline pipeline = new TintPipeline();
                pipeline.setProps(pipelineProperties);
                pipeline.load();

                Annotation annotation = pipeline.runRaw(text);
                String json = JSONOutputter.jsonPrint(annotation);
                Files.write(json, outputFile, Charsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
                    .withOption("t", "threads", "Number of threads", "THREADS", CommandLine.Type.POSITIVE_INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            Integer numFiles = cmd.getOptionValue("num-files", Integer.class, 0);
            boolean overwrite = !cmd.hasOption("skip-overwrite");
            Integer threads = cmd.getOptionValue("threads", Integer.class, 1);

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();

            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ita_verb, ita_derivatario, readability, " +
                    "monosillabi, apostrofi, maiuscole, articoli, loro, gli, quest, trivial, imperfetti, gerundi, ind_pres, " +
                    "stare_andare, affissi, nominali, connettivi, congiunzioni, punteggiatura, perche_quando, gergale, anglicismi, " +
                    "politicamente_corretto, polirematiche, plastismi, frasi_scisse, li, d_eufonica, " +
//                    "perche_quando, " +
                    "");

            pipeline.setProperty("customAnnotatorClass.ita_derivatario", "eu.fbk.dh.tint.derived.DerivationAnnotator");

            pipeline.addProperties(IpraseProperties.properties);

            pipeline.load();

            Properties pipelineProps = pipeline.getProps();

            int i = 0;

            ExecutorService pool = Executors.newFixedThreadPool(threads);

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

//                LOGGER.info(String.format("%s - %d/%d", file.getName(), i, files.length));
                String jsonFileName = outputFolder.getAbsolutePath() + File.separator + file.getName() + ".json";
                File outputFile = new File(jsonFileName);

                if (outputFile.exists() && outputFile.length() > 0 && !overwrite) {
                    LOGGER.info("Skipping file {}", outputFile.getName());
                    continue;
                }

                pool.submit(new ParseSingle(pipelineProps, file, outputFile));
//                String text = Files.toString(file, Charsets.UTF_8);
//                text = text.replaceAll("\\.{3,}", "...");
//
//                Annotation annotation = pipelineProperties.runRaw(text);
//                String json = JSONOutputter.jsonPrint(annotation);
//
//                Files.write(json, outputFile, Charsets.UTF_8);
            }

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            pool.shutdownNow();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
