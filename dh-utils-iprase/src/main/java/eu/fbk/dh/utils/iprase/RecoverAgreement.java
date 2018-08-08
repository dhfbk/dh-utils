package eu.fbk.dh.utils.iprase;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecoverAgreement {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./recover-agreement")
                    .withHeader(
                            "Recover agreement information")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            Map<String, Set<Integer>> files = new HashMap<>();

            for (File folder : inputFolder.listFiles()) {
                if (!folder.isDirectory()) {
                    continue;
                }

                files.put(folder.getName(), new HashSet<>());
                for (File file : folder.listFiles()) {
                    if (!file.getName().endsWith(".txt.json")) {
                        continue;
                    }

                    Integer intName = Integer.parseInt(file.getName().replaceAll("[^0-9]", ""));
                    files.get(folder.getName()).add(intName);
                }
            }

            for (String key1 : files.keySet()) {
                for (String key2 : files.keySet()) {
                    if (key1.equals(key2)) {
                        continue;
                    }

                    Set<Integer> set1 = files.get(key1);
                    Set<Integer> set2 = files.get(key2);

                    Set<Integer> intersection = new HashSet<>(set1);
                    intersection.retainAll(set2);
                    if (intersection.size() > 0) {
                        System.out.println(String.format("%s - %s -> %s", key1, key2, intersection.toString()));
                    }
                }
            }


        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
