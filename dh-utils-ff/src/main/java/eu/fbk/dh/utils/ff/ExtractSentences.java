package eu.fbk.dh.utils.ff;

import com.google.common.base.Charsets;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class ExtractSentences {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-sentences")
                    .withHeader("Extracts sentences with list of lemmas")
                    .withOption("i", "input", "Input file with sentences", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("l", "lemmas", "List of lemmas", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File lemmaFile = cmd.getOptionValue("lemmas", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("ita_toksent.ssplitOnlyOnNewLine", "1");
            pipeline.setProperty("ita_toksent.tokenizeOnlyOnSpace", "1");
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, readability");
            pipeline.load();

            Map<String, Set<String>> lemmas = new HashMap();
            List<String> lemmasFromFile = Files.readAllLines(lemmaFile.toPath(), Charsets.UTF_8);
            for (String lemma : lemmasFromFile) {
                lemma = lemma.trim();
                if (lemma.length() == 0) {
                    continue;
                }
                String[] parts = lemma.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }
                lemmas.putIfAbsent(parts[0].toLowerCase(), new HashSet<>());
                lemmas.get(parts[0].toLowerCase()).add(parts[1]);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                line += " .";

                Map<Integer, Set<String>> offsets = new TreeMap<>();
                Map<Integer, Integer> difficulties = new TreeMap<>();
                Set<String> langs = new HashSet<>();
                Map<Integer, String> words = new HashMap<>();

                Annotation annotation = pipeline.runRaw(line);
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                        if (!pos.equals("S") && !pos.equals("V") &&
                                !pos.equals("A") && !pos.equals("B")) {
                            continue;
                        }

                        String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                        Integer difficulty = token.get(ReadabilityAnnotations.DifficultyLevelAnnotation.class);
                        if (difficulty == null) {
                            difficulty = 4;
                        }
                        lemma = lemma.toLowerCase();

                        Integer offset = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                        words.put(offset, lemma);
                        offsets.put(offset, new HashSet<>());
                        difficulties.putIfAbsent(offset, difficulty);

                        if (lemmas.containsKey(lemma)) {
                            for (String lang : lemmas.get(lemma)) {
                                offsets.get(offset).add(lang);
                                langs.add(lang);
                            }
                        } else {
                            String lang = "xx";
                            offsets.putIfAbsent(offset, new HashSet<>());
                            offsets.get(offset).add(lang);
                            langs.add(lang);
                        }
                    }

//                    System.out.println(sentence.get(CoreAnnotations.TextAnnotation.class));
                    break;
                }

                writer.append(Integer.toString(langs.size()));
                writer.append("\t");
                writer.append(line);
                for (Integer offset : offsets.keySet()) {
                    for (String lang : offsets.get(offset)) {
                        writer.append("\t").append(offset.toString());
                        writer.append("\t").append(difficulties.get(offset).toString());
                        writer.append("\t").append(lang);
                        writer.append("\t").append(words.get(offset));
                    }
                }
                writer.append("\n");

//                break;
            }
            reader.close();

            writer.close();

//            System.out.println(lemmas);
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
