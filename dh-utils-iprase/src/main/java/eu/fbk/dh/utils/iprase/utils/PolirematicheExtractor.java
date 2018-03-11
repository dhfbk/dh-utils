package eu.fbk.dh.utils.iprase.utils;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;

public class PolirematicheExtractor {

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./polirematiche-extractor")
                    .withHeader(
                            "Extracts polirematiche from De Mauro file")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("\t");
                line = parts[0];

                line = line.replaceAll("'", "' ");

                writer.append(line).append("\n");
                String noHyphen = line.replaceAll("-", "");
                if (!noHyphen.equals(line)) {
                    writer.append(noHyphen).append("\n");
                }
            }

            writer.close();
            reader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
