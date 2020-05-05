package eu.fbk.dh.utils.tint;

import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

import java.util.Properties;

public class DepecheMoodTest {
    public static void main(String[] args) {
        try {
            Properties properties = new Properties();
            properties.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, readability, mood");
            properties.setProperty("customAnnotatorClass.mood", "eu.fbk.fcw.depechemood.DepecheMoodAnnotator");
            properties.setProperty("mood.language", "it");

            TintPipeline pipeline = new TintPipeline(true);
            pipeline.addProperties(properties);
            pipeline.load();

            String text = "Vuole attenersi alle regole e non lo biasimo anche se mi dispiace saperlo da solo. Anche io dopo tutti questi giorno di isolamento comincio a risentirne ma tant'è";
            Annotation annotation = pipeline.runRaw(text);

            System.out.println(JSONOutputter.jsonPrint(annotation));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
