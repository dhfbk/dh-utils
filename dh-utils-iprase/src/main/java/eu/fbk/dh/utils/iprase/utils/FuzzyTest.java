package eu.fbk.dh.utils.iprase.utils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.utils.iprase.ApplyQuotes;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class FuzzyTest {

    public static void main(String[] args) {
        try {

//            File temaFile = new File("/Users/alessio/Google Drive/Temi-Superiori/txt-final/1232.txt");
//            File tracciaFile = new File("/Users/alessio/Google Drive/Temi-Superiori/tracce/2001.txt");
            File temaFile = new File("/Users/alessio/Google Drive/Temi-Superiori/txt-final/2093.txt");
            File tracciaFile = new File("/Users/alessio/Google Drive/Temi-Superiori/tracce/2007.txt");

            String fileContent = Files.toString(temaFile, Charsets.UTF_8);
            String tracciaContent = Files.toString(tracciaFile, Charsets.UTF_8);

//            fileContent = ApplyQuotes.normalize(fileContent, true);
//            tracciaContent = ApplyQuotes.normalize(tracciaContent, true);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent");

            Annotation tracciaAnnotation = pipeline.runRaw(tracciaContent);
            Set<String> tracciaSentences = new HashSet<>();
            for (CoreMap sentence : tracciaAnnotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                if (sentenceText.length() < 20) {
                    continue;
                }
                tracciaSentences.add(ApplyQuotes.normalize(sentenceText, true));
            }

            Annotation annotation = pipeline.runRaw(fileContent);
            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                sentenceText = ApplyQuotes.normalize(sentenceText, true);

                if (sentenceText.length() < 20) {
                    continue;
                }

                for (String tracciaSentence : tracciaSentences) {
                    int metric = FuzzySearch.tokenSetRatio(sentenceText, tracciaSentence);
                    double lengthRatio = (tracciaSentence.length() * 1.0) / (sentenceText.length() * 1.0);
                    if (metric > 90 && lengthRatio > 0.7 && lengthRatio < 1.5) {
                        System.out.println(sentenceText);
                        System.out.println(tracciaSentence);
                        System.out.println(metric);
                        System.out.println(lengthRatio);
                        System.out.println();
                    }

//                    System.out.println(FuzzySearch.tokenSortPartialRatio(sentenceText, tracciaSentence));
//                    System.out.println(FuzzySearch.tokenSortRatio(sentenceText, tracciaSentence));
//                    System.out.println(FuzzySearch.tokenSetPartialRatio(sentenceText, tracciaSentence));
//                    System.out.println(FuzzySearch.tokenSetRatio(sentenceText, tracciaSentence));
//                    System.out.println();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
