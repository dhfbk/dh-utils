package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

/**
 * Created by alessio on 08/08/16.
 */

public class PaisaParser {

    private static final String annotators = "ita_toksent, pos, ita_morpho, ita_lemma, depparse, readability";
    private static final Pattern numPattern = Pattern.compile("id=.([0-9]+)");

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("paisa-parser")
                    .withHeader("Parse PAISA corpus")
                    .withOption("i", "input-path", "PAISA corpus", "FILE", CommandLine.Type.FILE_EXISTING, true, false,
                            true)
                    .withOption("o", "output-path", "output folder", "FOLDER", CommandLine.Type.DIRECTORY, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File corpusFile = cmd.getOptionValue("i", File.class);
            File outputFolder = cmd.getOptionValue("o", File.class);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", annotators);
            pipeline.load();

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
//            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            String line;
            StringBuffer buffer = new StringBuffer();
            Integer id = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("<text")) {
                    Matcher matcher = numPattern.matcher(line);
                    if (!matcher.find()) {
                        id = null;
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
                if (line.startsWith("permalink")) {
                    continue;
                }

                if (line.startsWith("</text")) {

                    String text = buffer.toString();

                    if (id == null) {
                        System.err.println("Error in text, no ID found");
                    } else if (text.length() <= 100) {
                        System.err.println("Text is small - " + id.toString());
                    } else {

                        String formatted = String.format("%07d", id);
                        String folder = formatted.substring(0, 3);
                        File folderFile = new File(outputFolder.getAbsolutePath() + File.separator + folder);
                        if (!folderFile.exists()) {
                            folderFile.mkdirs();
                        }
                        File file = new File(folderFile.getAbsolutePath() + File.separator + formatted + ".json.gz");
                        FileOutputStream fos = new FileOutputStream(file);
                        GZIPOutputStream stream = new GZIPOutputStream(fos);
                        pipeline.run(text, stream, TintRunner.OutputFormat.JSON);
                        stream.close();
                        fos.close();

//                        Annotation annotation = pipeline.runRaw(text);
//                        Readability readability = annotation.get(ReadabilityAnnotations.ReadabilityAnnotation.class);
//
//                        Double main = readability.getMeasures().get("main");
//
//                        if (main > 20) {
//                            Double level1 = readability.getMeasures().get("level1");
//                            Double level2 = readability.getMeasures().get("level2");
//                            Double level3 = readability.getMeasures().get("level3");
//
//                            writer.append(id.toString()).append("\t").append(main.toString()).append("\t");
//                            writer.append(level1.toString()).append("\t");
//                            writer.append(level2.toString()).append("\t");
//                            writer.append(level3.toString()).append("\n");
//
//                            writer.flush();
//                        }
                    }

                    buffer = new StringBuffer();
                    continue;
                }

                buffer.append(line).append("\n");
            }

//            writer.close();
            reader.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
