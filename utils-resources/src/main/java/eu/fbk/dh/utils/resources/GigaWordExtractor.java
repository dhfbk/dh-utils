package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by alessio on 07/03/17.
 */

public class GigaWordExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GigaWordExtractor.class);
    private static final Integer DEFAULT_BLOCK_SIZE = 100;

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("gigaword-extractor")
                    .withHeader("Extract GigaWord corpus and save it tokenized one sentence per line")
                    .withOption("i", "input-path", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("o", "output-path", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("b", "block-size", String.format("Block size (default %d)", DEFAULT_BLOCK_SIZE), "NUM", CommandLine.Type.INTEGER,
                            true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input-path", File.class);
            File outputFile = cmd.getOptionValue("output-path", File.class);
            Integer blockSize = cmd.getOptionValue("block-size", Integer.class, DEFAULT_BLOCK_SIZE);

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit");
            LOGGER.info("Starting CoreNLP");
            StanfordCoreNLP pipeline = new StanfordCoreNLP();
            LOGGER.info("Stanford initialized");

            LOGGER.info("Starting reading");
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            LOGGER.info("Starting writing");
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            int i = 0;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                buffer.append(line).append("\n");
                if (i >= blockSize) {
                    LOGGER.info(Integer.toString(i));

                    Annotation annotation = new Annotation(buffer.toString());
                    pipeline.annotate(annotation);

                    StringBuffer sentenceBuffer = new StringBuffer();
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                            sentenceBuffer.append(token.originalText()).append(" ");
                        }
                        writer.append(sentenceBuffer.toString().trim()).append("\n");
                        writer.flush();
                    }

                    buffer = new StringBuffer();
                    i = 0;
                }
                i++;
            }

            LOGGER.info("Closing stuff");

            reader.close();
            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
