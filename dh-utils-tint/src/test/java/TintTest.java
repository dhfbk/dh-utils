import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;

import java.io.File;

/**
 * Created by alessio on 07/09/16.
 */

public class TintTest {

    public static void main(String[] args) {
        String sentenceText;
        try {

//            sentenceText = "The trip was very beautiful. Unfortunately, my dog has died in the meantime.";
//            Annotation annotation = new Annotation(sentenceText);
//            Properties properties = new Properties();
//            properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
//            StanfordCoreNLP stanfordCoreNLP = new StanfordCoreNLP(properties);
//            stanfordCoreNLP.annotate(annotation);
//
//            String json = JSONOutputter.jsonPrint(annotation);
//            System.out.println(json);
//
//            System.exit(1);

//            sentenceText = "Domani insalatiamo tutti insieme. " +
//                    "Il mio cane ha smerdazzato il tuo. " +
//                    "Meglio dormirci su. " +
//                    "Nel suo mondo color arcobaleno e pieno di pucciosi unicorni si aspettava che LEXenstein fosse un programma in stile Word";
            sentenceText =
                    "L’innovazione tecnologica e sociale deve avere al centro le persone e le comunità. " +
                            "Può guidare i processi di pianificazione territoriale, favorire la ripresa economica e aiutare le città ad essere più amichevoli. " +
                            "FBK è partner tecnologico di diversi progetti già avviati sul territorio e, grazie alla collaborazione con il Comune, ha attivato strumenti di confronto con cui condividere idee per co-gestire i beni comuni. " +
                            "Nuovi servizi digitali permetteranno l’integrazione fra l’apporto dell’amministrazione, il frutto della ricerca, il contributo dell’industria e l’ascolto dei cittadini. " +
                            "";
//            sentenceText = "Ricordandomi di questo.";
//            sentenceText = "La Luna, ormai, non affascina più come un tempo. L'uomo, per natura, deve esplorare e conoscere cose sempre più lontane. L'attenzione degli scienziati si è focalizzata su Marte e, dopo anni di intense ricerche, non sono mancati i risultati. Su Marte, infatti, c'è dell'acqua; secondo Roberto Battiston, presidente dell'Agenzia Spaziale Italiana, questa è solo la prima delle tante sorprese che potrà regalarci il Pianeta Rosso.";

//            sentenceText = Files.toString(new File("/Users/alessio/Desktop/sanremo2018.txt"), Charsets.UTF_8);

            sentenceText = "Barack Obama è stato presidente degli Stati Uniti.";

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();

//            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ita_verb, ita_semafor");
//            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ita_verb, ita_derivation");
//            pipeline.setProperty("annotators", "ita_toksent");
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ner, depparse, fake_dep");

            pipeline.setProperty("stem.lang", "it");

            pipeline.setProperty("ita_semafor.yandex.key", "trnsl.1.1.20171010T091318Z.1e87b765f05f625b.099f046a438c4dd1efac8bbbd3a6f9f7d80fc866");
            pipeline.setProperty("ita_semafor.engine", "yandex");

            pipeline.setProperty("ita_semafor.stanford.annotators", "tokenize, ssplit, pos, lemma, conll_parse, semafor");
            pipeline.setProperty("ita_semafor.stanford.semafor.model_dir", "/Volumes/Dati/Resources/pikes/models/semafor-orig");
            pipeline.setProperty("ita_semafor.stanford.semafor.use_conll", "true");
            pipeline.setProperty("ita_semafor.stanford.customAnnotatorClass.mst_fake", "eu.fbk.dkm.pikes.depparseannotation.FakeMstParserAnnotator");
            pipeline.setProperty("ita_semafor.stanford.customAnnotatorClass.semafor", "eu.fbk.fcw.semafor.SemaforAnnotator");
            pipeline.setProperty("ita_semafor.stanford.customAnnotatorClass.conll_parse", "eu.fbk.fcw.mate.AnnaParseAnnotator");
            pipeline.setProperty("ita_semafor.stanford.conll_parse.model", "/Volumes/Dati/Resources/pikes/models/anna_parse.model");

            pipeline.setProperty("ita_semafor.aligner.host", "dh-server.fbk.eu");
            pipeline.setProperty("ita_semafor.aligner.port", "9010");

            pipeline.setProperty("ita_semafor.includeOriginal", "1");

            pipeline.load();

            pipeline.run(sentenceText, System.out, TintRunner.OutputFormat.JSON);
//            pipeline.run(sentenceText, ByteStreams.nullOutputStream(), TintRunner.OutputFormat.JSON);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}