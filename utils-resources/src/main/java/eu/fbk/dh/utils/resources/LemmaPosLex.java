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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 17/01/17.
 */

public class LemmaPosLex {

    private static final Logger LOGGER = LoggerFactory.getLogger(LemmaPosLex.class);
    private static final Pattern p = Pattern.compile("_");

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("lex-lemma-pos")
                    .withHeader("Tags with lemma-pos Lexenstein eval files")
                    .withOption("i", "input-path", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("o", "output-path", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("i", File.class);
            File outputFile = cmd.getOptionValue("o", File.class);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma");
            pipeline.setProperty("ita_toksent.tokenizeOnlyOnSpace", "true");
            pipeline.setProperty("ita_toksent.ssplitOnlyOnNewLine", "true");
            pipeline.load();

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }

                Integer tokenNo = Integer.parseInt(parts[2]);

                Matcher m = p.matcher(parts[0]);
                Set<Integer> offsets = new HashSet<>();

                while (m.find()) {
                    offsets.add(m.end());
                }

                String text = parts[0].replaceAll("_", " ");

                StringBuffer buffer = new StringBuffer();
                Annotation annotation = pipeline.runRaw(text);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        Integer offset = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                        char c = ' ';
                        if (offsets.contains(offset)) {
                            c = '_';
                        }
                        String lemma = token.lemma();
                        if (lemma.equals("[PUNCT]")) {
                            lemma = token.originalText();
                        }
                        lemma = lemma.replaceAll("\\s+", "_");
                        buffer
                                .append(c)
                                .append(lemma)
                                .append("_")
                                .append(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                    }
                }

                String lastText = buffer.toString().trim();
                String[] lastParts = lastText.split(" ");

                writer.append(lastText);
                writer.append("\t");
                writer.append(lastParts[tokenNo - 1]);
                writer.append("\t");
                writer.append(Integer.toString(tokenNo));
                writer.append("\n");

            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
