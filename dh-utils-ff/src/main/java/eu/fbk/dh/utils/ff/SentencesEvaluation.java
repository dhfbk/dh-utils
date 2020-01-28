package eu.fbk.dh.utils.ff;

import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class SentencesEvaluation {

    public static Set<String> blackList = new HashSet<>();

    static {
        blackList.add("essere");
        blackList.add("avere");
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./sentences-evaluation")
                    .withHeader("Parse sentences for evaluation")
                    .withOption("s", "sentences", "Sentences file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("r", "results", "Results file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("w", "words", "Words file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("m", "metrics", "Metrics file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("l", "lang", "Lang", "CODE", CommandLine.Type.STRING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("f", "output-frequencies", "Output file for frequencies", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption(null, "demauro3", "Output file for De Mauro 3", "FILE", CommandLine.Type.FILE, true, false, false)
                    .withOption(null, "demauro4", "Output file for De Mauro 4", "FILE", CommandLine.Type.FILE, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File sentencesFile = cmd.getOptionValue("sentences", File.class);
            File resultsFile = cmd.getOptionValue("results", File.class);
            File wordsFile = cmd.getOptionValue("words", File.class);
            File metricsFile = cmd.getOptionValue("metrics", File.class);
            String globalLang = cmd.getOptionValue("lang", String.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            File freqFile = cmd.getOptionValue("output-frequencies", File.class);

            File deMauro3File = cmd.getOptionValue("demauro3", File.class);
            File deMauro4File = cmd.getOptionValue("demauro4", File.class);

            List<String> results = Files.readAllLines(resultsFile.toPath());
            List<String> rawWords = Files.readAllLines(wordsFile.toPath());
            List<String> rawMetrics = Files.readAllLines(metricsFile.toPath());

            Set<String> cognates = new HashSet<>();
            for (String rawMetric : rawMetrics) {
                String[] parts = rawMetric.split("/");
                String word = parts[0];
                double value = Double.parseDouble(parts[parts.length - 1]);

                if (value >= 2) {
                    cognates.add(word);
                }
            }

            if (rawWords.size() != results.size()) {
                System.err.println("Error in size");
                System.exit(1);
            }

            Map<String, Boolean> falseFriends = new HashMap<>();

            for (int i = 0; i < rawWords.size(); i++) {
                String rawWord = rawWords.get(i);
                String result = results.get(i);

                String[] parts = rawWord.split("\t");
                String word = parts[1];
                if (cognates.contains(word)) {
                    result = "0";
                }

                falseFriends.put(word, result.equals("1"));
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            BufferedReader reader = new BufferedReader(new FileReader(sentencesFile));

            FrequencyHashSet<String> frequencies = new FrequencyHashSet<>();
            FrequencyHashSet<String> frequenciesD3 = new FrequencyHashSet<>();
            FrequencyHashSet<String> frequenciesD4 = new FrequencyHashSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }

                String sentence = parts[1];

                writer.append("# ");
                writer.append(sentence);
                writer.append("\n");

                int steps = (parts.length - 2) / 4;
                for (int i = 0; i < steps; i++) {
                    Integer deMauro = Integer.parseInt(parts[2 + i * 4 + 1]);
                    String word = parts[2 + i * 4 + 3];
                    String lang = parts[2 + i * 4 + 2];

                    if (blackList.contains(word)) {
                        continue;
                    }

                    if (deMauro >= 3) {
                        frequenciesD3.add(word);
                    }
                    if (deMauro >= 4) {
                        frequenciesD4.add(word);
                    }

                    if (!lang.equals("xx") && !lang.equals(globalLang)) {
                        continue;
                    }

                    if (!falseFriends.containsKey(word)) {
//                        System.out.println(word);
                        falseFriends.put(word, false);
                    }

                    writer.append(globalLang).append("\t");
                    writer.append(word).append("\t");
                    writer.append(globalLang.equals(lang) ? "1" : "0").append("\t");
                    writer.append(falseFriends.get(word) ? "1" : "0").append("\t");
                    writer.append(deMauro.toString()).append("\n");

                    if (falseFriends.get(word)) {
                        frequencies.add(word);
                    }
                }
            }

            reader.close();
            writer.close();

            writer = new BufferedWriter(new FileWriter(freqFile));
            for (String key : frequencies.keySet()) {
                writer.append(globalLang).append("\t").append(key).append("\t").append(frequencies.get(key).toString()).append("\n");
            }
            writer.close();

            if (deMauro3File != null) {
                writer = new BufferedWriter(new FileWriter(deMauro3File));
                for (String key : frequenciesD3.keySet()) {
                    writer.append("dm3").append("\t").append(key).append("\t").append(frequenciesD3.get(key).toString()).append("\n");
                }
                writer.close();
            }
            if (deMauro4File != null) {
                writer = new BufferedWriter(new FileWriter(deMauro4File));
                for (String key : frequenciesD4.keySet()) {
                    writer.append("dm4").append("\t").append(key).append("\t").append(frequenciesD4.get(key).toString()).append("\n");
                }
                writer.close();
            }

//            System.out.println(frequencies);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
