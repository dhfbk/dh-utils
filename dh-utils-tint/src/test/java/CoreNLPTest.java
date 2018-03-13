import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by alessio on 07/09/16.
 */

public class CoreNLPTest {

    public static void main(String[] args) {
        String sentenceText = "Barack Obama was the President of the United    States.\nHe was very cool.";
        try {

            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./corenlp-test")
                    .withHeader("CoreNLP test class")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            Properties properties = new Properties();

            properties.setProperty("annotators", "tokenize, ssplit, pos, parse, udfeats");
//            properties.setProperty("tokenize.keepeol", "true");
//            properties.setProperty("tokenize.whitespace", "true");

            StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);

            Annotation annotation = new Annotation(sentenceText);
            pipeline.annotate(annotation);

//            for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
//                System.out.println(token);
//                System.out.println(token.get(CoreAnnotations.CoNLLUFeats.class));
//                System.out.println(token.get(CoreAnnotations.CoarseTagAnnotation.class));
//            }

            System.out.println(JSONOutputter.jsonPrint(annotation));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}