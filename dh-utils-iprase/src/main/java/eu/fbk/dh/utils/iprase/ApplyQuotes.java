package eu.fbk.dh.utils.iprase;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplyQuotes {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyQuotes.class);

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
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File quotesFolder = cmd.getOptionValue("quotes", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);

//            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("jodd")).setLevel(Level.SEVERE);

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            LOGGER.info("Loading original quotes");
            Map<String, String> originalQuotes = new HashMap<>();
            for (File quotesFile : quotesFolder.listFiles()) {
                if (!quotesFile.getName().endsWith(".txt")) {
                    continue;
                }

                String text = Files.toString(quotesFile, Charsets.UTF_8);
                text = normalize(text);
                originalQuotes.put(quotesFile.getName(), text);
            }

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, quote2");

            pipeline.setProperty("customAnnotatorClass.quote2", "eu.fbk.dh.utils.iprase.utils.QuoteAnnotator");
            pipeline.setProperty("quote2.attributeQuotes", "false");
            pipeline.setProperty("ner.applyFineGrained", "false");

            pipeline.setProperty("verbose", "false");

            LOGGER.info("Loading CoreNLP");
            pipeline.load();

//            System.out.println(originalQuotes.get("2007.txt"));

            LOGGER.info("Loading files");
            for (File file : inputFolder.listFiles()) {
                if (!file.getName().endsWith(".txt")) {
                    continue;
                }

//                if (!file.getName().equals("1506.txt")) {
//                    continue;
//                }

                LOGGER.info("File: {}", file.getAbsolutePath());
                String outputFile = outputFolder.getAbsolutePath() + File.separator + file.getName();

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                String text = Files.toString(file, Charsets.UTF_8);

                Annotation annotation = pipeline.runRaw(text);

                // Get ends of sentences
                List<Integer> sentenceEnds = new ArrayList<>();
                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
//                    System.out.println(sentenceText);
//                    if (sentenceText.endsWith(";") || sentenceText.endsWith(":") || sentenceText.endsWith("...")) {
                    char lastChar = sentenceText.charAt(sentenceText.length() - 1);
                    if (lastChar == ';' || lastChar == ':') {
                        continue;
                    }

                    sentenceEnds.add(sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
                }

                List<CoreMap> quotes = annotation.get(CoreAnnotations.QuotationsAnnotation.class);
                StringBuilder finalText = new StringBuilder();

                int lastEnd = 0;
                if (quotes != null && quotes.size() > 0) {
                    for (CoreMap quote : quotes) {

                        Integer begin = quote.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                        Integer end = quote.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
                        String quoteText = quote.get(CoreAnnotations.TextAnnotation.class);

                        if (begin <= lastEnd) {
                            continue;
                        }

                        finalText.append(text, lastEnd, begin);

                        int nextSentenceEnd = text.length() - 1;
                        if (quoteText.length() > 200) {
                            for (Integer sentenceEnd : sentenceEnds) {
                                if (sentenceEnd > begin) {
                                    nextSentenceEnd = sentenceEnd;
                                    break;
                                }
                            }
                        }

                        lastEnd = Math.min(end, nextSentenceEnd);
//                        System.out.println(String.format("A. %d - %d", lastEnd, begin));

                        boolean removeQuote = false;
                        quoteText = normalize(quoteText);

                        if (quoteText.length() >= 30) {

                            for (String originalQuoteKey : originalQuotes.keySet()) {
                                String originalQuote = originalQuotes.get(originalQuoteKey);

                                int parts = quoteText.length() / 25;
//                                System.out.println("Quote: " + quoteText);
                                for (int i = 0; i < parts; i++) {
                                    int offStart = i * 25;
                                    int offEnd = offStart + 25;
                                    String compare = quoteText.substring(offStart, offEnd);
//                                    System.out.println("Comparing: " + compare);
                                    int index = originalQuote.indexOf(compare);
                                    if (index != -1) {
                                        removeQuote = true;
                                        break;
                                    }
                                }

//                            String start = quoteText.substring(0, 25);
//                            int index = originalQuote.indexOf(start);
//                            if (index != -1) {
//                                removeQuote = true;
//                            }
                            }
                        }

                        if (!removeQuote) {
                            finalText.append(text, begin, lastEnd);
//                            System.out.println(String.format("B. %d - %d", begin, end));
                        }
                    }
                }

                finalText.append(text, lastEnd, text.length());
//                System.out.println(String.format("C. %d - %d", lastEnd, text.length()));

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
