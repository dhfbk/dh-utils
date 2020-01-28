package eu.fbk.dh.utils.tint;

import eu.fbk.dh.tint.digimorph.DigiMorph;
import eu.fbk.utils.core.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SilviaLuTag {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./lu-tag")
                    .withHeader("Extract pos for lemmas")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            Set<String> okPos = new HashSet<>();
            okPos.add("a");
            okPos.add("n");
            okPos.add("v");

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';').withRecordSeparator('\n'));

            DigiMorph digiMorph = new DigiMorph();

            Reader in = new FileReader(inputFile);
            Iterable<CSVRecord> records = CSVFormat.newFormat(';').parse(in);
            for (CSVRecord record : records) {
                String lemma = record.get(1).trim().toLowerCase();
                if (lemma.length() == 0) {
                    continue;
                }

                Set<String> pos = new HashSet<>();

                List<String> morphology = digiMorph.getMorphology(lemma);
                String[] parts = morphology.get(0).split("\\s+");
                for (int i = 1; i < parts.length; i++) {
                    String part = parts[i];
                    if (part.contains("v+part+")) {
                        pos.add("a");
                        continue;
                    }

                    String[] mParts = part.split("\\+");
                    String pLemma = mParts[0];
                    String pPos = mParts[1];
                    if (pPos.equals("adj")) {
                        pPos = "a";
                    }
                    if (lemma.equals(pLemma) && okPos.contains(pPos)) {
                        pos.add(pPos);
                    }
                }

                csvPrinter.printRecord(record.get(0), record.get(1), record.get(2), pos.toString().replaceAll("[\\[\\] ]", ""));

//                System.out.println(morphology);
//                System.out.println(record.get(0));
//                System.out.println(record.get(1));
//                System.out.println(record.get(2));
//                System.out.println(pos);
//                System.out.println();

            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
