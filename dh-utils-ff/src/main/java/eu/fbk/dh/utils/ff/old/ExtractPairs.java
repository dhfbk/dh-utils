package eu.fbk.dh.utils.ff.old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class ExtractPairs {
    public static void main(String[] args) {
        String inputFile = "/Users/alessio/Desktop/ff/candidates_selected.tsv";
        String outputFile = "/Users/alessio/Desktop/ff/candidates_selected_pairs.tsv";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                String itLemma = parts[0].split("/")[0];
                for (int i = 1; i < parts.length; i++) {
                    writer.append(itLemma).append("\t").append(parts[i]).append("\n");
                }
            }

            writer.close();
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
