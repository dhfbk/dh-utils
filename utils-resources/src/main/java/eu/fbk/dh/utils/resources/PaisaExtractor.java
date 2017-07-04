package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by alessio on 08/08/16.
 */

public class PaisaExtractor {

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("paisa-parser")
                    .withHeader("Convert PAISA corpus to txt")
                    .withOption("i", "input-path", "PAISA corpus", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("o", "output-path", "output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File corpusFile = cmd.getOptionValue("i", File.class);
            File outputFile = cmd.getOptionValue("o", File.class);
            BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            Properties props = new Properties();
            props.setProperty("annotators", "ita_toksent");
            props.setProperty("customAnnotatorClass.ita_toksent",
                    "eu.fbk.dh.tint.tokenizer.annotators.ItalianTokenizerAnnotator");
            StanfordCoreNLP tmpPipeline = new StanfordCoreNLP(props);

            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("<text")) {
                    continue;
                }

                if (line.startsWith("postato da")) {
                    continue;
                }

                if (line.startsWith("categoria:")) {
                    continue;
                }

                if (line.startsWith("</text")) {

                    String text = buffer.toString();

                    Annotation document = new Annotation(text);
//                    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                    tmpPipeline.annotate(document);
//                    System.out.println(pipeline.timingInformation());

                    for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                        StringBuffer sentenceBuffer = new StringBuffer();
                        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                            sentenceBuffer.append(token.originalText());
                            sentenceBuffer.append(" ");
                        }
                        writer.append(sentenceBuffer.toString().trim());
                        writer.append("\n");
                    }
                    writer.append("\n");
//                    writer.flush();

                    buffer = new StringBuffer();
                    continue;
                }

                buffer.append(line).append("\n");
            }

            writer.close();
            reader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
