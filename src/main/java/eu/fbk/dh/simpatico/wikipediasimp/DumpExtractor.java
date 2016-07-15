package eu.fbk.dh.simpatico.wikipediasimp;

import eu.fbk.dkm.utils.CommandLine;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by alessio on 06/07/16.
 */

public class DumpExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DumpExtractor.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("command")
                    .withHeader("Description of the command")
                    .withOption("i", "input-path", "the base path of the corpus", "DIR",
                            CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output-path", "output file", "DIR",
                            CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("i", File.class);
            final File outputPath = cmd.getOptionValue("o", File.class);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            FileInputStream in = new FileInputStream(inputPath);
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);

            DumpHandler handler = new DumpHandler(outputPath);
            saxParser.parse(bzIn, handler);

            bzIn.close();
            in.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
