package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.XMLHelper;
import org.apache.commons.io.output.WriterOutputStream;
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
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by alessio on 02/09/16.
 */

public class DeGasperi2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeGasperi.class);

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-degasperi")
                    .withHeader("Extract De Gasperi corpus for embeddings")
                    .withOption("i", "input", "Input file", "FILE",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FILE",
                            CommandLine.Type.DIRECTORY, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File folder = cmd.getOptionValue("input", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);

            outputFolder.mkdirs();

//            FrequencyHashSet<String> tenses = new FrequencyHashSet();
//            FrequencyHashSet<String> ners = new FrequencyHashSet();
//            AtomicInteger persons = new AtomicInteger(0);
//            AtomicInteger linkedPersons = new AtomicInteger(0);
//            AtomicInteger documents = new AtomicInteger(0);

            java.nio.file.Files.walk(folder.toPath()).collect(Collectors.toList()) // .parallelStream()
                    .forEach((Path filePath) -> {
                        try {
                            if (java.nio.file.Files.isRegularFile(filePath)) {
                                Path fileName = filePath.getFileName();

                                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                                XPathFactory xPathfactory = XPathFactory.newInstance();
                                XPath xpath = xPathfactory.newXPath();

                                TintPipeline pipeline = new TintPipeline();
                                pipeline.loadDefaultProperties();
                                pipeline.setProperty("annotators", "ita_toksent, quote");
                                pipeline.load();

                                LOGGER.info("FILE: {}", fileName);
                                InputStream stream = new FileInputStream(filePath.toFile());

                                Document doc = dBuilder.parse(stream);
                                doc.getDocumentElement().normalize();

                                XPathExpression expr;
                                NodeList nl;

                                expr = xpath.compile("/xml/file");
                                nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                                for (int i = 0; i < nl.getLength(); i++) {
                                    Node fileNode = nl.item(i);
                                    Node headNode = XMLHelper.getNode("head", fileNode.getChildNodes());

                                    String content = XMLHelper.getNodeValue("content", fileNode.getChildNodes());
                                    String title = XMLHelper.getNodeValue("title", headNode.getChildNodes());
                                    String date = XMLHelper.getNodeValue("date", headNode.getChildNodes());
                                    String id = XMLHelper.getNodeAttr("id", fileNode);

                                    System.out.println("ID: " + id);
                                    if (content.length() == 0) {
                                        LOGGER.warn("Text is empty");
                                        break;
                                    }

                                    String outputFile =
                                            outputFolder.getAbsolutePath() + File.separator + fileName.toString() + "."
                                                    + id;
                                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                                    pipeline.setDocumentDate(date);

                                    try {

                                        InputStream inputStream = new ByteArrayInputStream(content.getBytes());
//                                        Annotation annotation = pipeline.runRaw(content);
//                                        List<CoreMap> quotes = annotation.get(CoreAnnotations.QuotationsAnnotation.class);
//                                        if (quotes.size() > 0) {
//                                            System.out.println(quotes);
//                                            System.exit(1);
//                                        }
                                        pipeline.run(inputStream, new WriterOutputStream(writer),
                                                TintRunner.OutputFormat.JSON);
                                        writer.close();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
