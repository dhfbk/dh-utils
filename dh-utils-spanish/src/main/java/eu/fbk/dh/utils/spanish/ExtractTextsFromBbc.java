package eu.fbk.dh.utils.spanish;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dkm.pikes.twm.MachineLinking;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class ExtractTextsFromBbc {

    static Pattern urlPattern = Pattern.compile(".*/([^/]+)$");

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-bbc")
                    .withHeader("Extract texts from BBC news")
                    .withOption("f", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("d", "values", "Values file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File folderFile = cmd.getOptionValue("input", File.class);
            File valuesFile = cmd.getOptionValue("values", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit");
            properties.setProperty("tokenize.language", "es");
            StanfordCoreNLP coreNLP = new StanfordCoreNLP(properties);

            List<String> lines = Files.readLines(valuesFile, Charsets.UTF_8);
            HashMap<String, Double> values = new HashMap<>();
            for (String line : lines) {
                line = line.trim();
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                String fileName = parts[0];
                Double value = Double.parseDouble(parts[1]);
                values.put(fileName, value);
            }

            Properties mlProperties = new Properties();
            mlProperties.setProperty("address", "http://ml.apnetwork.it/annotate");
            MachineLinking machineLinking = new MachineLinking(mlProperties);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            values.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed()).forEach(pippo -> {
                File thisFile = new File(folderFile.getAbsolutePath() + File.separator + pippo.getKey());
                if (!thisFile.exists()) {
                    System.out.println(String.format("Err: file %s does not exist", pippo.getKey()));
                }
                try {
                    String text = Files.toString(thisFile, Charsets.UTF_8);
                    String lang = machineLinking.lang(text);
                    if (!lang.equals("es")) {
                        return;
                    }

                    Annotation annotation = new Annotation(text);
                    coreNLP.annotate(annotation);
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class).trim();
                        sentenceText = sentenceText.replaceAll("\\s+", " ");
                        if (sentenceText.contains("___")) {
                            continue;
                        }
                        if (sentenceText.contains("En fotos:")) {
                            continue;
                        }
                        if (sentenceText.length() < 5) {
                            continue;
                        }
                        writer.append(sentenceText).append("\n");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
