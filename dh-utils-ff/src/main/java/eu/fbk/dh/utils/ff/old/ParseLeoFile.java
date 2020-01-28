package eu.fbk.dh.utils.ff.old;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.dh.tint.runner.TintPipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class ParseLeoFile {

    static Set<String> skipWords = new HashSet<>();
    static {
        skipWords.add("avere");
        skipWords.add("essere");
        skipWords.add("più");
        skipWords.add("non");
        skipWords.add("mio");
        skipWords.add("tuo");
        skipWords.add("suo");
        skipWords.add("mia");
        skipWords.add("tua");
        skipWords.add("sua");
        skipWords.add("mie");
        skipWords.add("tue");
        skipWords.add("sue");
        skipWords.add("miei");
        skipWords.add("tuoi");
        skipWords.add("suoi");
        skipWords.add("nostro");
        skipWords.add("vostro");
        skipWords.add("nostra");
        skipWords.add("vostra");
        skipWords.add("nostri");
        skipWords.add("vostri");
        skipWords.add("nostre");
        skipWords.add("vostre");
        skipWords.add("loro");
        skipWords.add("così");
        skipWords.add("fare");
        skipWords.add("proprio");
        skipWords.add("proprie");
        skipWords.add("propria");
        skipWords.add("propri");
        skipWords.add("tutto");

    }

    public static void main(String[] args) {
        String inputFile = "/Users/alessio/Desktop/ff/dati-leonardo.txt";
        String outputContentWords = "/Users/alessio/Desktop/ff/out-content.txt";

        try {
            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ner, depparse, readability");
            pipeline.load();

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputContentWords));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 3) {
                    continue;
                }

                String type = parts[0];
                String word = parts[1].toLowerCase();
                String sentence = parts[2];

                System.out.println(sentence);
                Annotation annotation = pipeline.runRaw(sentence);

                for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
                    String tokenText = token.lemma().toLowerCase();
                    Boolean isContentWord = token.get(ReadabilityAnnotations.ContentWord.class);
                    Integer difficultyLevel = token.get(ReadabilityAnnotations.DifficultyLevelAnnotation.class);
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                    String thisType = "no";
                    if (word.equals(tokenText)) {
                        thisType = type;
                    }

                    if (pos.equals("SP")) {
                        continue;
                    }

                    if (skipWords.contains(tokenText)) {
                        continue;
                    }

                    if (!isContentWord) {
                        continue;
                    }

                    writer.append(thisType).append("\t");
                    writer.append(difficultyLevel.toString()).append("\t");
                    writer.append(tokenText).append("\n");

                }
            }
            writer.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
