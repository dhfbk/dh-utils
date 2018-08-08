package eu.fbk.dh.utils.iprase.utils;

import java.util.Properties;

public class IpraseProperties {

    static public Properties properties = new Properties();

    static {

        properties.setProperty("customAnnotatorClass.monosillabi", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("monosillabi.id", "1");
        properties.setProperty("monosillabi.name", "MONOSILLABI");
        properties.setProperty("monosillabi.class", "eu.fbk.dh.utils.iprase.annotators.MonosillabiAnnotator");

        properties.setProperty("customAnnotatorClass.apostrofi", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("apostrofi.id", "2");
        properties.setProperty("apostrofi.name", "APOSTROFI");
        properties.setProperty("apostrofi.class", "eu.fbk.dh.utils.iprase.annotators.ApostrofiAnnotator");

        properties.setProperty("customAnnotatorClass.maiuscole", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("maiuscole.id", "3");
        properties.setProperty("maiuscole.name", "MAIUSCOLE");
        properties.setProperty("maiuscole.class", "eu.fbk.dh.utils.iprase.annotators.MaiuscoleAnnotator");

        properties.setProperty("customAnnotatorClass.articoli", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("articoli.id", "4");
        properties.setProperty("articoli.name", "IL");
        properties.setProperty("articoli.class", "eu.fbk.dh.utils.iprase.annotators.ArticleAnnotator");

        properties.setProperty("customAnnotatorClass.loro", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("loro.id", "5");
        properties.setProperty("loro.name", "LORO");
        properties.setProperty("loro.class", "eu.fbk.dh.utils.iprase.annotators.LoroAnnotator");

        properties.setProperty("customAnnotatorClass.gli", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("gli.id", "6");
        properties.setProperty("gli.name", "GLI");
        properties.setProperty("gli.class", "eu.fbk.dh.utils.iprase.annotators.GliAnnotator");

        properties.setProperty("customAnnotatorClass.quest", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("quest.id", "7");
        properties.setProperty("quest.name", "QUESTO");
        properties.setProperty("quest.class", "eu.fbk.dh.utils.iprase.annotators.QuestAnnotator");

        properties.setProperty("customAnnotatorClass.trivial", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("trivial.id", "8");
        properties.setProperty("trivial.name", "PAROLE_BANALI");
        properties.setProperty("trivial.class", "eu.fbk.dh.utils.iprase.annotators.TrivialAnnotator");

        properties.setProperty("customAnnotatorClass.imperfetti", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("imperfetti.id", "9");
        properties.setProperty("imperfetti.name", "INDICATIVO_IMPERFETTO");
        properties.setProperty("imperfetti.class", "eu.fbk.dh.utils.iprase.annotators.ImperfettiAnnotator");

        properties.setProperty("customAnnotatorClass.gerundi", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("gerundi.id", "10");
        properties.setProperty("gerundi.name", "GERUNDIO");
        properties.setProperty("gerundi.class", "eu.fbk.dh.utils.iprase.annotators.GerundiAnnotator");

        properties.setProperty("customAnnotatorClass.ind_pres", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("ind_pres.id", "11");
        properties.setProperty("ind_pres.name", "INDICATIVO_PRESENTE");
        properties.setProperty("ind_pres.class", "eu.fbk.dh.utils.iprase.annotators.IndicativiPresentiAnnotator");

        properties.setProperty("customAnnotatorClass.stare_andare", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("stare_andare.id", "12");
        properties.setProperty("stare_andare.name", "STARE_ANDARE");
        properties.setProperty("stare_andare.class", "eu.fbk.dh.utils.iprase.annotators.StareAndareAnnotator");

        properties.setProperty("customAnnotatorClass.affissi", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("affissi.id", "13");
        properties.setProperty("affissi.name", "AFFISSI");
        properties.setProperty("affissi.class", "eu.fbk.dh.utils.iprase.annotators.AffissiAnnotator");

        properties.setProperty("customAnnotatorClass.nominali", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("nominali.id", "14");
        properties.setProperty("nominali.name", "FRASI_NOMINALI");
        properties.setProperty("nominali.class", "eu.fbk.dh.utils.iprase.annotators.NominaliAnnotator");

        properties.setProperty("customAnnotatorClass.connettivi", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("connettivi.id", "15");
        properties.setProperty("connettivi.name", "CONNETTIVI");
        properties.setProperty("connettivi.class", "eu.fbk.dh.utils.iprase.annotators.ConnettiviAnnotator");

        properties.setProperty("customAnnotatorClass.congiunzioni", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("congiunzioni.id", "16");
        properties.setProperty("congiunzioni.name", "CONGIUNZIONI");
        properties.setProperty("congiunzioni.class", "eu.fbk.dh.utils.iprase.annotators.CongiunzioniAnnotator");

        properties.setProperty("customAnnotatorClass.punteggiatura", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("punteggiatura.id", "17");
        properties.setProperty("punteggiatura.name", "PUNTEGGIATURA");
        properties.setProperty("punteggiatura.class", "eu.fbk.dh.utils.iprase.annotators.PunteggiaturaAnnotator");

        properties.setProperty("customAnnotatorClass.perche_quando", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("perche_quando.id", "18");
        properties.setProperty("perche_quando.name", "PERCHE_QUANDO");
        properties.setProperty("perche_quando.class", "eu.fbk.dh.utils.iprase.annotators.PercheQuandoAnnotator");

        properties.setProperty("customAnnotatorClass.gergale", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("gergale.id", "19");
        properties.setProperty("gergale.name", "REGISTRO_INFORMALE");
        properties.setProperty("gergale.class", "eu.fbk.dh.utils.iprase.annotators.GergaleAnnotator");

        properties.setProperty("customAnnotatorClass.anglicismi", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("anglicismi.id", "20");
        properties.setProperty("anglicismi.name", "ANGLICISMI");
        properties.setProperty("anglicismi.class", "eu.fbk.dh.utils.iprase.annotators.AnglicismiAnnotator");

        properties.setProperty("customAnnotatorClass.politicamente_corretto", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("politicamente_corretto.id", "21");
        properties.setProperty("politicamente_corretto.name", "POLITICAMENTE_CORRETTO");
        properties.setProperty("politicamente_corretto.class", "eu.fbk.dh.utils.iprase.annotators.PoliticamenteCorrettoAnnotator");

        properties.setProperty("customAnnotatorClass.polirematiche", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("polirematiche.id", "22");
        properties.setProperty("polirematiche.name", "POLIREMATICHE");
        properties.setProperty("polirematiche.class", "eu.fbk.dh.utils.iprase.annotators.PolirematicheAnnotator");

        properties.setProperty("customAnnotatorClass.plastismi", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("plastismi.id", "23");
        properties.setProperty("plastismi.name", "PLASTISMI");
        properties.setProperty("plastismi.class", "eu.fbk.dh.utils.iprase.annotators.PlastismiAnnotator");

        properties.setProperty("customAnnotatorClass.frasi_scisse", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("frasi_scisse.id", "25");
        properties.setProperty("frasi_scisse.name", "FRASI_SCISSE");
        properties.setProperty("frasi_scisse.class", "eu.fbk.dh.utils.iprase.annotators.FrasiScisseAnnotator");

        properties.setProperty("customAnnotatorClass.li", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("li.id", "27");
        properties.setProperty("li.name", "LI");
        properties.setProperty("li.class", "eu.fbk.dh.utils.iprase.annotators.LiAnnotator");

        properties.setProperty("customAnnotatorClass.d_eufonica", "eu.fbk.dh.utils.iprase.MainAnnotator");
        properties.setProperty("d_eufonica.id", "28");
        properties.setProperty("d_eufonica.name", "D_EUFONICA");
        properties.setProperty("d_eufonica.class", "eu.fbk.dh.utils.iprase.annotators.DEufonicaAnnotator");

    }
}
