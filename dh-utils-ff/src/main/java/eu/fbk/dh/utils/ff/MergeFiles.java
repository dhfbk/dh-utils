package eu.fbk.dh.utils.ff;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.FrequencyHashSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeFiles {

    public static void main(String[] args) {
        String pairsFile = "/Users/alessio/Desktop/ff/clic/candidates_selected_pairs.tsv";
        String dataFile = "/Users/alessio/Desktop/ff/clic/out-content.txt";

        String resFile = "/Users/alessio/Desktop/ff/clic/test2.vectors/test_nosyn_vec.out";
        String outputFile = "/Users/alessio/Desktop/ff/clic/test2.vectors/res_nosyn.out";

        try {
            List<String> pairs = Files.readLines(new File(pairsFile), Charsets.UTF_8);
            List<String> res = Files.readLines(new File(resFile), Charsets.UTF_8);
            List<String> data = Files.readLines(new File(dataFile), Charsets.UTF_8);

            if (pairs.size() != res.size()) {
                System.out.println("ERROR!");
                System.exit(1);
            }

            Map<String, FrequencyHashSet<Integer>> values = new HashMap<>();

            for (int i = 0; i < pairs.size(); i++) {
                String pair = pairs.get(i);
                String itLemma = pair.split("\t")[0];
                values.putIfAbsent(itLemma, new FrequencyHashSet<>());
                Integer r = Integer.parseInt(res.get(i));
                values.get(itLemma).add(r);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            for (String rowData : data) {
                String[] parts = rowData.split("\t");
                String word = parts[2];
                FrequencyHashSet<Integer> hs = values.get(word);
                int ff = 0;
                int co = 5;
                if (hs != null) {
                    ff = values.get(word).getZero(1);
                    co = values.get(word).getZero(0);
                }
                writer.append(parts[0]).append("\t");
                writer.append(parts[1]).append("\t");
                writer.append(Integer.toString(co)).append("\t");
                writer.append(Integer.toString(ff)).append("\t");
                writer.append(word).append("\n");
            }

            writer.close();


//            System.out.println(values);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
