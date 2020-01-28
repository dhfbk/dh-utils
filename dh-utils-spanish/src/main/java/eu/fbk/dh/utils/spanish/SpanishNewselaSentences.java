package eu.fbk.dh.utils.spanish;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

public class SpanishNewselaSentences {
    public static void main(String[] args) {

        try {
            File inputFolder = new File("/Users/alessio/Google Drive/simplification/newsela_article_corpus_2016-01-29/spanish");
            File outputFolder = new File("/Users/alessio/Google Drive/simplification/newsela_article_corpus_2016-01-29/spanish-ss");

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit");
            properties.setProperty("ssplit.newlineIsSentenceBreak", "always");
            properties.setProperty("tokenize.language", "es");
            StanfordCoreNLP coreNLP = new StanfordCoreNLP(properties);

            for (File file : inputFolder.listFiles()) {
                String name = file.getName();
                File outputFile = new File(outputFolder.getAbsolutePath() + File.separator + name);

                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                String text = Files.toString(file, Charsets.UTF_8);
                Annotation annotation = new Annotation(text);
                coreNLP.annotate(annotation);

                for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                    String line = sentence.get(CoreAnnotations.TextAnnotation.class).trim();
                    if (line.length() < 5) {
                        continue;
                    }
                    if (line.startsWith("#")) {
                        continue;
                    }
                    line = line.replaceAll("\n", "");
                    writer.append(line).append("\n").append("\n");
                }

                writer.close();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
