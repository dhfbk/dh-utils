package eu.fbk.dh.utils.resources;

import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import kotlin.text.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * Created by alessio on 08/06/17.
 */

public class Holden {

    private static final Logger LOGGER = LoggerFactory.getLogger(Holden.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine.parser().withName("holden")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "DIR", CommandLine.Type.DIRECTORY, true, false, true)
                    .withOption("j", "join", "Join lines")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            Boolean joinLines = cmd.hasOption("join");

            if (outputFile.exists() && !outputFile.isDirectory()) {
                LOGGER.error("Output folder must be a directory");
                System.exit(1);
            }

            if (!outputFile.exists() && !outputFile.mkdirs()) {
                LOGGER.error("Cannot create output folder");
                System.exit(1);
            }

            int i = 0;
            boolean skipNextLine = false;
            List<String> lines = Files.readLines(inputFile, Charsets.UTF_8);
            StringBuffer buffer = new StringBuffer();
            for (String line : lines) {
                if (skipNextLine) {
                    skipNextLine = false;
                    continue;
                }

                if (line.startsWith("----------")) {
                    skipNextLine = true;
                    String all = buffer.toString().trim();
                    if (all.length() > 0) {
                        i++;
                        File newFile = new File(outputFile.getAbsolutePath() + File.separator + i + ".txt");
                        Files.write(all, newFile, com.google.common.base.Charsets.UTF_8);
                    }
                    buffer = new StringBuffer();
                    continue;
                }

                line = line.replaceAll("í", "ì");
                line = line.replaceAll("ú", "ù");

                char separator = '\n';
                if (joinLines) {
                    separator = ' ';
                }

                buffer.append(separator).append(line);
            }

            String all = buffer.toString().trim();
            if (all.length() > 0) {
                i++;
                File newFile = new File(outputFile.getAbsolutePath() + File.separator + i + ".txt");
                Files.write(all, newFile, com.google.common.base.Charsets.UTF_8);
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
