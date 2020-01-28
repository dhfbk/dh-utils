package eu.fbk.dh.utils.tint;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;
import eu.fbk.dh.utils.iprase.utils.IpraseProperties;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MultiWordExtractor {

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./mw-extractor")
                    .withHeader("Multiword extractor")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            for (File file : inputFolder.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    continue;
                }
                String s = Files.toString(file, Charsets.UTF_8);
//                String[] parts = s.split("\n+");
//                s = parts[0];
                TintPipeline pipeline = new TintPipeline();
                pipeline.loadDefaultProperties();
                pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, polirematiche");
                pipeline.addProperties(IpraseProperties.properties);
                pipeline.load();

                String outFileName = outputFile.getAbsolutePath() + "-" + file.getName() + ".json";
                OutputStream outputStream = new FileOutputStream(outFileName);
                pipeline.run(s, outputStream, TintRunner.OutputFormat.JSON);

//                break;
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
