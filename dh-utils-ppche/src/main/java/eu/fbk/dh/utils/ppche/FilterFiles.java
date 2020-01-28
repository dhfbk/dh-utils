package eu.fbk.dh.utils.ppche;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterFiles {

    final static Pattern filePattern = Pattern.compile("(.*-e[0-9])");
    final static Map<String, String> mapping = new HashMap<>();
    static {
        mapping.put("harleyedw-e2", "harley-e2");
        mapping.put("wpaston-e2", "wpaston2-e2");
        mapping.put("conway-e3", "conway2-e3");
        mapping.put("thoward-e2", "thoward2-e2");
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./filter-files")
                    .withHeader("Filter files based on list")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("l", "list", "Input list", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File listFile = cmd.getOptionValue("list", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);

            outputFolder.mkdirs();
            Map<String, String> files = new HashMap<>();

            BufferedReader reader = new BufferedReader(new FileReader(listFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                String fileName = parts[0];
                String category = parts[1].replaceAll("[^a-z0-9A-Z-]+", "-");
                File categoryFolder = new File(outputFolder.getAbsolutePath() + File.separator + category);
                categoryFolder.mkdirs();
                files.put(fileName, category);
                if (fileName.contains("-e12")) {
                    files.put(fileName.replaceAll("-e12", "-e1"), category);
                    files.put(fileName.replaceAll("-e12", "-e2"), category);
                }
            }
            reader.close();

            for (File file : inputFolder.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }

                String name = file.getName();
                Matcher matcher = filePattern.matcher(name);
                if (matcher.find()) {
                    String g = matcher.group(1);
                    g = g.replaceAll("[0-9]{4}-", "");
                    g = g.replaceAll("-[a-z]-", "-");
                    g = g.replaceAll("[0-9]+-e([0-9])", "-e$1");
                    g = g.replaceAll("^stat-e([0-9])", "stat-period$1-e$1");

                    if (mapping.containsKey(g)) {
                        g = mapping.get(g);
                    }

                    if (!files.containsKey(g)) {
                        System.out.println("Unable to find " + g);
                        continue;
                    }

                    String category = files.get(g);
                    File newFile = new File(outputFolder.getAbsolutePath() + File.separator + category + File.separator + name);
                    if (newFile.exists()) {
                        newFile.delete();
                    }
                    Files.copy(file.toPath(), newFile.toPath());
                }
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
