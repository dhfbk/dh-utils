package eu.fbk.dh.utils.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;


public class PaisaCollectSimple {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaisaCollectSimple.class);
    private static final Integer DEFAULT_THREADS = 1;

    static JsonParser parser = new JsonParser();

    public static class WorkerThread implements Runnable {

        File file;
        Map<String, Map<Integer, Double>> values;
        BufferedWriter writer;
        AtomicInteger i;

        public WorkerThread(File file, Map<String, Map<Integer, Double>> values, BufferedWriter writer, AtomicInteger i) {
            this.file = file;
            this.values = values;
            this.writer = writer;
            this.i = i;
        }

        @Override
        public void run() {
            try {
                FileInputStream stream = new FileInputStream(file);
                GZIPInputStream zStream = new GZIPInputStream(stream);
                JsonObject o = parser.parse(new InputStreamReader(zStream)).getAsJsonObject();
                int i1 = i.incrementAndGet();
                if (i1 % 1000 == 0) {
                    LOGGER.info("Reached: {}", i1);
                }

                JsonArray sentences = o.getAsJsonArray("sentences");
                for (JsonElement sentence : sentences) {
                    JsonObject sentenceObj = sentence.getAsJsonObject();
                    Integer index = sentenceObj.get("index").getAsInt();
                    String text = sentenceObj.get("text").getAsString();
                    text = text.replaceAll("\\s+", " ");

                    Map<Integer, Double> map = values.get(file.getName());
                    if (!map.containsKey(index)) {
                        return;
                    }

                    synchronized (writer) {
                        writer.append(map.get(index).toString()).append('\t').append(text).append('\n');
                    }
                }

                zStream.close();
                stream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./paisa-collect-simple")
                    .withHeader(
                            "Collect simple sentences from Paisa' corpus")
                    .withOption("i", "input", "Input stats", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("f", "input-folder", "Input folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("t", "threads", "Number of threads", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            Integer threads = cmd.getOptionValue("threads", Integer.class, DEFAULT_THREADS);
            File inputFolder = cmd.getOptionValue("input-folder", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            Map<String, Map<Integer, Double>> values = new HashMap<>();
            parser = new JsonParser();

            LOGGER.info("Reading file");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 5) {
                    continue;
                }

                String[] fParts = parts[0].split("\\|");
                values.putIfAbsent(fParts[0], new HashMap<>());
                Double val = Double.parseDouble(parts[1]);
                Integer sentenceID = Integer.parseInt(fParts[1]);
                values.get(fParts[0]).put(sentenceID, val);
            }
            Integer numFiles = values.size();
            LOGGER.info("Total files: {}", numFiles);
            reader.close();

            LOGGER.info("Running threads ({})", threads);

            AtomicInteger i = new AtomicInteger();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            for (File folder : inputFolder.listFiles()) {
                if (!folder.isDirectory()) {
                    continue;
                }
                for (File file : folder.listFiles()) {
                    if (!file.getName().endsWith(".json.gz")) {
                        continue;
                    }
                    if (!values.containsKey(file.getName())) {
                        continue;
                    }

                    Runnable worker = new WorkerThread(file, values, writer, i);
                    executor.execute(worker);
                }
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            LOGGER.info("Finished all threads");
            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
