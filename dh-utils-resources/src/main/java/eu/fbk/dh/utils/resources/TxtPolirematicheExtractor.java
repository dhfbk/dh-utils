package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.utils.iprase.annotations.AnnotationEvent;
import eu.fbk.dh.utils.iprase.annotations.CatAnnotations;
import eu.fbk.dh.utils.iprase.annotations.Task;
import eu.fbk.dh.utils.iprase.utils.IpraseProperties;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.IO;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by alessio on 11/01/17.
 */

public class TxtPolirematicheExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TxtPolirematicheExtractor.class);
    private static final Integer NUM_THREADS = 1;

    static class ParseSingle implements Runnable {

        private Properties pipelineProperties;
        private String line;
        private BufferedWriter writer;

        public ParseSingle(Properties pipelineProperties, String line, BufferedWriter writer) {
            this.pipelineProperties = pipelineProperties;
            this.line = line;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {

                TintPipeline pipeline = new TintPipeline();
                pipeline.setProps(pipelineProperties);
                pipeline.load();

                StringBuffer stringBuffer = new StringBuffer();
                Set<Integer> ids = new HashSet<>();
                Annotation annotation = pipeline.runRaw(line);
                List<Task> tasks = annotation.get(CatAnnotations.CatTasksAnnotation.class);
                for (AnnotationEvent event : tasks.get(0).getEvents()) {
                    boolean useEvent = true;

                    Integer prevToken = null;
                    for (Integer tokenID : event.getTokenIDs()) {
                        if (prevToken != null) {
                            int diff = tokenID - prevToken;
                            if (diff != 1) {
                                useEvent = false;
                            }
                        }

                        prevToken = tokenID;
                    }

                    if (useEvent) {
                        List<Integer> tokenIDs = event.getTokenIDs();
                        for (int i1 = 1; i1 < tokenIDs.size(); i1++) {
                            ids.add(tokenIDs.get(i1));
                        }
                    }
                }

                StringBuffer finalText = new StringBuffer();
                for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
                    int index = token.index();
                    char separator = ' ';
                    if (ids.contains(index)) {
                        separator = '_';
                    }
                    finalText.append(separator).append(token.originalText());
                }

                stringBuffer.append(finalText.toString().trim()).append("\n");

//                stringBuffer.append("\n");
                writer.append(stringBuffer.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("extract-subtitles")
                    .withHeader("Extract subtitles")
                    .withOption("i", "input-file", "Txt file", "FILE",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output-path", "Output file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withOption("t", "threads", "Number of threads", "THREADS", CommandLine.Type.POSITIVE_INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input-file", File.class);
            File outputFile = cmd.getOptionValue("output-path", File.class);
            Integer threads = cmd.getOptionValue("threads", Integer.class, NUM_THREADS);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, polirematiche");
            pipeline.addProperties(IpraseProperties.properties);
            pipeline.load();

            Properties pipelineProps = pipeline.getProps();

            ExecutorService pool = Executors.newFixedThreadPool(threads);
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                pool.submit(new ParseSingle(pipelineProps, line, writer));
            }
            reader.close();

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            pool.shutdownNow();

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
