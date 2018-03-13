package eu.fbk.dh.utils.migrantech;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;

public class ParseSentiment {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParseSentiment.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./parse-sentiment")
                    .withHeader(
                            "Parse sentiment from Migrantech file")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            List<String> lines;
            if (inputFile != null) {
                lines = Files.readLines(inputFile, Charsets.UTF_8);
            } else {
                URL list = Resources.getResource("sentences.txt");
                lines = Resources.readLines(list, Charsets.UTF_8);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
            properties.setProperty("ssplit.isOneSentence", "true");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

            for (String line : lines) {
                line = line.replace('\t', ' ').trim();
                Annotation annotation = new Annotation(line);
                pipeline.annotate(annotation);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
                    writer.append(line).append("\t").append(sentiment).append("\n");
                }
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
