package eu.fbk.dh.utils.creep;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class EnglishParse {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./english-parse")
                    .withHeader("Parse English texts and add lemmas")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit, pos, lemma");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }
                String text = parts[0];
                Annotation annotation = new Annotation(text);
                pipeline.annotate(annotation);

                StringBuffer buffer = new StringBuffer();
                for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
                    buffer.append(token.lemma()).append(" ");
                }

                writer.append(buffer.toString().trim()).append("\t").append(parts[1]).append("\t").append(parts[2]).append("\n");
            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
