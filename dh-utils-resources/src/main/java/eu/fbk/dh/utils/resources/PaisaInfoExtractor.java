package eu.fbk.dh.utils.resources;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;


public class PaisaInfoExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaisaInfoExtractor.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./paisa-info-extractor")
                    .withHeader(
                            "Extracts information from Paisa JSONs")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            JsonParser parser = new JsonParser();

            for (File folder : inputFolder.listFiles()) {
                if (!folder.isDirectory()) {
                    continue;
                }
                if (folder.getName().startsWith(".")) {
                    continue;
                }

                LOGGER.info("Folder: {}", folder.getName());

                for (File file : folder.listFiles()) {
                    if (!file.getName().endsWith(".json.gz")) {
                        continue;
                    }

                    FileInputStream stream = new FileInputStream(file);
                    GZIPInputStream zStream = new GZIPInputStream(stream);
                    JsonObject o = parser.parse(new InputStreamReader(zStream)).getAsJsonObject();

                    JsonObject readability = o.getAsJsonObject("readability");

                    Map<Integer, Integer> deeps = new HashMap<>();
                    try {
                        JsonObject deepsObj = readability.getAsJsonObject("deeps");
                        for (Map.Entry<String, JsonElement> obj : deepsObj.entrySet()) {
                            deeps.put(Integer.parseInt(obj.getKey()), obj.getValue().getAsInt());
                        }
                    }
                    catch (Exception e) {
                        LOGGER.warn("Missing deeps field [file {}]", file.getName());
                        continue;
                    }

                    JsonArray sentences = o.getAsJsonArray("sentences");
                    for (JsonElement sentence : sentences) {
                        JsonObject sentenceObject = sentence.getAsJsonObject();
                        int index = sentenceObject.get("index").getAsInt();

                        Set<Integer> literals = new HashSet<>();
                        Set<Integer> verbs = new HashSet<>();
                        Set<Integer> auxVerbs = new HashSet<>();

                        JsonArray tokens = sentenceObject.getAsJsonArray("tokens");
                        int sum = 0;
                        int num = 0;
                        for (JsonElement token : tokens) {
                            int tokenIndex = token.getAsJsonObject().get("index").getAsInt();
                            boolean literalWord = false;
                            try {
                                literalWord = token.getAsJsonObject().get("literalWord").getAsBoolean();
                            } catch (Exception e) {
                                LOGGER.warn("No literalWord [file {} sent {}]", file.getName(), index);
                            }

                            if (!literalWord) {
                                continue;
                            }
                            literals.add(tokenIndex);
                            sum += token.getAsJsonObject().get("word").getAsString().length();
                            num++;

                            String pos = token.getAsJsonObject().get("pos").getAsString();
                            if (pos.equals("V")) {
                                verbs.add(tokenIndex);
                            }
                            else {
                                if (pos.startsWith("V")) {
                                    auxVerbs.add(tokenIndex);
                                }
                            }
                        }

                        JsonArray dependencies = sentenceObject.getAsJsonArray("basic-dependencies");
                        FrequencyHashSet<Integer> children = new FrequencyHashSet<>();
                        for (JsonElement dependency : dependencies) {
                            int governor = dependency.getAsJsonObject().get("governor").getAsInt();
                            int dependent = dependency.getAsJsonObject().get("dependent").getAsInt();
                            if (literals.contains(dependent) && !auxVerbs.contains(dependent)) {
                                children.add(governor);
                            }
                        }

                        int arSum = 0;
                        for (Integer verb : verbs) {
                            arSum += children.getZero(verb);
                        }

                        double ariety = 1.0;
                        if (verbs.size() > 0) {
                            ariety = arSum * 1.0 / verbs.size();
                        }
                        double avg = sum * 1.0 / num;

                        writer.append(file.getName()).append('\t'); // File name
                        writer.append(Integer.toString(index)).append('\t'); // Sentence index
                        writer.append(Integer.toString(num)).append('\t'); // Token count
                        writer.append(Double.toString(avg)).append('\t'); // Average word length
                        writer.append(Integer.toString(deeps.get(index))).append('\t'); // Depth
                        writer.append(Double.toString(ariety)).append('\n'); // Average ariety of verbs
                    }

                    zStream.close();
                    stream.close();

                    writer.flush();
                }

            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
