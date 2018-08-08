package eu.fbk.dh.utils.migrantech;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

/**
 * Created by alessio on 07/09/16.
 */

public class CoreNLPTokenize {

    public static void main(String[] args) {


        try {

            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./corenlp-test")
                    .withHeader("CoreNLP test class")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            String text = Files.toString(inputFile, Charsets.UTF_8);

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            Annotation annotation = new Annotation(text);
            pipeline.annotate(annotation);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                writer.append(sentence.get(CoreAnnotations.TextAnnotation.class).trim()).append("\n");
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}