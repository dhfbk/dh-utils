package eu.fbk.dh.utils.tint;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class LatinTest {
    public static void main(String[] args) {

        // ssh -L 12345:localhost:9043 -L 12346:localhost:9006 dhuser@dh-server.fbk.eu

        Properties properties = new Properties();
        properties.setProperty("annotators", "ita_toksent, udpipe, entities");
        properties.setProperty("customAnnotatorClass.udpipe", "eu.fbk.fcw.udpipe.api.UDPipeAnnotator");
        properties.setProperty("customAnnotatorClass.herodotos", "eu.fbk.fcw.herodotos.HerodotosAnnotator");
        properties.setProperty("customAnnotatorClass.ita_toksent", "eu.fbk.dh.tint.tokenizer.annotators.ItalianTokenizerAnnotator");
        properties.setProperty("udpipe.server", "localhost");
        properties.setProperty("udpipe.port", "12345");
        properties.setProperty("udpipe.keepOriginal", "1");
        properties.setProperty("udpipe.alreadyTokenized", "1");
        properties.setProperty("herodotos.server", "localhost");
        properties.setProperty("herodotos.port", "12346");

        properties.setProperty("customAnnotatorClass.entities", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("entities.id", "1");
        properties.setProperty("entities.name", "ENTITIES");
        properties.setProperty("entities.class", "eu.fbk.dh.utils.tint.CustomLocuzioniAnnotator");

        String text = "Baptistam Adrianum Adolfus ad sui temporis historias conscribendas amplissimis oblatis praemiis persuasit.\n" +
                "Virtutes, atque opera tam Magni Parentis imitatus est Franciscus, qui in Imperio successit, et antiquitatis studio maxime delectatus, praeclaras, atque innumeas venerandae vetustatis reliquias, lapides, gemmas, numismata collegit.";
//        text = "Baptistam Adrianum Adolfus";
//        String text = "Gallia est omnis divisa in partes tres, quarum unam incolunt Belgae, aliam Aquitani, tertiam qui ipsorum lingua Celtae, nostra Galli appellantur.";
//        String text = null;
//        try {
//            text = Files.toString(new File("/Users/alessio/Downloads/Benedetti_1585.txt"), Charsets.UTF_8);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Annotation annotation = new Annotation(text);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        pipeline.annotate(annotation);

        String json = null;
        try {
            json = JSONOutputter.jsonPrint(annotation);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(json);
    }
}
