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
    private static int FUZZY_LIMIT = 90;
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
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File quotesFolder = cmd.getOptionValue("quotes", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            File yearsFile = cmd.getOptionValue("years", File.class);

            LOGGER.info("Loading years file");
            BufferedReader reader = new BufferedReader(new FileReader(yearsFile));
            String line;
            Map<Integer, String> years = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                try {
                    int id = Integer.parseInt(parts[0]);
                    String year = parts[1].trim();
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

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

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


            LOGGER.info("Loading files");
            for (File file : inputFolder.listFiles()) {
                if (!file.getName().endsWith(".txt")) {
                    continue;
                }

//                if (!file.getName().equals("1506.txt")) {
//                    continue;
//                }

                Integer id = Integer.parseInt(file.getName().replaceAll("[^0-9]", ""));
                String yearFile = years.get(id);
                if (yearFile == null) {
                    LOGGER.warn("Unable to find year for file " + Integer.toString(id));
                }

                LOGGER.info(String.format("File: %s (%s)", file.getAbsolutePath(), yearFile));
                String outputFile = outputFolder.getAbsolutePath() + File.separator + file.getName();

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                String text = Files.toString(file, Charsets.UTF_8);
                StringBuilder finalText = new StringBuilder();

                Annotation annotation = quotePipeline.runRaw(text);
                List<CoreMap> quotes = annotation.get(CoreAnnotations.QuotationsAnnotation.class);
                for (CoreMap quote : quotes) {
                    String quoteText = quote.get(CoreAnnotations.TextAnnotation.class);
                    quoteText = NormalizationAnotator.normalize(quoteText, true);
                    System.out.println(quoteText);
//                    for (CoreLabel token : quote.get(CoreAnnotations.TokensAnnotation.class)) {
//                        System.out.println(token.sentIndex());
//                    }
                }

                // RESTART FROM HERE (above)
                /*
                *
                * Bisogna aggiungere i token da escludere a tokensToRemove (vedi sotto)
                * ovviamente modificando la mappa in modo che includa anche l'ID della sentence
                *
                * */


                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    String originalSentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                    LOGGER.trace("Original sentence: {}", originalSentenceText);
                    NormalizedSentence normalizedSentence = sentence.get(CatAnnotations.NormalizedSentenceAnnotation.class);
                    String sentenceText = normalizedSentence.getNormalizedText();

                    boolean keepSent = true;
                    TreeMap<Integer, Boolean> tokensToRemove = new TreeMap<>();

                    if (sentenceText.length() >= LEN_LIMIT) {
                        for (NormalizedSentence normalizedTracciaSentence : originalQuotes.get(yearFile)) {
                            // normalizedTracciaSentence
                            // normalizedSentence

                            String tracciaSentence = normalizedTracciaSentence.getNormalizedText();
                            int metric = FuzzySearch.tokenSetRatio(sentenceText, tracciaSentence);
                            double lengthRatio = (tracciaSentence.length() * 1.0) / (sentenceText.length() * 1.0);
                            if (metric > FUZZY_LIMIT) {
                                if (lengthRatio > MIN_RATIO) {
                                    keepSent = false;
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
//                                            System.out.println(i);
//                                            System.out.println(partialMetric);
//                                            System.out.println(join);
//                                            System.out.println(tracciaSentence);
//                                            System.out.println();
                                        }
                                    }

                                    LOGGER.trace("Best: {} ", String.join(" ", sentenceParts.subList(maxI, maxI + tracciaParts.size())));
                                    int firstToken = normalizedSentence.getTokenIDs().get(maxI);
                                    int lastToken = normalizedSentence.getTokenIDs().get(maxI + tracciaParts.size() - 1);
                                    for (int i = firstToken; i <= lastToken; i++) {
                                        tokensToRemove.put(i, false);
                                    }
                                    tokensToRemove.put(firstToken - 1, true);
                                    tokensToRemove.put(lastToken + 1, true);
//                                    System.out.println(best);
//                                    System.out.println(maxValue);
//                                    System.out.println(firstToken);
//                                    System.out.println(lastToken);
                                }
                            }
                        }

                        LOGGER.trace("Original sentence normalized: {}", sentenceText);
                        LOGGER.trace("Tokens to remove: {}", tokensToRemove.toString());
                    }

                    if (tokensToRemove.size() > 0) {
                        String newText = originalSentenceText;
                        for (Integer tokenID : tokensToRemove.descendingKeySet()) {
                            CoreLabel token;
                            try {
                                token = sentence.get(CoreAnnotations.TokensAnnotation.class).get(tokenID - 1);
                            } catch (ArrayIndexOutOfBoundsException e) {
                                continue;
                            }
                            Boolean checkQuote = tokensToRemove.get(tokenID);
                            if (checkQuote) {
                                String tokenText = token.originalText();
                                if (!quoteSymbols.contains(tokenText)) {
                                    continue;
                                }
                            }

                            Integer sentenceOffset = sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                            Integer begin = token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) - sentenceOffset;
                            Integer end = token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) - sentenceOffset;

                            String before = newText.substring(0, begin);
                            String after = "";
                            try {
                                after = newText.substring(end);
                            } catch (StringIndexOutOfBoundsException e) {
                                // nothing
                            }
                            newText = before + after;
                        }
                        newText = newText.replaceAll("\\s+", " ");
                        LOGGER.trace("Resulting sentence: {}", newText);
                        finalText.append(newText).append("\n");
                    } else if (keepSent) {
                        finalText.append(originalSentenceText.trim()).append("\n");
                    }

                }

//                System.out.println(JSONOutputter.jsonPrint(annotation));

//                Annotation annotation = pipeline.runRaw(text);
//
//                // Get ends of sentences
//                List<Integer> sentenceEnds = new ArrayList<>();
//                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
//                    String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
////                    System.out.println(sentenceText);
////                    if (sentenceText.endsWith(";") || sentenceText.endsWith(":") || sentenceText.endsWith("...")) {
//                    char lastChar = sentenceText.charAt(sentenceText.length() - 1);
//                    if (lastChar == ';' || lastChar == ':') {
//                        continue;
//                    }
//
//                    sentenceEnds.add(sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
//                }
//
//                List<CoreMap> quotes = annotation.get(CoreAnnotations.QuotationsAnnotation.class);
//                StringBuilder finalText = new StringBuilder();
//
//                int lastEnd = 0;
//                if (quotes != null && quotes.size() > 0) {
//                    for (CoreMap quote : quotes) {
//
//                        Integer begin = quote.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
//                        Integer end = quote.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
//                        String quoteText = quote.get(CoreAnnotations.TextAnnotation.class);
//
//                        if (begin <= lastEnd) {
//                            continue;
//                        }
//
//                        finalText.append(text, lastEnd, begin);
//
//                        int nextSentenceEnd = text.length() - 1;
//                        if (quoteText.length() > 200) {
//                            for (Integer sentenceEnd : sentenceEnds) {
//                                if (sentenceEnd > begin) {
//                                    nextSentenceEnd = sentenceEnd;
//                                    break;
//                                }
//                            }
//                        }
//
//                        lastEnd = Math.min(end, nextSentenceEnd);
////                        System.out.println(String.format("A. %d - %d", lastEnd, begin));
//
//                        boolean removeQuote = false;
//                        quoteText = normalize(quoteText);
//
//                        if (quoteText.length() >= 30) {
//
//                            for (String originalQuoteKey : originalQuotes.keySet()) {
//                                String originalQuote = originalQuotes.get(originalQuoteKey);
//
//                                int parts = quoteText.length() / 25;
////                                System.out.println("Quote: " + quoteText);
//                                for (int i = 0; i < parts; i++) {
//                                    int offStart = i * 25;
//                                    int offEnd = offStart + 25;
//                                    String compare = quoteText.substring(offStart, offEnd);
////                                    System.out.println("Comparing: " + compare);
//                                    int index = originalQuote.indexOf(compare);
//                                    if (index != -1) {
//                                        removeQuote = true;
//                                        break;
//                                    }
//                                }
//
////                            String start = quoteText.substring(0, 25);
////                            int index = originalQuote.indexOf(start);
////                            if (index != -1) {
////                                removeQuote = true;
////                            }
//                            }
//                        }
//
//                        if (!removeQuote) {
//                            finalText.append(text, begin, lastEnd);
////                            System.out.println(String.format("B. %d - %d", begin, end));
//                        }
//                    }
//                }
//
//                finalText.append(text, lastEnd, text.length());
////                System.out.println(String.format("C. %d - %d", lastEnd, text.length()));

                writer.append(finalText.toString());
                writer.close();
            }

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
