package eu.fbk.dh.utils.spanish;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.FrequencyHashSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Properties;

public class Parse {

//    public static Pattern spacePattern = Pattern.compile("^\t");

    public static void main(String[] args) {
        File startFolder = new File("/Users/alessio/Dropbox/spagnolo");
        File outFile = new File("/Users/alessio/Dropbox/spagnolo/all-texts.txt");

        FrequencyHashSet<String> count = new FrequencyHashSet<>();

        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

            Properties properties = new Properties();
            properties.setProperty("annotators", "tokenize, ssplit");
            properties.setProperty("ssplit.newlineIsSentenceBreak", "always");

            StanfordCoreNLP coreNLP = new StanfordCoreNLP(properties);

            for (File file : startFolder.listFiles()) {
                if (file.isDirectory()) {
                    if (!file.getName().startsWith("out")) {
                        System.out.println("Skipping " + file.getName());
                        continue;
                    }
                    for (File documentFile : file.listFiles()) {
                        count.add(file.toString());
                        for (String line : Files.readLines(documentFile, Charsets.UTF_8)) {

                            if (line.startsWith("\t")) {
                                continue;
                            }

                            line = line.trim();

                            if (line.startsWith("Descárgatelo en pdf")) {
                                break;
                            }
                            if (line.startsWith("Vocabulario de la noticia")) {
                                break;
                            }
                            if (line.startsWith("Comprensión de la noticia")) {
                                break;
                            }
                            if (line.startsWith("Apuntes de gramática")) {
                                break;
                            }
                            if (line.startsWith("Ejercicio 1")) {
                                break;
                            }
                            if (line.startsWith("Escucha el audio")) {
                                break;
                            }
                            if (line.equals("Comprehension")) {
//                                System.out.println(documentFile);
                                break;
                            }
                            if (line.length() == 0) {
                                continue;
                            }

                            Annotation annotation = new Annotation(line);
                            coreNLP.annotate(annotation);

                            for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
                                String sentenceText = sentence.get(CoreAnnotations.TextAnnotation.class);
                                writer.write(sentenceText);
                                writer.write("\n");
                            }
                        }

//                        writer.write("\n");
                    }
                }
            }

            writer.close();

            for (String key : count.keySet()) {
                System.out.println(key + " ---> " + count.get(key));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
