import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class AndrewTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AndrewTest.class);

    public static void main(String[] args) {
        TintPipeline pipeline = new TintPipeline();
        pipeline.setProperty("annotators", "ita_toksent, pos");
        Annotation annotation = pipeline.runRaw("Taci, Gliceria, e non turbarmi l'anima con preghiere che non mi è concesso d'esaudire; finché rimarrà in castello, a nessuna di voi più non si conviene lo stare con me.... l'ho detto, e non può essere altrimenti.");
//        Annotation annotation = pipeline.runRaw("Chi l’ha vista? Maria Elena Boschi dal 14 dicembre, giorno del letale confronto tv con Marco Travaglio, è scomparsa dalle ribalte mediatiche, ma anche dalle piazze e dai giornali patinati. Ordine di scuderia, arrivato dal Nazareno, valido almeno fino a che non si sia sciolto il rebus dei collegi. Perché scegliere quello adatto per «Meb» non è cosa facile. E non sarebbe stato facile nemmeno proteggerla (e nasconderla) nel listino proporzionale quando tutti i ministri si misureranno in collegi uninominali. Tanto che in queste ore si è deciso di darle un collegio.");
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println(sentence.get(CoreAnnotations.TextAnnotation.class));
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.print(token.originalText() + " ");
            }
            System.out.println();

        }
//        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
//            System.out.println(token.originalText());
//        }

//        try {
//            JSONOutputter.jsonPrint(annotation, System.out);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
