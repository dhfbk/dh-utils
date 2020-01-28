package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractForStanford {

    private static Set<String> ignoreTags = new HashSet<>();
    private static Pattern slashPattern = Pattern.compile("^(.*)/([^/]+)$");
    private static Integer NUM_EXAMPLES = 3;
    private static double percentage = 0.1d;
    private static int fileMinLength = 5000;
    private static boolean replaceNumbers = true;

    static {
        ignoreTags.add("CODE");
        ignoreTags.add("ID");
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-examples")
                    .withHeader("Examples extraction for each POS tag")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("e", "examples", "Examples file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            File examplesFile = cmd.getOptionValue("examples", File.class);

            File outputLog = new File(outputFile.getAbsolutePath() + ".log");
            File outputTrain = new File(outputFile.getAbsolutePath() + ".train");
            File outputTest = new File(outputFile.getAbsolutePath() + ".test");

            Map<String, String> posMap = new HashMap<>();
            int total = 0, first = 0, noFirst = 0;

            for (String line : Files.readAllLines(examplesFile.toPath(), Charsets.UTF_8)) {
                line = line.trim();
                if (!line.startsWith("###")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                if (ignoreTags.contains(parts[1])) {
                    continue;
                }

                total++;

                if (parts.length > 2) {
                    posMap.put(parts[1], parts[2]);

                    String[] plusParts = parts[1].split("\\+");
                    if (plusParts[0].equals(parts[2])) {
                        first++;
                    } else {
                        noFirst++;
                    }
                }
            }

            System.out.println("Total: " + total);
            System.out.println("First: " + first);
            System.out.println("No-first: " + noFirst);

            FrequencyHashSet<String> originalDistribution = new FrequencyHashSet<>();
            FrequencyHashSet<String> newDistribution = new FrequencyHashSet<>();

            BufferedWriter logWriter = new BufferedWriter(new FileWriter(outputLog));
            BufferedWriter testWriter = new BufferedWriter(new FileWriter(outputTest));
            BufferedWriter trainWriter = new BufferedWriter(new FileWriter(outputTrain));

            File[] files = inputFolder.listFiles();
            Arrays.sort(files);
            int filesForTest = (int) Math.ceil(percentage * files.length);
            int skipLen = files.length / filesForTest;
            int count = 0;
            int testCount = 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }

                if (!file.getAbsolutePath().endsWith(".pos")) {
                    continue;
                }

                boolean isTest = false;

                if (testCount * skipLen < count && testCount < filesForTest && file.length() >= fileMinLength) {
                    isTest = true;
                    testCount++;
                }

                count++;

                logWriter.append(file.getName()).append("\t").append(isTest ? "TEST" : "TRAIN").append("\n");

                boolean newSentence = false;
                StringBuffer buffer = new StringBuffer();

                for (String line : com.google.common.io.Files.readLines(file, Charsets.UTF_8)) {
                    line = line.trim();
                    if (line.length() == 0) {
                        newSentence = true;
                        continue;
                    }

                    String[] parts = line.split("/");
                    String pos = parts[1].toUpperCase();
                    if (ignoreTags.contains(pos)) {
                        continue;
                    }

                    originalDistribution.add(pos);
                    if (posMap.containsKey(pos)) {
                        pos = posMap.get(pos);
                    }
                    if (replaceNumbers) {
                        pos = pos.replaceAll("[0-9]+$", "");
                        pos = pos.replaceAll("-$", "");
                    }
                    newDistribution.add(pos);

                    if (newSentence) {
                        if (buffer.length() > 0) {
                            if (isTest) {
                                testWriter.append(buffer.toString().trim()).append("\n");
                            } else {
                                trainWriter.append(buffer.toString().trim()).append("\n");
                            }
                            buffer = new StringBuffer();
                        }

                        newSentence = false;
                    }

                    String word = parts[0].trim().replaceAll("\\s+", "_");
                    buffer.append(word).append("/").append(pos).append(" ");
                }

                if (buffer.length() > 0) {
                    if (isTest) {
                        testWriter.append(buffer.toString().trim()).append("\n");
                    } else {
                        trainWriter.append(buffer.toString().trim()).append("\n");
                    }
                }
            }

            trainWriter.close();
            testWriter.close();
            logWriter.close();

            System.out.println("Original size: " + originalDistribution.size());
            System.out.println("New size: " + newDistribution.size());

            ArrayList<String> list = new ArrayList<>(newDistribution.keySet());
            Collections.sort(list);
            System.out.println(list);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
