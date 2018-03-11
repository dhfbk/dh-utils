package eu.fbk.dh.utils.iprase;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.*;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Statistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(Statistics.class);
    private static final int countTask = 14;
    private static final Double ratio = 10000.0;
    private static final Set<Integer> tasks = new HashSet<>();
    private static final DecimalFormat df = new DecimalFormat("#.000");

    static {
        tasks.add(5);
        tasks.add(8);
        tasks.add(14);
        tasks.add(16);
        tasks.add(17);
        tasks.add(28);
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-statistics")
                    .withHeader(
                            "Get statistics from JSON files")
                    .withOption("i", "input", "Input JSON folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("t", "tsv", "TSV years file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File tsvFile = cmd.getOptionValue("tsv", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            Map<String, String> years = new HashMap<>();
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                BigDecimal value = BigDecimal.valueOf(src);
                return new JsonPrimitive(df.format(value).replaceAll("\\.000", ""));
            });

            Gson gson = builder.setPrettyPrinting().create();

            Map<Integer, Map<String, Map<String, Double>>> results = new HashMap<>();

            for (Integer task : tasks) {
                results.put(task, new HashMap<>());
            }


            BufferedReader reader = new BufferedReader(new FileReader(tsvFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                years.put(parts[0], parts[1]);
            }
            reader.close();

            for (File file : inputFolder.listFiles()) {
                if (!file.isFile()) {
                    continue;
                }
                if (!file.getName().endsWith(".json")) {
                    continue;
                }

                String id = file.getName().replaceAll("[^0-9]", "");
                String year = years.get(id);

                if (year == null) {
                    LOGGER.warn("No year set for {}", id);
                    continue;
                }

                JsonObject myJson = gson.fromJson(Files.toString(file, Charsets.UTF_8), JsonObject.class);
                JsonArray catTasks = myJson.get("cat_tasks").getAsJsonArray();
                for (JsonElement catTask : catTasks) {
                    int taskID = catTask.getAsJsonObject().get("taskID").getAsInt();

                    if (!tasks.contains(taskID)) {
                        continue;
                    }

                    JsonObject frequencies;
                    try {
                        frequencies = catTask.getAsJsonObject().get("statistics").getAsJsonObject()
                                .get("frequencies").getAsJsonObject().get("support").getAsJsonObject();
                    } catch (NullPointerException e) {
                        LOGGER.warn("ID {} - Task {}", id, taskID);
                        continue;
                    }

                    for (Map.Entry<String, JsonElement> entry : frequencies.entrySet()) {
                        String key = entry.getKey();
                        int value = entry.getValue().getAsInt();

                        results.get(taskID).putIfAbsent(year, new HashMap<>());
                        Double currentValue = results.get(taskID).get(year).getOrDefault(key, 0d);
                        currentValue += value;
                        results.get(taskID).get(year).put(key, (double) currentValue);
                    }
                }
            }

            FrequencyHashSet<String> tokens = new FrequencyHashSet<>();
            for (String year : results.get(countTask).keySet()) {
                Double words = results.get(countTask).get(year).get("words");
                tokens.add(year, words.intValue());
            }

            for (Integer taskID : results.keySet()) {
                if (taskID.equals(countTask)) {
                    continue;
                }

                for (String year : results.get(taskID).keySet()) {
                    for (String key : results.get(taskID).get(year).keySet()) {
                        Double value = results.get(taskID).get(year).get(key);
                        Double normVal = tokens.get(year) / ratio;
//                        System.out.println(value + " ---> " + normVal);
                        results.get(taskID).get(year).put(key, value / normVal);
                    }

                }
            }

            Files.write(gson.toJson(results), outputFile, Charsets.UTF_8);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
