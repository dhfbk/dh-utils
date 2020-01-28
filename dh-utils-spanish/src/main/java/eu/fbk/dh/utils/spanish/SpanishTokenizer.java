package eu.fbk.dh.utils.spanish;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

public class SpanishTokenizer {
    public static void main(String[] args) {
        try {
            File inputFile = new File("/Users/alessio/Google Drive/simplification/newsela_article_corpus_2016-01-29/out/artifact.ses.notok");
            File outputFile = new File("/Users/alessio/Google Drive/simplification/newsela_article_corpus_2016-01-29/out/artifact.ses");

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit");
            properties.setProperty("tokenize.language", "es");
            properties.setProperty("ssplit.eolonly", "true");
            StanfordCoreNLP coreNLP = new StanfordCoreNLP(properties);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String text = Files.toString(inputFile, Charsets.UTF_8);
            Annotation annotation = new Annotation(text);
            coreNLP.annotate(annotation);

            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                StringBuffer buffer = new StringBuffer();
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    buffer.append(token.originalText().trim()).append(" ");
                }
                writer.append(buffer.toString().trim()).append("\n");
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
