package eu.fbk.dh.utils.ff;

import com.google.common.base.Charsets;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SplitTestTrain {

    public static int testSize = 500;

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./split-test-train")
                    .withHeader("Split test and train")
                    .withOption("i", "input", "Input file with sentences", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("l", "lemmas", "Input file with lemmas", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("t", "output-train-test", "Output file (train/test)", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File lemmaFile = cmd.getOptionValue("lemmas", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            File outputTtFile = cmd.getOptionValue("output-train-test", File.class);

            Set<String> testWords = new HashSet<>();
            Set<String> trainWords = new HashSet<>();

            BufferedWriter ttWriter = new BufferedWriter(new FileWriter(outputTtFile));

            List<String> lemmaLines = Files.readAllLines(lemmaFile.toPath(), Charsets.UTF_8);
            for (String lemmaLine : lemmaLines) {
                lemmaLine = lemmaLine.trim();
                if (lemmaLine.length() == 0) {
                    continue;
                }
                lemmaLine = lemmaLine.toLowerCase();
                String[] parts = lemmaLine.split("\t");
                if (parts.length < 2) {
                    continue;
                }
                String word = parts[0].trim();
                String lang = parts[1].trim();

                if (testWords.size() < testSize) {
                    testWords.add(word);
                    ttWriter.append("test");
                } else {
                    if (!testWords.contains(word)) {
                        trainWords.add(word);
                        ttWriter.append("train");
                    } else {
                        ttWriter.append("test");
                    }
                }
                ttWriter.append("\t").append(word);
                ttWriter.append("\t").append(lang);
                ttWriter.append("\n");
            }

            ttWriter.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String line;
            lineloop:
            while ((line = reader.readLine()) != null) {
                line.trim();
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }
                if (parts[0].equals("0")) {
                    continue;
                }

                for (int i = 2; i < parts.length; i++) {
                    String part = parts[i];
                    int index = i - 2;
                    if (index % 4 == 3) {
                        if (trainWords.contains(part)) {
                            continue lineloop;
                        }
                    }
                }

                writer.append(line).append("\n");
            }

            reader.close();
            writer.close();

//            System.out.println(testWords.size());
//            System.out.println(trainWords.size());

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
