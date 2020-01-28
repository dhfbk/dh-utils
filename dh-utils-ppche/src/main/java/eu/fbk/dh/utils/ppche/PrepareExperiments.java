package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrepareExperiments {

    static public Pattern listPattern = Pattern.compile("Training ([a-z0-9+]+) -> Test ([a-z0-9+]+)");

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./prepare-experiments")
                    .withHeader(
                            "Prepare experiments for Stanford")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File listFile = new File(inputFolder.getAbsolutePath() + File.separator + "list.txt");
            File dataFolder = new File(inputFolder.getAbsolutePath() + File.separator + "_data");

            for (String line : Files.readLines(listFile, Charsets.UTF_8)) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }
                Matcher matcher = listPattern.matcher(line);
                if (matcher.find()) {
                    String trainString = matcher.group(1);
                    String testString = matcher.group(2);

                    File outFolder = new File(inputFolder.getAbsolutePath() + File.separator + trainString + "-" + testString);
                    outFolder.mkdirs();

                    File trainFile = new File(outFolder.getAbsolutePath() + File.separator + "train");
                    File testFile = new File(outFolder.getAbsolutePath() + File.separator + "test");

                    BufferedWriter writer;
                    String[] parts;

                    writer = new BufferedWriter(new FileWriter(trainFile));
                    parts = trainString.split("\\+");
                    for (String part : parts) {
                        String s = Files.toString(new File(dataFolder.getAbsolutePath() + File.separator + part), Charsets.UTF_8);
                        writer.append(s).append("\n");
                    }
                    writer.close();

                    writer = new BufferedWriter(new FileWriter(testFile));
                    parts = testString.split("\\+");
                    for (String part : parts) {
                        String s = Files.toString(new File(dataFolder.getAbsolutePath() + File.separator + part), Charsets.UTF_8);
                        writer.append(s).append("\n");
                    }
                    writer.close();
                }
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
