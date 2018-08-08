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
import java.util.*;

public class Statistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(Statistics.class);
    private static final int countTask = 14;
    private static final Double ratio = 10000.0;
    private static final Set<Integer> tasks = new HashSet<>();
    private static final Set<Integer> countTasks = new HashSet<>();
    private static final DecimalFormat df = new DecimalFormat("#.000");

    static {
        tasks.add(2);
        tasks.add(4);
        tasks.add(5);
        tasks.add(8);
        tasks.add(12);
        tasks.add(14);
        tasks.add(16);
        tasks.add(17);
        tasks.add(18);
        tasks.add(28);
        countTasks.add(14);
        countTasks.add(0);
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
            Map<String, String> schools = new HashMap<>();

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Double.class, (JsonSerializer<Double>) (src, typeOfSrc, context) -> {
                BigDecimal value = BigDecimal.valueOf(src);
                return new JsonPrimitive(df.format(value).replaceAll("\\.000", ""));
            });

            Gson gson = builder.setPrettyPrinting().create();

            Map<Integer, Map<String, Map<String, Double>>> yearResults = new HashMap<>();
            Map<String, Map<String, Set<Double>>> yearMeasures = new HashMap<>();
            Map<Integer, Map<String, Map<String, Double>>> schoolResults = new HashMap<>();
            Map<String, Map<String, Set<Double>>> schoolMeasures = new HashMap<>();
            for (Integer task : tasks) {
                yearResults.put(task, new HashMap<>());
                schoolResults.put(task, new HashMap<>());
            }

            BufferedReader reader = new BufferedReader(new FileReader(tsvFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }
                if (parts.length > 4) {
                    schools.put(parts[0], parts[4]);
                }

                years.put(parts[0], parts[1]);
            }
            reader.close();

            Set<String> onlyYears = new HashSet<>(years.values());
            Set<String> onlySchools = new HashSet<>(schools.values());

            for (File file : inputFolder.listFiles()) {
                if (!file.isFile()) {
                    continue;
                }
                if (!file.getName().endsWith(".json")) {
                    continue;
                }

                LOGGER.info("File: {}", file.getName());

                String id = file.getName().replaceAll("[^0-9]", "");
                String year = years.get(id);
                String school = schools.get(id);

                JsonObject myJson = gson.fromJson(Files.toString(file, Charsets.UTF_8), JsonObject.class);
                populateResults(yearResults, myJson, id, year, yearMeasures);
                populateResults(schoolResults, myJson, id, school, schoolMeasures);
            }

            postProcess(yearResults, yearMeasures, onlyYears);
            postProcess(schoolResults, schoolMeasures, onlySchools);

            List<Map> maps = new ArrayList<>();
            maps.add(yearResults);
            maps.add(schoolResults);

            Files.write(gson.toJson(maps), outputFile, Charsets.UTF_8);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    private static void postProcess(Map<Integer, Map<String, Map<String, Double>>> results, Map<String, Map<String, Set<Double>>> measures, Set<String> list) {
        for (String year : list) {
            updateResults(results, year, 16, "affinch", "#affinche");
            updateResults(results, year, 16, "giacch", "#giacche");
            updateResults(results, year, 18, "perch", "#perche");
        }

        results.put(0, new HashMap<>());
        for (String year : measures.keySet()) {
            results.get(0).put(year, new HashMap<>());
            for (String measureName : measures.get(year).keySet()) {
                Set<Double> values = measures.get(year).get(measureName);
                results.get(0).get(year).put(measureName, values.stream().mapToDouble(v -> v).average().getAsDouble());
            }
        }

        FrequencyHashSet<String> tokens = new FrequencyHashSet<>();
        for (String year : results.get(countTask).keySet()) {
            Double words = results.get(countTask).get(year).get("words");
            tokens.add(year, words.intValue());
        }

        for (Integer taskID : results.keySet()) {
            if (countTasks.contains(taskID)) {
                continue;
            }

            for (String year : results.get(taskID).keySet()) {
                for (String key : results.get(taskID).get(year).keySet()) {
                    Double value = results.get(taskID).get(year).get(key);
                    Double normVal = tokens.get(year) / ratio;
                    results.get(taskID).get(year).put(key, value / normVal);
                }

            }
        }

    }

    private static void populateResults(Map<Integer, Map<String, Map<String, Double>>> results, JsonObject myJson, String id, String parameter, Map<String, Map<String, Set<Double>>> measures) {
        if (parameter == null) {
            LOGGER.warn("No parameter set for {}", id);
            return;
        }

        try {
            JsonObject readabilityMeasures = myJson.get("readability").getAsJsonObject().get("measures").getAsJsonObject();
            measures.putIfAbsent(parameter, new HashMap<>());
            for (Map.Entry<String, JsonElement> measure : readabilityMeasures.entrySet()) {
                measures.get(parameter).putIfAbsent(measure.getKey(), new HashSet<>());
                measures.get(parameter).get(measure.getKey()).add(measure.getValue().getAsDouble());
            }
            double ttrValue = myJson.get("readability").getAsJsonObject().get("ttrValue").getAsDouble();
            measures.get(parameter).putIfAbsent("ttrValue", new HashSet<>());
            measures.get(parameter).get("ttrValue").add(ttrValue);

            results.putIfAbsent(-1, new HashMap<>());
            results.get(-1).putIfAbsent(parameter, new HashMap<>());
            JsonObject posFrequencies = myJson.get("readability").getAsJsonObject().get("genericPosStats").getAsJsonObject()
                    .get("support").getAsJsonObject();
            for (Map.Entry<String, JsonElement> pos : posFrequencies.entrySet()) {
                Double currentValue = results.get(-1).get(parameter).getOrDefault(pos.getKey(), 0d);
                currentValue += pos.getValue().getAsInt();
                results.get(-1).get(parameter).put(pos.getKey(), currentValue);
            }

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

                    results.get(taskID).putIfAbsent(parameter, new HashMap<>());
                    Double currentValue = results.get(taskID).get(parameter).getOrDefault(key, 0d);
                    currentValue += value;
                    results.get(taskID).get(parameter).put(key, currentValue);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private static void updateResults(Map<Integer, Map<String, Map<String, Double>>> results, String year, int taskID, String strToCheck, String finalString) {
        double total = 0.0d;
        if (results.get(taskID).containsKey(year)) {
            for (String key : results.get(taskID).get(year).keySet()) {
                if (key.startsWith(strToCheck)) {
                    total += results.get(taskID).get(year).get(key);
                }
            }
            results.get(taskID).get(year).put(finalString, total);
        }
    }
}
