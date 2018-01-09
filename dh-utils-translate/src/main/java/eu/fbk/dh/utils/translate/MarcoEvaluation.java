package eu.fbk.dh.utils.translate;

import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MarcoEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarcoEvaluation.class);

    public static void main(String[] args) {
        String sentenceText;
        try {

            sentenceText = "Il camion si arresta all'ingresso del paese.";

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();

            pipeline.setProperty("annotators", "ita_toksent, pos, semafor_ita");
            pipeline.setProperty("customAnnotatorClass.semafor_ita", "eu.fbk.fcw.semafortranslate.SemaforTranslateAnnotator");

            pipeline.setProperty("semafor_ita.yandex.key", "trnsl.1.1.20171010T091318Z.1e87b765f05f625b.099f046a438c4dd1efac8bbbd3a6f9f7d80fc866");
            pipeline.setProperty("semafor_ita.engine", "deepl");

            pipeline.setProperty("semafor_ita.stanford.annotators", "tokenize, ssplit, pos, lemma, conll_parse, semafor");
//            pipeline.setProperty("semafor_ita.stanford.annotators", "tokenize, ssplit");
            pipeline.setProperty("semafor_ita.stanford.semafor.model_dir", "/Volumes/Dati/Resources/pikes/models/semafor-orig");
            pipeline.setProperty("semafor_ita.stanford.semafor.use_conll", "true");
            pipeline.setProperty("semafor_ita.stanford.customAnnotatorClass.mst_fake", "eu.fbk.dkm.pikes.depparseannotation.FakeMstParserAnnotator");
            pipeline.setProperty("semafor_ita.stanford.customAnnotatorClass.semafor", "eu.fbk.fcw.semafor.SemaforAnnotator");
            pipeline.setProperty("semafor_ita.stanford.customAnnotatorClass.conll_parse", "eu.fbk.fcw.mate.AnnaParseAnnotator");
            pipeline.setProperty("semafor_ita.stanford.conll_parse.model", "/Volumes/Dati/Resources/pikes/models/anna_parse.model");

            pipeline.setProperty("semafor_ita.aligner.host", "dh-server.fbk.eu");
            pipeline.setProperty("semafor_ita.aligner.port", "9010");
            pipeline.load();

//            Annotation annotation = pipeline.runRaw(sentenceText);
            pipeline.run(sentenceText, System.out, TintRunner.OutputFormat.JSON);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
