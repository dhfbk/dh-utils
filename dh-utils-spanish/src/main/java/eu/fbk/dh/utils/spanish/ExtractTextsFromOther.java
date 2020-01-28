package eu.fbk.dh.utils.spanish;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ExtractTextsFromOther {
    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-text-other")
                    .withHeader("Extract texts from other resources")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            Set<String> folders = new HashSet<>();
            folders.add("out");
            folders.add("out2");
            folders.add("out3");
            folders.add("out4");
            folders.add("out5");
            folders.add("out6");
            folders.add("out7");
            folders.add("outwiki");

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit");
            properties.setProperty("tokenize.language", "es");
            StanfordCoreNLP coreNLP = new StanfordCoreNLP(properties);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            for (String folder : folders) {
                File folderFile = new File(inputFolder.getAbsolutePath() + File.separator + folder);
                if (!folderFile.exists()) {
                    System.out.println(String.format("Folder %s does not exist", folderFile.getAbsolutePath()));
                    continue;
                }

                for (File file : folderFile.listFiles()) {
                    boolean skip = false;
                    String text = Files.toString(file, Charsets.UTF_8);
                    Annotation annotation = new Annotation(text);
                    coreNLP.annotate(annotation);
                    for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                        String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class).trim();
                        sentenceText = sentenceText.replaceAll("\\s+", " ");
                        if (sentenceText.length() < 5) {
                            continue;
                        }
                        if (sentenceText.contains("%%")) {
                            continue;
                        }
                        if (sentenceText.contains("agent = navigator")) {
                            skip = true;
                        }
                        if (sentenceText.contains("nextSibling")) {
                            skip = false;
                            continue;
                        }
                        if (!skip && sentenceText.contains("userAgent")) {
                            skip = true;
                        }
                        if (sentenceText.contains("insertBefore")) {
                            skip = false;
                            continue;
                        }

                        if (!skip) {
                            writer.append(sentenceText).append("\n");
                        }
                    }
                }
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
