package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;
import eu.fbk.dh.utils.iprase.ParseTexts;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by alessio on 11/01/17.
 */

public class SubtitlesPolirematicheExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitlesPolirematicheExtractor.class);
    private static final Integer NUM_THREADS = 1;

    static class ParseSingle implements Runnable {

        private Properties pipelineProperties;
        private File inputFile;
        private BufferedWriter writer;

        public ParseSingle(Properties pipelineProperties, File inputFile, BufferedWriter writer) {
            this.pipelineProperties = pipelineProperties;
            this.inputFile = inputFile;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {

                StringBuffer stringBuffer = new StringBuffer();

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();
                XPathExpression expr = xpath.compile("/document/s");

                TintPipeline pipeline = new TintPipeline();
                pipeline.setProps(pipelineProperties);
                pipeline.load();

                InputStream read = IO.read(inputFile.getAbsolutePath());
                Document doc = dBuilder.parse(read);
                doc.getDocumentElement().normalize();

                LOGGER.info("Starting {}", inputFile.getAbsolutePath());

                NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                for (int i = 0; i < nl.getLength(); i++) {
                    Node item = nl.item(i);
                    NodeList tokens = item.getChildNodes();
                    StringBuffer buffer = new StringBuffer();
                    for (int j = 0; j < tokens.getLength(); j++) {
                        Node tokenNode = tokens.item(j);
                        String text = tokenNode.getTextContent().trim();
                        if (text.length() == 0) {
                            continue;
                        }
                        buffer.append(text).append(" ");
                    }

                    String text = buffer.toString().trim();
                    if (text.length() == 0) {
                        continue;
                    }

                    Set<Integer> ids = new HashSet<>();
                    Annotation annotation = pipeline.runRaw(text);
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
                }

                LOGGER.info("Ended {}", inputFile.getAbsolutePath());

                stringBuffer.append("\n");
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
                    .withOption("i", "input-path", "Subtitles base folder", "FILE",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output-path", "Output file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withOption("t", "threads", "Number of threads", "THREADS", CommandLine.Type.POSITIVE_INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input-path", File.class);
            File outputFile = cmd.getOptionValue("output-path", File.class);
            Integer threads = cmd.getOptionValue("threads", Integer.class, NUM_THREADS);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String[] extensions = {"xml.gz", "xml"};
            Iterator<File> fileIterator = FileUtils.iterateFiles(inputFile, extensions, true);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, polirematiche");
            pipeline.addProperties(IpraseProperties.properties);
            pipeline.load();

            Properties pipelineProps = pipeline.getProps();

            ExecutorService pool = Executors.newFixedThreadPool(threads);
            while (fileIterator.hasNext()) {
                File f = fileIterator.next();
                pool.submit(new ParseSingle(pipelineProps, f, writer));
            }

            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            pool.shutdownNow();

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
