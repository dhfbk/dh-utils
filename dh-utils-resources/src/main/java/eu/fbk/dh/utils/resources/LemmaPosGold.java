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

public class LemmaPosGold {

    private static final Logger LOGGER = LoggerFactory.getLogger(LemmaPosGold.class);
    private static final Pattern p = Pattern.compile("_");

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("gold-lemma-pos")
                    .withHeader("Tags with lemma-pos gold files")
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
                if (parts.length < 2) {
                    continue;
                }

                String goldToken = parts[1];
                String text = parts[0];

                int tmpOffset = -1;
                Set<Integer> originalOffsets = new HashSet<>();
                while (tmpOffset < text.length()) {
                    int originalOffset = text.indexOf(goldToken, tmpOffset + 1);
                    if (originalOffset == -1) {
                        break;
                    }
                    originalOffsets.add(originalOffset);
                    tmpOffset = originalOffset;
                }

                Matcher m = p.matcher(text);
                Set<Integer> offsets = new HashSet<>();
                while (m.find()) {
                    offsets.add(m.end());
                }

                text = parts[0].replaceAll("_", " ");

//                System.out.println(goldToken);
//                System.out.println(text);
//                System.out.println(originalOffsets);
//                System.out.println();

                Integer tokenNo = null;

                int i = 0;
                StringBuffer buffer = new StringBuffer();
                Annotation annotation = pipeline.runRaw(text);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        i++;
                        Integer offset = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                        if (originalOffsets.contains(offset)) {
                            tokenNo = i;
                        }
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

                if (tokenNo == null) {
                    LOGGER.error("Error in sentence: " + text);
                    continue;
                }

                writer.append(lastText);
                writer.append("\t");
                writer.append(lastParts[tokenNo - 1]);
//                writer.append("\t");
//                writer.append(Integer.toString(tokenNo));
                writer.append("\n");

            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
