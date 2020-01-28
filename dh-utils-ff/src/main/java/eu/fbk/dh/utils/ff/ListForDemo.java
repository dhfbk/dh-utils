package eu.fbk.dh.utils.ff;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListForDemo {

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-list")
                    .withHeader("Extract list for demo")
                    .withOption("f", "feature-file", "Feature file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("w", "words-file", "Words file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("c", "classification-file", "Words file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("g", "gold-file", "Words file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("feature-file", File.class);
            File wordsFile = cmd.getOptionValue("words-file", File.class);
            File classificationFile = cmd.getOptionValue("classification-file", File.class);
            File goldFile = cmd.getOptionValue("gold-file", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedReader reader;
            String line;

            Set<String> words = new HashSet<>();
            Set<String> falseFriends = new HashSet<>();

            reader = new BufferedReader(new FileReader(inputFile));
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                String word = parts[0].split("/")[0];
                Float value = Float.parseFloat(parts[1].split("/")[parts[1].split("/").length - 1]);

                if (value < 2) {
                    words.add(word.toLowerCase());
                }
            }
            reader.close();

            List<String> classifiedWords = new ArrayList<>();
            List<Integer> classifications = new ArrayList<>();

            reader = new BufferedReader(new FileReader(wordsFile));
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                classifiedWords.add(parts[1].toLowerCase());
            }
            reader.close();

            reader = new BufferedReader(new FileReader(classificationFile));
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    classifications.add(Integer.parseInt(line));
                }
            }
            reader.close();

            for (int i = 1; i < classifiedWords.size(); i++) {
                String classifiedWord = classifiedWords.get(i);
                Integer classification = classifications.get(i - 1);

                if (classification.equals(1) && words.contains(classifiedWord)) {
                    falseFriends.add(classifiedWord);
                }
            }

            reader = new BufferedReader(new FileReader(goldFile));
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                if (parts[0].equals("ff")) {
                    falseFriends.add(parts[1].toLowerCase());
                }
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (String falseFriend : falseFriends) {
                writer.append(falseFriend).append("\n");
            }
            writer.close();

//            System.out.println(falseFriends);
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
