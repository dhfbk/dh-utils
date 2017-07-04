package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by alessio on 17/01/17.
 */

public class LemmaPos {

    private static final Logger LOGGER = LoggerFactory.getLogger(LemmaPos.class);
    private static final int DEFAULT_LINES = 1000;

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("camera-parser")
                    .withHeader("Convert Camera corpus to txt")
                    .withOption("i", "input-path", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("o", "output-path", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("l", "lines", "Lines (default " + DEFAULT_LINES + ")", "NUM", CommandLine.Type.INTEGER,
                            true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File corpusFile = cmd.getOptionValue("i", File.class);
            File outputFile = cmd.getOptionValue("o", File.class);
            int linesNum = cmd.getOptionValue("l", Integer.class, DEFAULT_LINES);
//            corpusFile = new File("/Users/alessio/Desktop/small.txt.tokens");
//            outputFile = new File("/Users/alessio/Desktop/output.txt");

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma");
            pipeline.setProperty("ita_toksent.tokenizeOnlyOnSpace", "true");
            pipeline.setProperty("ita_toksent.ssplitOnlyOnNewLine", "true");
            pipeline.load();

            BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            StringBuffer buffer = new StringBuffer();
            int i = 0;

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                buffer.append(line.trim()).append("\n");
                if (++i > linesNum) {
                    parseText(buffer, pipeline, writer, linesNum);
                    buffer = new StringBuffer();
                    i = 0;
                }
            }
            parseText(buffer, pipeline, writer, linesNum);

            reader.close();
            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    private static void parseText(StringBuffer buffer, TintPipeline pipeline, BufferedWriter writer, int linesNum)
            throws IOException {
        String text = buffer.toString();
        Annotation annotation = pipeline.runRaw(text);
        LOGGER.info("Parsing " + linesNum + " lines...");
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String lemma = token.lemma();
                if (lemma.equals("[PUNCT]")) {
                    lemma = token.originalText();
                }
                lemma = lemma.replaceAll("\\s+", "_");
                writer
                        .append(lemma)
                        .append("_")
                        .append(token.get(CoreAnnotations.PartOfSpeechAnnotation.class))
                        .append(" ");
            }
            writer.append("\n");
        }
    }
}
