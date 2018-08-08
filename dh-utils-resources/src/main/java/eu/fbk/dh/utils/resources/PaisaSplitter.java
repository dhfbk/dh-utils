package eu.fbk.dh.utils.resources;

import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 08/08/16.
 */

public class PaisaSplitter {

    private static final String annotators = "ita_toksent, pos, ita_morpho, ita_lemma, readability";
    private static final Pattern numPattern = Pattern.compile("id=.([0-9]+)");
    private static final Integer documentNumber = 387593;
    private static final Integer parts = 8;

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("paisa-splitter")
                    .withHeader("Split PAISA corpus")
                    .withOption("i", "input-path", "PAISA corpus", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("o", "output-path", "output file", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File corpusFile = cmd.getOptionValue("i", File.class);
            File outputFolder = cmd.getOptionValue("o", File.class);

            int perPart = documentNumber / parts;
            if (documentNumber % parts != 0) {
                perPart++;
            }

//            TintPipeline pipeline = new TintPipeline();
//            pipeline.loadDefaultProperties();
//            pipeline.setProperty("annotators", annotators);
//            pipeline.load();

            BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
//            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            outputFolder.mkdirs();

            String line;
            StringBuffer buffer = new StringBuffer();
            StringBuffer partBuffer = new StringBuffer();
            Integer id = null;
            int k = 0;
            int part = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("<text ")) {
                    Matcher matcher = numPattern.matcher(line);
                    if (!matcher.find()) {
                        id = null;
                        System.err.println("No ID here: " + line);
                        continue;
                    }

                    id = Integer.parseInt(matcher.group(1));

                    continue;
                }

                if (line.startsWith("postato da")) {
                    continue;
                }

                if (line.startsWith("categoria:")) {
                    continue;
                }

                if (line.startsWith("</text>")) {

                    String text = buffer.toString();

                    if (id == null) {
                        System.err.println("Error in text, no ID found");
                    } else if (text.length() == 0) {
//                        System.err.println("Text is empty, " + id.toString());
                    } else {
                        k++;

                        partBuffer.append(String.format("<text id='%d'>", id)).append("\n");
                        partBuffer.append(text).append("\n");
                        partBuffer.append("</text>").append("\n");

                        if (k >= perPart) {
                            part++;
                            File outFile = new File(outputFolder.getAbsolutePath() + File.separator + part + ".txt");
                            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
                            writer.append(partBuffer.toString());
                            writer.close();

                            System.out.println(String.format("Written part %d (k is %d)", part, k));

                            k = 0;
                            partBuffer = new StringBuffer();
                        }
                    }

                    buffer = new StringBuffer();
                    continue;
                }

                buffer.append(line).append("\n");
            }

            if (partBuffer.toString().trim().length() > 0) {
                part++;
                File outFile = new File(outputFolder.getAbsolutePath() + File.separator + part + ".txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
                writer.append(partBuffer.toString());
                writer.close();

                System.out.println(String.format("Written part %d (k is %d)", part, k));
            }

//            writer.close();
            reader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
