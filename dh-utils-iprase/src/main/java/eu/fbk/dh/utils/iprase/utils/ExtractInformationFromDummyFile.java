package eu.fbk.dh.utils.iprase.utils;

import eu.fbk.utils.core.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ExtractInformationFromDummyFile {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractInformationFromDummyFile.class);
    private static final Integer SCHOOL_TYPE = 1;
    private static final Integer YEAR = 2;

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./eifdf")
                    .withHeader(
                            "Extract IDs from dummy file from IPRASE")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            Iterable<CSVRecord> records = CSVFormat.newFormat(',').parse(reader);

            Map<Integer, String> schools = new HashMap<>();
            Map<Integer, String> info = new HashMap<>();
            String year = "";
            String schoolType = "";

            boolean firstRow = true;
            for (CSVRecord record : records) {
                if (firstRow) {
                    for (int i = 0; i < record.size(); i++) {
                        String school = record.get(i);
                        school = school.trim().replaceAll("\\s+", " ");
                        if (school.length() > 0) {
                            schools.put(i, school);
                        }
                    }
                    firstRow = false;
                    continue;
                }

                String yearTemp = record.get(YEAR).trim().replaceAll("\\s+", " ");
                if (yearTemp.length() > 0) {
                    year = yearTemp;
                }
                String schoolTypeTemp = record.get(SCHOOL_TYPE).trim().replaceAll("\\s+", " ");
                if (schoolTypeTemp.length() > 0) {
                    schoolType = schoolTypeTemp;
                }

                for (Integer key : schools.keySet()) {
                    String value = record.get(key);
                    String schoolName = schools.get(key);

                    String[] parts = value.split("\\s+");
                    for (String id : parts) {
                        try {
                            int idInt = Integer.parseInt(id);
                            if (info.containsKey(idInt)) {
                                System.out.println("WARNING: id already taken -> " + id);
                            }
                            info.put(idInt, year + "\t" + schoolType + "\t" + schoolName);
                        }
                        catch (Exception e) {
                            // ignored
                        }
                    }

                }
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (int i = 1; i <= Collections.max(info.keySet()); i++) {
                String value = info.get(i);
                if (value == null) {
                    value = "";
                }
                writer.append(Integer.toString(i)).append('\t').append(value).append('\n');
            }
            writer.close();
            reader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

}
