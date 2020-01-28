package eu.fbk.dh.utils.iprase.cat;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class FindAgreementSentences {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./find-agreement-sentences")
                    .withHeader("Find sentences where there is / there is not agreement")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("l", "list", "Input list of documents", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
//                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File inputList = cmd.getOptionValue("list", File.class);
//            File outputFile = cmd.getOptionValue("output", File.class);

//            for (String line : Files.readLines(inputList, Charsets.UTF_8)) {
//                line = line.trim();
//                if (line.length() == 0) {
//                    continue;
//                }
//                System.out.println(line);
//            }


            FrequencyHashSet<String> frequencyHashSet = new FrequencyHashSet<>();

            for (File folder : inputFolder.listFiles()) {
                if (!folder.getName().startsWith("Iprase")) {
                    continue;
                }
                for (File temiFolder : folder.listFiles()) {
                    if (!temiFolder.isDirectory()) {
                        continue;
                    }
                    if (!temiFolder.exists()) {
                        System.out.println(temiFolder);
                        continue;
                    }
                    for (File taskFolder : temiFolder.listFiles()) {
                        if (!taskFolder.isDirectory()) {
                            continue;
                        }
                        for (File file : taskFolder.listFiles()) {
                            frequencyHashSet.add(taskFolder.getName() + "|" + file.getName());
                        }
                    }
                }
            }

            for (Map.Entry<String, Integer> entry : frequencyHashSet.getSorted()) {
                if (entry.getValue() <= 1) {
                    continue;
                }
                System.out.println(entry);
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
