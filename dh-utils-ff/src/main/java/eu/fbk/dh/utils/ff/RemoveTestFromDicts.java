package eu.fbk.dh.utils.ff;

import com.google.common.base.Charsets;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoveTestFromDicts {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./remove-test-from-dict")
                    .withHeader("Remove test words from dictionaries")
                    .withOption("i", "input", "Input dictionary file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("t", "input-test", "Input test file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File inputTestFile = cmd.getOptionValue("input-test", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            List<String> dictLines = Files.readAllLines(inputFile.toPath(), Charsets.UTF_8);
            List<String> testLines = Files.readAllLines(inputTestFile.toPath(), Charsets.UTF_8);

            Set<String> testWords = new HashSet<>();
            for (String testLine : testLines) {
                testLine = testLine.trim().toLowerCase();
                String[] parts = testLine.split("\t");
                if (parts.length < 3) {
                    continue;
                }
                String word = parts[1].trim();
                testWords.add(word);
            }


            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (String dictLine : dictLines) {
                dictLine = dictLine.trim();
                String[] parts = dictLine.toLowerCase().split("\t");
                if (parts.length < 2) {
                    continue;
                }
                String word = parts[0].trim();
                if (testWords.contains(word)) {
                    continue;
                }
                writer.append(dictLine).append("\n");
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
