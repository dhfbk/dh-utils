package eu.fbk.dh.utils.resources;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;


public class PaisaCollectAvgSortedStats {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaisaCollectAvgSortedStats.class);

    private static final Integer level3Up = 200;
    private static final Integer level3Down = 1;
    private static final Integer level4Up = 50;
    private static final Integer level4Down = 1;
    private static final Integer level6Up = 10;

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./paisa-collect-avg-stats")
                    .withHeader(
                            "Collect avg stats from Paisa' corpus")
                    .withOption("i", "input", "Statistics file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            LOGGER.info("Collecting");

            Map<String, Double> avgs = new HashMap<>();
            Map<String, String> originalValues = new HashMap<>();

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 6) {
                    continue;
                }

                StringBuffer buffer = new StringBuffer();
                buffer.append(parts[0]).append("|").append(parts[1]);

                try {
                    Integer l3 = Integer.parseInt(parts[2]);
                    Double l4 = Double.parseDouble(parts[3]);
//                    Integer l5 = Integer.parseInt(parts[4]);
                    Double l6 = Double.parseDouble(parts[5]);

                    if (l3 > level3Up) {
                        continue;
                    }
                    if (l3 < level3Down) {
                        continue;
                    }
                    if (l4 > level4Up) {
                        continue;
                    }
                    if (l4 < level4Down) {
                        continue;
                    }
                    l6 = Math.min(level6Up, l6);

                    double l3norm = 1.0 * l3 / level3Up;
                    double l4norm = 1.0 * l4 / level4Up;
                    double l6norm = 1.0 * l6 / level6Up;

                    double avg = (l3norm + l4norm + l6norm) / 3.0;
                    avgs.put(buffer.toString(), avg);

                    StringBuffer originalValue = new StringBuffer();
                    originalValue.append('\t').append(l3.toString());
                    originalValue.append('\t').append(l4.toString());
//                    originalValue.append('\t').append(l5.toString());
                    originalValue.append('\t').append(l6.toString());
                    originalValues.put(buffer.toString(), originalValue.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            reader.close();

            LOGGER.info("Sorting");
            List<Map.Entry<String, Double>> entries = entriesSortedByValues(avgs);

            LOGGER.info("Writing");
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (Map.Entry<String, Double> entry : entries) {
                writer.append(entry.getKey());
                writer.append('\t');
                writer.append(entry.getValue().toString());
                writer.append(originalValues.get(entry.getKey()));
                writer.append('\n');
            }
            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    static <K, V extends Comparable<? super V>>
    List<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {

        List<Map.Entry<K, V>> sortedEntries = new ArrayList<Map.Entry<K, V>>(map.entrySet());

        Collections.sort(sortedEntries,
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e1.getValue().compareTo(e2.getValue());
                    }
                }
        );

        return sortedEntries;
    }

}
