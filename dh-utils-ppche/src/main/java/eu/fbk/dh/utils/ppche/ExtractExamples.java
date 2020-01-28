package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtractExamples {

    private static Set<String> ignoreTags = new HashSet<>();
    private static Pattern slashPattern = Pattern.compile("^(.*)/([^/]+)$");
    private static Integer NUM_EXAMPLES = 3;

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
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            HashMultimap<String, String> words = HashMultimap.create();

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            for (File file : inputFolder.listFiles()) {
                if (!file.getName().endsWith(".pos")) {
                    continue;
                }

                List<String> lines = Files.readAllLines(file.toPath(), Charsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    Matcher matcher = slashPattern.matcher(line);
                    if (matcher.find()) {
                        String word = matcher.group(1);
                        String tag = matcher.group(2);
                        if (ignoreTags.contains(tag)) {
                            continue;
                        }
                        words.put(tag, word);
                    }
                }
            }

            for (String key : words.keySet()) {
                int i = 0;
                writer.append("### ");
                writer.append(key);
                writer.append("\n");
                for (String word : words.get(key)) {
                    if (++i > NUM_EXAMPLES) {
                        break;
                    }
                    writer.append(word);
                    writer.append("\n");
                }
                writer.append("\n");
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
