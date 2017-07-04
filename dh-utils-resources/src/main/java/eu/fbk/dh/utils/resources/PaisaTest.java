package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

/**
 * Created by alessio on 08/08/16.
 */

public class PaisaTest {

    static Integer DEFAULT_NTHREADS = 1;

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("paisa-parser")
                    .withHeader("POS tagger for paisa corpus using Tint")
                    .withOption("i", "input-path", "PAISA corpus", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("o", "output-path", "output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("m", "pos-model", "POS model", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("t", "threads", String.format("# of threads (default %d)", DEFAULT_NTHREADS), "NUM",
                            CommandLine.Type.INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

//            String corpusFileName = "/Volumes/LEXAR/Resources/ita/corpora/paisa.raw.utf8";
//            String outputFileName = "/Volumes/LEXAR/Resources/ita/corpora/paisa.raw.utf8.tagged";
            File corpusFile = cmd.getOptionValue("i", File.class);
            File outputFile = cmd.getOptionValue("o", File.class);
            File posModel = cmd.getOptionValue("m", File.class);
            Integer nThreads = cmd.getOptionValue("t", Integer.class, DEFAULT_NTHREADS);

            Properties props = new Properties();
            props.setProperty("annotators", "ita_toksent, pos");
            props.setProperty("pos.model", posModel.getAbsolutePath());
            props.setProperty("customAnnotatorClass.ita_toksent",
                    "eu.fbk.dh.tint.tokenizer.annotators.ItalianTokenizerAnnotator");
            props.setProperty("pos.nthreads", Integer.toString(nThreads));
            StanfordCoreNLP tmpPipeline = new StanfordCoreNLP(props);

            BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("<text")) {
                    continue;
                }

                if (line.startsWith("</text")) {

                    String text = buffer.toString();
//                    System.out.println(text);
                    Annotation document = new Annotation(text);
                    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
                    pipeline.annotate(document);
//                    System.out.println(pipeline.timingInformation());

                    for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
                        StringBuffer sentenceBuffer = new StringBuffer();
                        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                            sentenceBuffer.append(token.originalText().replaceAll("[\\s_]", ""));
                            sentenceBuffer.append("_");
                            sentenceBuffer.append(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                            sentenceBuffer.append(" ");
                        }
                        writer.append(sentenceBuffer.toString().trim());
                        writer.append("\n");
                    }
                    writer.append("\n");
                    writer.flush();

                    buffer = new StringBuffer();
                    continue;
                }

                buffer.append(line).append("\n");
            }

            writer.close();
            reader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
