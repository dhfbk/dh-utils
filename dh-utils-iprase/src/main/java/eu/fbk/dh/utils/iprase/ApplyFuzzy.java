package eu.fbk.dh.utils.iprase;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.utils.iprase.annotations.CatAnnotations;
import eu.fbk.dh.utils.iprase.annotations.NormalizedSentence;
import eu.fbk.dh.utils.iprase.utils.NormalizationAnotator;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class ApplyFuzzy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyFuzzy.class);
    private static int LEN_LIMIT = 20;
    private static int FUZZY_LIMIT = 80;
    private static double MIN_RATIO = 0.9;
//    private static double MAX_RATIO = 1.5;

    private static Set<String> quoteSymbols = new HashSet<>();

    static {
        quoteSymbols.add("\"");
    }

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./apply-quotes")
                    .withHeader(
                            "Apply quote annotator to texts")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("q", "quotes", "Quotes folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withOption("y", "years", "Years file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("s", "statistics", "Output file with statistics", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File quotesFolder = cmd.getOptionValue("quotes", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            File statisticsFile = cmd.getOptionValue("statistics", File.class);
            File yearsFile = cmd.getOptionValue("years", File.class);

            LOGGER.info("Loading years file");
            BufferedReader reader = new BufferedReader(new FileReader(yearsFile));
            String line;
            Map<Integer, String> years = new HashMap<>();
            Map<Integer, String> schools = new HashMap<>();
            Map<Integer, String> schoolNames = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0]);
                    String year = parts[1].trim();
                    String school = parts[4].trim();
                    String schoolName = parts[3].trim();
                    schools.put(id, school);
                    schoolNames.put(id, schoolName);
                    String[] yParts = year.split("-");
                    if (yParts.length < 2) {
                        continue;
                    }

                    String yString = "20" + yParts[1] + ".txt";
                    years.put(id, yString);
                } catch (Exception e) {
                    LOGGER.warn("Unable to parse id from string " + parts[0]);
                    continue;
                }
            }
            reader.close();

//            if (!outputFolder.exists()) {
//                outputFolder.mkdirs();
//            }

            TintPipeline quotePipeline = new TintPipeline();
            quotePipeline.loadDefaultProperties();
            quotePipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, quote2, normalized_text");
            quotePipeline.setProperty("customAnnotatorClass.quote2", "eu.fbk.dh.utils.iprase.utils.QuoteAnnotator");
            quotePipeline.setProperty("customAnnotatorClass.normalized_text", "eu.fbk.dh.utils.iprase.utils.NormalizationAnotator");
            quotePipeline.setProperty("quote2.attributeQuotes", "false");
            quotePipeline.setProperty("ner.applyFineGrained", "false");
            quotePipeline.setProperty("verbose", "false");
            LOGGER.info("Loading quote pipeline");
            quotePipeline.load();

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, normalized_text, pos, ita_morpho, ita_lemma");
            pipeline.setProperty("ita_toksent.newlineIsSentenceBreak", "false");
            pipeline.setProperty("customAnnotatorClass.normalized_text", "eu.fbk.dh.utils.iprase.utils.NormalizationAnotator");
            LOGGER.info("Loading original texts pipeline");
            pipeline.load();

            LOGGER.info("Loading original quotes");
            Map<String, Set<NormalizedSentence>> originalQuotes = new HashMap<>();
            for (File quotesFile : quotesFolder.listFiles()) {
                if (!quotesFile.getName().endsWith(".txt")) {
                    continue;
                }

                String text = Files.toString(quotesFile, Charsets.UTF_8);
                Annotation tracciaAnnotation = pipeline.runRaw(text);
                Set<NormalizedSentence> tracciaSentences = new HashSet<>();
                for (CoreMap sentence : tracciaAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                    if (sentenceText.length() < LEN_LIMIT) {
                        continue;
                    }
                    tracciaSentences.add(sentence.get(CatAnnotations.NormalizedSentenceAnnotation.class));
                }

                originalQuotes.put(quotesFile.getName(), tracciaSentences);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(statisticsFile));

            LOGGER.info("Loading files");
            for (File file : inputFolder.listFiles()) {
                if (!file.getName().endsWith(".txt")) {
                    continue;
                }

//                if (!file.getName().equals("1313.txt")) {
//                    continue;
//                }

                Integer id = Integer.parseInt(file.getName().replaceAll("[^0-9]", ""));
                String yearFile = years.get(id);
                if (yearFile == null) {
                    LOGGER.warn("Unable to find year for file " + Integer.toString(id));
//                    continue;
                }

                writer.append(file.getName()).append("\t");

                Set<String> quoteTokens = new HashSet<>();
                Set<String> citationTokens = new HashSet<>();

                writer.append(yearFile).append("\t");
                writer.append(schools.get(id)).append("\t");
                writer.append(schoolNames.get(id)).append("\t");

                LOGGER.info(String.format("File: %s (%s)", file.getAbsolutePath(), yearFile));
                String outputFile = outputFolder.getAbsolutePath() + File.separator + file.getName();
                BufferedWriter thisFileWriter = new BufferedWriter(new FileWriter(outputFile));

                String text = Files.toString(file, Charsets.UTF_8);

                Annotation annotation = quotePipeline.runRaw(text);

                writer.append(Integer.toString(annotation.get(CoreAnnotations.TokensAnnotation.class).size())).append("\t");
                List<CoreMap> quotes = annotation.get(CoreAnnotations.QuotationsAnnotation.class);
                for (CoreMap quote : quotes) {
                    for (CoreLabel token : quote.get(CoreAnnotations.TokensAnnotation.class)) {
                        String tokenID = token.get(CoreAnnotations.SentenceIndexAnnotation.class) + "_" + token.index();
                        quoteTokens.add(tokenID);
                    }

//                    String quoteText = quote.get(CoreAnnotations.TextAnnotation.class);
//                    quoteText = NormalizationAnotator.normalize(quoteText, true);
//                    System.out.println(quoteText);
//                    System.out.println();
//                    for (CoreLabel token : quote.get(CoreAnnotations.TokensAnnotation.class)) {
//                        System.out.println(token.sentIndex());
//                    }
                }

                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    String originalSentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                    LOGGER.trace("Original sentence: {}", originalSentenceText);
                    NormalizedSentence normalizedSentence = sentence.get(CatAnnotations.NormalizedSentenceAnnotation.class);
                    String sentenceText = normalizedSentence.getNormalizedText();

//                    boolean keepSent = true;
//                    TreeMap<Integer, Boolean> tokensToRemove = new TreeMap<>();

                    if (sentenceText.length() >= LEN_LIMIT && yearFile != null) {
                        for (NormalizedSentence normalizedTracciaSentence : originalQuotes.get(yearFile)) {
                            // normalizedTracciaSentence
                            // normalizedSentence

                            String tracciaSentence = normalizedTracciaSentence.getNormalizedText();
                            int metric = FuzzySearch.tokenSetRatio(sentenceText, tracciaSentence);
//                            if (tracciaSentence.contains("per rendere giusta una")) {
//                                System.out.println(tracciaSentence);
//                                System.out.println(sentenceText);
//                                System.out.println(metric);
//                            }
                            double lengthRatio = (tracciaSentence.length() * 1.0) / (sentenceText.length() * 1.0);
                            if (metric > FUZZY_LIMIT) {
                                if (lengthRatio > MIN_RATIO) {
                                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                                        String tokenID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class) + "_" + token.index();
                                        citationTokens.add(tokenID);
                                    }
                                } else {

//                                    System.out.println("---");
//                                    System.out.println(metric);
//                                    System.out.println(lengthRatio);
//                                    System.out.println(sentenceText);
//                                    System.out.println(tracciaSentence);

                                    List<String> tracciaParts = normalizedTracciaSentence.getTokens();
                                    List<String> sentenceParts = normalizedSentence.getTokens();

                                    int diff = sentenceParts.size() - tracciaParts.size();
                                    int maxI = -1;
                                    int maxValue = 0;
//                                    System.out.println("Diff: " + diff);
                                    if (diff > 0) {
                                        for (int i = 0; i <= diff; i++) {
                                            List<String> copy = sentenceParts.subList(i, i + tracciaParts.size());
                                            String join = String.join(" ", copy);
                                            int partialMetric = FuzzySearch.tokenSetRatio(join, tracciaSentence);
                                            if (partialMetric > maxValue) {
                                                maxValue = partialMetric;
                                                maxI = i;
                                            }
                                        }
                                    }
                                    else {
                                        maxI = 0;
                                    }

                                    LOGGER.trace("Best: {} ", String.join(" ", sentenceParts.subList(maxI, maxI + tracciaParts.size())));
                                    int firstToken = normalizedSentence.getTokenIDs().get(maxI);
                                    int lastToken = normalizedSentence.getTokenIDs().get(maxI + tracciaParts.size() - 1);

//                                    System.out.println(String.join(" ", sentenceParts.subList(maxI, maxI + tracciaParts.size())));
                                    for (int i = firstToken; i <= lastToken; i++) {
                                        String tokenID = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class) + "_" + i;
                                        citationTokens.add(tokenID);
//                                        System.out.print(tokenID + " ");
//                                        tokensToRemove.put(i, false);
                                    }
//                                    System.out.println();
//                                    tokensToRemove.put(firstToken - 1, true);
//                                    tokensToRemove.put(lastToken + 1, true);

                                }
                            }
                        }

                        LOGGER.trace("Original sentence normalized: {}", sentenceText);
//                        LOGGER.trace("Tokens to remove: {}", tokensToRemove.toString());
                    }

//                    if (tokensToRemove.size() > 0) {
//                        String newText = originalSentenceText;
//                        for (Integer tokenID : tokensToRemove.descendingKeySet()) {
//                            CoreLabel token;
//                            try {
//                                token = sentence.get(CoreAnnotations.TokensAnnotation.class).get(tokenID - 1);
//                            } catch (ArrayIndexOutOfBoundsException e) {
//                                continue;
//                            }
//                            Boolean checkQuote = tokensToRemove.get(tokenID);
//                            if (checkQuote) {
//                                String tokenText = token.originalText();
//                                if (!quoteSymbols.contains(tokenText)) {
//                                    continue;
//                                }
//                            }
//
//                            Integer sentenceOffset = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                            Integer begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentenceOffset;
//                            Integer end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) - sentenceOffset;
//
//                            String before = newText.substring(0, begin);
//                            String after = "";
//                            try {
//                                after = newText.substring(end);
//                            } catch (StringIndexOutOfBoundsException e) {
//                                // nothing
//                            }
//                            newText = before + after;
//                        }
//                        newText = newText.replaceAll("\\s+", " ");
//                        LOGGER.trace("Resulting sentence: {}", newText);
////                        finalText.append(newText).append("\n");
//                    } else if (keepSent) {
////                        finalText.append(originalSentenceText.trim()).append("\n");
//                    }
                    for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                        String tokenID = token.get(CoreAnnotations.SentenceIndexAnnotation.class) + "_" + token.index();
                        thisFileWriter.append(token.originalText().replace("\t", " "));
                        thisFileWriter.append("\t");
                        thisFileWriter.append(tokenID);
                        thisFileWriter.append("\t");
                        thisFileWriter.append(quoteTokens.contains(tokenID) ? "1" : "0");
                        thisFileWriter.append("\t");
                        thisFileWriter.append(citationTokens.contains(tokenID) ? "1" : "0");
                        thisFileWriter.append("\n");
                    }

                }
//                writer.append(finalText.toString());
//                writer.close();

                writer.append(Integer.toString(quoteTokens.size())).append("\t");
                writer.append(Integer.toString(citationTokens.size())).append("\t");
                citationTokens.retainAll(quoteTokens);
                writer.append(Integer.toString(citationTokens.size())).append("\n");

                thisFileWriter.close();
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    public static String normalize(String text) {
        return normalize(text, false);
    }

    public static String normalize(String text, boolean preserveSpaces) {
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        text = text.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        text = text.toLowerCase();
        if (preserveSpaces) {
            text = text.replaceAll("[^a-z]", " ");
            text = text.replaceAll("\\s+", " ");
        } else {
            text = text.replaceAll("[^a-z]", "");
        }
        return text;
    }
}
