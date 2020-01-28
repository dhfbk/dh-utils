package eu.fbk.dh.utils.resources;

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
import java.util.Iterator;

/**
 * Created by alessio on 11/01/17.
 */

public class SubtitlesExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubtitlesExtractor.class);

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
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input-path", File.class);
            File outputFile = cmd.getOptionValue("output-path", File.class);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String[] extensions = { "xml.gz", "xml" };
            Iterator<File> fileIterator = FileUtils.iterateFiles(inputFile, extensions, true);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("/document/s");

            while (fileIterator.hasNext()) {
                File f = fileIterator.next();
                InputStream read = IO.read(f.getAbsolutePath());
                Document doc = dBuilder.parse(read);
                doc.getDocumentElement().normalize();

                LOGGER.info(f.getAbsolutePath());

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
                    writer.append(buffer.toString().trim()).append("\n");
                }

                writer.append("\n");
                writer.flush();
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
