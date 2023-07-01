package eu.fbk.dh.utils.iprase.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.utils.iprase.ApplyQuotes;
import eu.fbk.dh.utils.iprase.annotations.CatAnnotations;
import eu.fbk.dh.utils.iprase.annotations.NormalizedSentence;
import eu.fbk.utils.core.CommandLine;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class FuzzyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FuzzyTest.class);

    public static void main(String[] args) {
        try {

            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./fuzzy-test")
                    .withHeader("Calculate similarity between two texts.")
                    .withOption("i", "input", "Input file (TXT or DOC/DOCX)", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("c", "comparison", "Comparison files (TXT or DOC/DOCX, separated by |)", "FILE", CommandLine.Type.STRING, true, false, true)
                    .withOption("f", "fuzzyness", "Fuzzyness (between 0 and 100m default 90)", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, false)
                    .withOption("l", "min-length", "Minimum length of the considered text (default 20)", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, false)
                    .withOption("m", "min-length-ratio", "Minimum length of the considered text with respect of the sentence (default 0.7)", "NUM", CommandLine.Type.NON_NEGATIVE_FLOAT, true, false, false)
                    .withOption("M", "max-length-ratio", "Maximum length of the considered text with respect of the sentence (default 1.5)", "NUM", CommandLine.Type.NON_NEGATIVE_FLOAT, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File temaFile = cmd.getOptionValue("input", File.class);
            String tracciaFile = cmd.getOptionValue("comparison", String.class);
            Integer fuzzyness = cmd.getOptionValue("fuzzyness", Integer.class, 90);
            Integer minLength = cmd.getOptionValue("min-length", Integer.class, 20);
            Double minRatio = cmd.getOptionValue("min-length-ratio", Double.class, 0.7);
            Double maxRatio = cmd.getOptionValue("max-length-ratio", Double.class, 1.5);

            String fileContent = getFileContent(temaFile);
            String[] comparisonFiles = tracciaFile.split("\\|");
            StringWriter writer = new StringWriter();
            for (String comparisonFile : comparisonFiles) {
                writer.append(getFileContent(new File(comparisonFile)));
                writer.append("\n");
            }
            String tracciaContent = writer.toString();

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();

            Annotation tracciaAnnotation = pipeline.runRaw(tracciaContent);
            Set<String> tracciaSentences = new HashSet<>();
            for (CoreMap sentence : tracciaAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                if (sentenceText.length() < minLength) {
                    continue;
                }
                tracciaSentences.add(ApplyQuotes.normalize(sentenceText, true));
            }

            int sentencesCount = 0;
            int sentencesTotalCount = 0;
            int charCount = 0;
            int charTotalCount = 0;
            Annotation annotation = pipeline.runRaw(fileContent);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                charTotalCount += sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) -
                        sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                sentencesTotalCount += 1;
                String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                sentenceText = ApplyQuotes.normalize(sentenceText, true);
                if (sentenceText.length() < minLength) {
                    continue;
                }

                for (String tracciaSentence : tracciaSentences) {
                    int metric = FuzzySearch.tokenSetRatio(sentenceText, tracciaSentence);
                    double lengthRatio = (tracciaSentence.length() * 1.0) / (sentenceText.length() * 1.0);
                    if (metric > fuzzyness && lengthRatio > minRatio && lengthRatio < maxRatio) {
                        System.out.println(sentenceText);
                        System.out.println(tracciaSentence);
                        System.out.println(metric);
                        System.out.println(lengthRatio);
                        System.out.println();

                        charCount += sentence.get(CoreAnnotations.CharacterOffsetEndAnnotation.class) -
                                sentence.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                        sentencesCount += 1;
                        break;

//                        System.out.println(FuzzySearch.tokenSortPartialRatio(sentenceText, tracciaSentence));
//                        System.out.println(FuzzySearch.tokenSortRatio(sentenceText, tracciaSentence));
//                        System.out.println(FuzzySearch.tokenSetPartialRatio(sentenceText, tracciaSentence));
//                        System.out.println(FuzzySearch.tokenSetRatio(sentenceText, tracciaSentence));
//                        System.out.println();
                    }

                }

            }

            System.out.println("Total chars count: " + charTotalCount);
            System.out.println("Plagiarism chars count: " + charCount);
            System.out.println("Total sentneces count: " + sentencesTotalCount);
            System.out.println("Plagiarism sentences count: " + sentencesCount);

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }

    private static String getFileContent(File f) throws IOException {
//        BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
        StringWriter writer = new StringWriter();

        try {
            if (f.getName().toLowerCase().endsWith(".doc")) {
                NPOIFSFileSystem fs = new NPOIFSFileSystem(f);
                WordExtractor extractor = new WordExtractor(fs.getRoot());

                for (String rawText : extractor.getParagraphText()) {
                    String text = extractor.stripFields(rawText);
                    writer.append(text.trim()).append("\n");
                }

            } else if (f.getName().toLowerCase().endsWith(".docx")) {
                XWPFDocument doc = new XWPFDocument(new FileInputStream(f));
                XWPFWordExtractor ex = new XWPFWordExtractor(doc);
                String text = ex.getText();
                writer.append(text.trim()).append("\n");
            } else {
                return Files.toString(f, Charsets.UTF_8);
            }
        } catch (Exception e) {
            LOGGER.error(f.getAbsolutePath());
        }

        writer.close();
        return writer.toString();
    }
}
