package eu.fbk.dh.utils.ff;

import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FrequenciesStatistics {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./freq-stats")
                    .withHeader("Frequencies statistics")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);

            Map<String, FrequencyHashSet<String>> frequencies = new HashMap<>();

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }

                String lang = parts[0];
                String word = parts[1];
                Integer frequency = Integer.parseInt(parts[2]);

                frequencies.putIfAbsent(lang, new FrequencyHashSet<>());
                frequencies.get(lang).add(word, frequency);
            }

            for (String lang : frequencies.keySet()) {
                int wordTotal = 0, wordTotalUnique = 0;
                int wordCount = 0, wordCountUnique = 0;
                for (String word : frequencies.get(lang).keySet()) {
                    wordTotal += word.length() * frequencies.get(lang).get(word);
                    wordTotalUnique += word.length();
                    wordCount += frequencies.get(lang).get(word);
                    wordCountUnique += 1;
                }
                System.out.println("### " + lang);
                System.out.println("  Non unique average length: " + wordTotal * 1.0 / wordCount);
                System.out.println("  Unique average length: " + wordTotalUnique * 1.0 / wordCountUnique);
            }

            Set<String> doneLangs = new HashSet<>();
            for (String l1 : frequencies.keySet()) {
                doneLangs.add(l1);
                int total = 0;
                Set<String> l1Words = new HashSet<>(frequencies.get(l1).keySet());
                for (String l2 : frequencies.keySet()) {
                    if (l2.equals(l1) || l2.startsWith("dm")) {
                        continue;
                    }

                    l1Words.removeAll(frequencies.get(l2).keySet());
                }

                for (String l1Word : l1Words) {
                    total += frequencies.get(l1).get(l1Word);
                }


                System.out.println("### " + l1);
                System.out.printf("  Simplified only for %s: %d (%d)\n", l1, total, l1Words.size());
//                System.out.println("  Simplified only for " + l1 + ": " + total);
//                System.out.println("  Uniquely simplified only for " + l1 + ": " + l1Words.size());

                for (String l2 : frequencies.keySet()) {
                    if (doneLangs.contains(l2)) {
                        continue;
                    }

                    System.out.println("  " + l1 + " --- " + l2);

                    Set<String> k1 = frequencies.get(l1).keySet();
                    Set<String> k2 = frequencies.get(l2).keySet();

                    System.out.printf("  Simplified for %s: %d (%d)\n", l1, frequencies.get(l1).sum(), k1.size());
                    System.out.printf("  Simplified for %s: %d (%d)\n", l2, frequencies.get(l2).sum(), k2.size());
//                    System.out.println("  Simplified for " + l1 + ": " + frequencies.get(l1).sum());
//                    System.out.println("  Simplified for " + l2 + ": " + frequencies.get(l2).sum());
//                    System.out.println("  Simplified for " + l1 + " (unique): " + k1.size());
//                    System.out.println("  Simplified for " + l2 + " (unique): " + k2.size());

                    int intSize = 0;
                    for (String s : k1) {
                        intSize += Math.min(frequencies.get(l1).getZero(s), frequencies.get(l2).getZero(s));
                    }
//                    System.out.println("  Intersection size: " + intSize);
                    Set<String> k = new HashSet<>(k1);
                    k.retainAll(k2);
                    System.out.printf("  Intersection size: %d (%d)\n", intSize, k.size());
//                    System.out.println("  Intesection size (unique): " + k.size());
                }
            }

            reader.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
