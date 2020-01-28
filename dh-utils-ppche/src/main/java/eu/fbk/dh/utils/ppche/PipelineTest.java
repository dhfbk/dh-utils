package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

import java.io.File;
import java.util.Properties;

public class PipelineTest {

    public static void main(String[] args) {
        Properties properties = new Properties();
//        properties.setProperty("annotators", "tokenize, ssplit, pos, stanfordpos, lemma, me_lemma, ner");
        properties.setProperty("annotators", "brackets, tokenize, ssplit, pos, stanfordpos, lemma, custom_lemma, ner, keyphrase");
        properties.setProperty("ner.applyFineGrained", "0");
        properties.setProperty("ner.useSUTime", "0");
        properties.setProperty("ner.applyNumericClassifiers", "0");
//        properties.setProperty("pos.model", "modern_model");
        properties.setProperty("pos.model", "all_model");

        properties.setProperty("customAnnotatorClass.keyphrase", "eu.fbk.dh.kd.annotator.DigiKDAnnotator");
        properties.setProperty("customAnnotatorClass.stanfordpos", "eu.fbk.dh.utils.ppche.StanfordPosAnnotator");
        properties.setProperty("customAnnotatorClass.brackets", "eu.fbk.dh.utils.ppche.BracketReplacer");
        properties.setProperty("customAnnotatorClass.custom_lemma", "eu.fbk.dh.utils.ppche.CustomLemmaAnnotator");

        properties.setProperty("keyphrase.numberOfConcepts", "20");
        properties.setProperty("keyphrase.local_frequency_threshold", "2");
        properties.setProperty("keyphrase.language", "ENGLISH");
        properties.setProperty("keyphrase.pos_class", "eu.fbk.dh.utils.ppche.CustomEnglishAnnotations$StanfordPos");

//        properties.setProperty("customAnnotatorClass.me_lemma", "");

        try {
            String text = Files.toString(new File("/Users/alessio/Downloads/Boyle.txt"), Charsets.UTF_8);

            StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
            Annotation annotation = new Annotation(text);
            pipeline.annotate(annotation);

            Files.write(JSONOutputter.jsonPrint(annotation), new File("/Users/alessio/Downloads/Boyle.json"), Charsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
