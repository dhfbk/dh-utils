package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FilterPos {

    public static void main(String[] args) {
        File inputFolder = new File("/Users/alessio/Desktop/PPCHE_4.0_corpus_files/pos/antico/mx4");
        File outputFolder = new File("/Users/alessio/Desktop/PPCHE_4.0_corpus_files/pos-training/a-mx4");
        double percentage = 0.1d;
        int fileMinLength = 5000;

        Set<String> skipTags = new HashSet<>();
        skipTags.add("CODE");
        skipTags.add("ID");

        try {
            outputFolder.mkdirs();

            File outputLog = new File(outputFolder.getAbsolutePath() + File.separator + "log");
            File outputTrain = new File(outputFolder.getAbsolutePath() + File.separator + "train");
            File outputTest = new File(outputFolder.getAbsolutePath() + File.separator + "test");

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

                boolean isTest = false;

                if (testCount * skipLen < count && testCount < filesForTest && file.length() >= fileMinLength) {
                    isTest = true;
                    testCount++;
                }

                count++;

                logWriter.append(file.getName()).append("\t").append(isTest ? "TEST" : "TRAIN").append("\n");

                boolean newSentence = false;
                StringBuffer buffer = new StringBuffer();

                for (String line : Files.readLines(file, Charsets.UTF_8)) {
                    line = line.trim();
                    if (line.length() == 0) {
                        newSentence = true;
                        continue;
                    }

                    String[] parts = line.split("/");
                    String pos = parts[1].toUpperCase();
                    if (skipTags.contains(pos)) {
                        continue;
                    }

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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
