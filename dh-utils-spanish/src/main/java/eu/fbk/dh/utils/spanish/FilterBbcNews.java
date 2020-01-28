package eu.fbk.dh.utils.spanish;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilterBbcNews {

    static Pattern urlPattern = Pattern.compile(".*/([^/]+)$");

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./filter-bbc")
                    .withHeader("Filter BBC News for Spanish")
                    .withOption("f", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("d", "done", "Done file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

//            File inputFolder = cmd.getOptionValue("input", File.class);
//            File outputFile = cmd.getOptionValue("output", File.class);
//            String folderName = "/Users/alessio/Google Drive/spagnolo/outbbc";
//            String doneName = "/Users/alessio/Google Drive/spagnolo/done_pages_bbc.txt";
//            String doneValueName = "/Users/alessio/Google Drive/spagnolo/done_value.txt";
            File folderFile = cmd.getOptionValue("input", File.class);
            File doneFile = cmd.getOptionValue("done", File.class);
            File doneValueNameFile = cmd.getOptionValue("output", File.class);

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit, pos, fake_lemma, readability");
            properties.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
            properties.setProperty("readability.language", "es");
            properties.setProperty("tokenize.language", "es");
            properties.setProperty("customAnnotatorClass.readability", "eu.fbk.dh.tint.readability.ReadabilityAnnotator");
            properties.setProperty("customAnnotatorClass.fake_lemma", "eu.fbk.utils.corenlp.FakeLemmaAnnotator");
            StanfordCoreNLP coreNLP = new StanfordCoreNLP(properties);

            FrequencyHashSet<String> frequencies = new FrequencyHashSet<>();
            Map<String, Double> values = new HashMap<>();

//            File doneValueNameFile = new File(doneValueName);
            if (doneValueNameFile.exists()) {
                List<String> lines = Files.readLines(doneValueNameFile, Charsets.UTF_8);
                for (String line : lines) {
                    line = line.trim();
                    String[] parts = line.split("\t+");
                    values.putIfAbsent(parts[0], Double.parseDouble(parts[1]));
                }
            }

            List<String> urlList = Files.readLines(doneFile, Charsets.UTF_8);
            for (String url : urlList) {
                url = url.trim();
                if (url.length() == 0) {
                    continue;
                }

                if (!url.contains("/mundo/")) {
                    continue;
                }
                Matcher matcher = urlPattern.matcher(url);
                if (!matcher.find()) {
                    continue;
                }

                String lastPart = matcher.group(1);
//                System.out.println(lastPart);

                frequencies.add(lastPart);
            }

            for (String key : frequencies.keySet()) {
                if (frequencies.get(key) > 1) {
//                    System.out.println(String.format("Key %s is duplicated", key));
                    continue;
                }

                if (values.containsKey(key)) {
                    continue;
                }

                File thisFile = new File(folderFile.getAbsolutePath() + File.separator + key);
                if (!thisFile.exists()) {
                    System.out.println(String.format("ERR - File %s does not exist", thisFile.getAbsolutePath()));
                    continue;
                }

                String text = Files.toString(thisFile, Charsets.UTF_8);
                text = text.trim();
                if (text.length() == 0) {
                    continue;
                }

                System.out.println(String.format("Parsing %s", thisFile.getName()));
                Annotation annotation = new Annotation(text);
                coreNLP.annotate(annotation);
                Readability readability = annotation.get(ReadabilityAnnotations.ReadabilityAnnotation.class);
                values.put(key, readability.getMeasures().get("main"));

                BufferedWriter writer = new BufferedWriter(new FileWriter(doneValueNameFile));
                for (String k2 : values.keySet()) {
                    writer.append(k2).append("\t").append(Double.toString(values.get(k2))).append("\n");
                }
                writer.close();
            }

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
