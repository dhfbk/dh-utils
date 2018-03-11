import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

import java.util.Properties;

/**
 * Created by alessio on 07/09/16.
 */

public class CoreNLPTest {

    public static void main(String[] args) {
        String sentenceText = "Barack Obama was the President of the United States.";
        try {

            Properties properties = new Properties();

            properties.setProperty("annotators", "tokenize, ssplit, pos, upos");

            properties.setProperty("customAnnotatorClass.upos", "eu.fbk.dh.tint.upos.UPosAnnotator");
            properties.setProperty("upos.map", "/Users/alessio/Desktop/en-ptb.map.txt");

            StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

            Annotation annotation = new Annotation(sentenceText);
            pipeline.annotate(annotation);

            System.out.println(JSONOutputter.jsonPrint(annotation));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}