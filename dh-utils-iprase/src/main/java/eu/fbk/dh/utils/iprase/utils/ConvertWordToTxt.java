package eu.fbk.dh.utils.iprase.utils;

import eu.fbk.utils.core.CommandLine;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

//import org.apache.poi.hwpf.extractor.WordExtractor;
//import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;

public class ConvertWordToTxt {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertWordToTxt.class);

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./convert-word-to-text")
                    .withHeader("Convert Word files to TXT")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);

            if (!outputFolder.exists()) {
                if (!outputFolder.mkdirs()) {
                    LOGGER.error("Unable to create target dir");
                    System.exit(1);
                }
            }

            for (File file : inputFolder.listFiles()) {
                if (!file.isDirectory()) {
                    continue;
                }

                File newFolder = new File(outputFolder + File.separator + file.getName());
                if (!newFolder.exists()) {
                    newFolder.mkdirs();
                }

                for (File wordFile : file.listFiles()) {
                    if (!wordFile.isFile()) {
                        continue;
                    }


                    //
//                    XWPFDocument doc = new XWPFDocument(in);
//                    XWPFWordExtractor ex = new XWPFWordExtractor(doc);
//                    String text = ex.getText();
                    //
                    if (!wordFile.getName().toLowerCase().endsWith("doc")
                            && !wordFile.getName().toLowerCase().endsWith("docx")) {
                        LOGGER.error("Invalid file {}", wordFile.getName());
                        continue;
                    }

                    String number = wordFile.getName().replaceAll("[^0-9]", "");

                    File newFile = new File(outputFolder + File.separator + file.getName() + File.separator + number + ".txt");
                    BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));

                    try {
                        if (wordFile.getName().toLowerCase().endsWith(".doc")) {
                            NPOIFSFileSystem fs = new NPOIFSFileSystem(wordFile);
                            WordExtractor extractor = new WordExtractor(fs.getRoot());

                            for (String rawText : extractor.getParagraphText()) {
                                String text = extractor.stripFields(rawText);
                                writer.append(text.trim()).append("\n");
                            }

                        }
                        if (wordFile.getName().toLowerCase().endsWith(".docx")) {
                            XWPFDocument doc = new XWPFDocument(new FileInputStream(wordFile));
                            XWPFWordExtractor ex = new XWPFWordExtractor(doc);
                            String text = ex.getText();
                            writer.append(text.trim()).append("\n");
                        }
                    } catch (Exception e) {
                        LOGGER.error(wordFile.getAbsolutePath());
                    }

                    writer.close();
                }

            }

        } catch (
                Exception e)

        {
            CommandLine.fail(e);
        }
    }

}
