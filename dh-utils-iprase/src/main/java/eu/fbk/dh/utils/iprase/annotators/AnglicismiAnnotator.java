package eu.fbk.dh.utils.iprase.annotators;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import eu.fbk.dh.utils.iprase.annotators.abstracts.WordsAnnotator;

import java.net.URL;

public class AnglicismiAnnotator extends WordsAnnotator {
    @Override
    public void load() {

        try {
            URL anglicismi = Resources.getResource("anglicismi-final");
            for (String line : Resources.readLines(anglicismi, Charsets.UTF_8)) {
                line = line.trim().toLowerCase();
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("@")) {
                    line = line.replaceAll("@", "");
                }

                wordsLabel.put(line, "Non_Adattato");
                String noHyphen = line.replaceAll("-", "");
                if (!noHyphen.equals(line)) {
                    wordsLabel.put(noHyphen, "Non_Adattato");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        wordsLabel.put("messenger", "Non_Adattato");
        wordsLabel.put("follower", "Non_Adattato");
        wordsLabel.put("stalking", "Non_Adattato");
        wordsLabel.put("stalker", "Non_Adattato");
        wordsLabel.put("store", "Non_Adattato");
        wordsLabel.put("welfare", "Non_Adattato");

        wordsLabel.remove("mixare");

        wordsLabel.put("gol", "Adattato");
        wordsLabel.put("chattare", "Adattato");
        wordsLabel.put("skillato", "Adattato");
        wordsLabel.put("skillare", "Adattato");
        wordsLabel.put("stoppare", "Adattato");
        wordsLabel.put("mixare", "Adattato");
        wordsLabel.put("demo", "Adattato");
        wordsLabel.put("app", "Adattato");
        wordsLabel.put("info", "Adattato");
        wordsLabel.put("spoilerare", "Adattato");
        wordsLabel.put("buggato", "Adattato");
        wordsLabel.put("buggare", "Adattato");
        wordsLabel.put("flammare", "Adattato");
        wordsLabel.put("killare", "Adattato");
        wordsLabel.put("whatsappare", "Adattato");
        wordsLabel.put("twittare", "Adattato");
        wordsLabel.put("stalkerare", "Adattato");

        wordsLabel.put("stressare", "Adattato");
        wordsLabel.put("flippare", "Adattato");
        wordsLabel.put("schedulare", "Adattato");
        wordsLabel.put("scrollare", "Adattato");
        wordsLabel.put("settare", "Adattato");
        wordsLabel.put("sniffare", "Adattato");
        wordsLabel.put("strippare", "Adattato");
        wordsLabel.put("zippare", "Adattato");
        wordsLabel.put("testare", "Adattato");
        wordsLabel.put("cliccare", "Adattato");
        wordsLabel.put("formattare", "Adattato");
        wordsLabel.put("forwardare", "Adattato");
        wordsLabel.put("installare", "Adattato");
        wordsLabel.put("linkare", "Adattato");
        wordsLabel.put("loggarsi", "Adattato");
        wordsLabel.put("loggare", "Adattato");
        wordsLabel.put("masterizzare", "Adattato");
        wordsLabel.put("postare", "Adattato");
        wordsLabel.put("processare", "Adattato");
        wordsLabel.put("resettare", "Adattato");
        wordsLabel.put("scannerizzare", "Adattato");
        wordsLabel.put("sortare", "Adattato");
        wordsLabel.put("surfare", "Adattato");
        wordsLabel.put("trashare", "Adattato");
        wordsLabel.put("visualizzare", "Adattato");
        wordsLabel.put("dezippare", "Adattato");
        wordsLabel.put("stressato", "Adattato");
        wordsLabel.put("formattato", "Adattato");
        wordsLabel.put("flippato", "Adattato");
        wordsLabel.put("schedulato", "Adattato");
        wordsLabel.put("scrollato", "Adattato");
        wordsLabel.put("settato", "Adattato");
        wordsLabel.put("sniffato", "Adattato");
        wordsLabel.put("strippato", "Adattato");
        wordsLabel.put("zippato", "Adattato");
        wordsLabel.put("testato", "Adattato");
        wordsLabel.put("chattato", "Adattato");
        wordsLabel.put("cliccato", "Adattato");
        wordsLabel.put("forwardato", "Adattato");
        wordsLabel.put("installato", "Adattato");
        wordsLabel.put("linkato", "Adattato");
        wordsLabel.put("loggato", "Adattato");
        wordsLabel.put("masterizzato", "Adattato");
        wordsLabel.put("postato", "Adattato");
        wordsLabel.put("processato", "Adattato");
        wordsLabel.put("resettato", "Adattato");
        wordsLabel.put("scannerizzato", "Adattato");
        wordsLabel.put("sortato", "Adattato");
        wordsLabel.put("surfato", "Adattato");
        wordsLabel.put("trashato", "Adattato");
        wordsLabel.put("visualizzato", "Adattato");
        wordsLabel.put("dezippato", "Adattato");

        wordsToCollect.addAll(wordsLabel.keySet());
        alsoLemma = true;

        super.load();
    }
}
