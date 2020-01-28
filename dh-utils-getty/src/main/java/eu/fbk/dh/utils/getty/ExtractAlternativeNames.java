package eu.fbk.dh.utils.getty;

import com.google.common.collect.HashMultimap;
import eu.fbk.utils.core.CommandLine;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ExtractAlternativeNames {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-alternative-names")
                    .withHeader("Extract alternative names from Getty")
                    .withOption("i", "input", "Input file with names", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("a", "input-alt", "Input file with alternative names", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File inputFileAlt = cmd.getOptionValue("input-alt", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedReader reader;
            RDFParser rdfParser;
            StatementCollector collector;

            Map<Resource, String> names = new HashMap<>();
            HashMultimap<Resource, String> altNames = HashMultimap.create();

            System.out.println("Reading names file");
            reader = new BufferedReader(new FileReader(inputFile));
            rdfParser = Rio.createParser(RDFFormat.TURTLE);
            collector = new StatementCollector();
            rdfParser.setRDFHandler(collector);
            rdfParser.parse(reader, "");

            System.out.println("Looping statements");
            for (Statement statement : collector.getStatements()) {
                names.put(statement.getSubject(), statement.getObject().stringValue());
            }

            System.out.println("Reading alternative names file");
            reader = new BufferedReader(new FileReader(inputFileAlt));
            rdfParser = Rio.createParser(RDFFormat.TURTLE);
            collector = new StatementCollector();
            rdfParser.setRDFHandler(collector);
            rdfParser.parse(reader, "");

            System.out.println("Looping statements");
            for (Statement statement : collector.getStatements()) {
                altNames.put(statement.getSubject(), statement.getObject().stringValue());
            }

            System.out.println("Writing file");
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            for (Resource originalUri : names.keySet()) {
                if (!altNames.containsKey(originalUri)) {
                    continue;
                }
                writer.append(names.get(originalUri));
                for (String altName : altNames.get(originalUri)) {
                    writer.append("\t").append(altName);
                }
                writer.append("\n");
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
