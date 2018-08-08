package eu.fbk.dh.utils.srb;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateIndex {

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./create-index")
                    .withHeader(
                            "Create index for SRB")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withOption("c", "ignore-case", "Ignore case")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            boolean ignoreCase = cmd.hasOption("ignore-case");

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            // Create Index
            Map<String, Integer> index = new LinkedHashMap<>();
            int i = 0;
            for (File file : inputFolder.listFiles()) {
                if (!file.getName().endsWith("it")) {
                    continue;
                }

                List<String> lines = Files.readLines(file, Charsets.UTF_8);
                for (String line : lines) {
                    String[] parts = line.split("\\s+");
                    for (String part : parts) {
                        if (ignoreCase) {
                            part = part.toLowerCase();
                        }
                        if (!index.containsKey(part)) {
                            index.put(part, ++i);
                        }
                    }
                }
            }

            for (File file : inputFolder.listFiles()) {
                if (!file.getName().endsWith("it")) {
                    continue;
                }

                File outFile = new File(outputFolder.getAbsolutePath() + File.separator + file.getName());

                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

                List<String> lines = Files.readLines(file, Charsets.UTF_8);
                for (String line : lines) {
                    String[] parts = line.split("\\s+");
                    StringBuffer buffer = new StringBuffer();
                    for (String part : parts) {
                        if (ignoreCase) {
                            part = part.toLowerCase();
                        }
                        Integer id = index.get(part);
                        buffer.append(id.toString()).append(" ");
                    }
                    writer.append(buffer.toString().trim());
                    writer.append("\n");
                }

                writer.close();
            }

            File indexFile = new File(outputFolder.getAbsolutePath() + File.separator + "index");
            BufferedWriter writer = new BufferedWriter(new FileWriter(indexFile));
            for (String key : index.keySet()) {
                Integer value = index.get(key);
                writer.append(key).append("\t").append(value.toString()).append("\n");
            }
            writer.close();

            System.out.println(index.size());

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
