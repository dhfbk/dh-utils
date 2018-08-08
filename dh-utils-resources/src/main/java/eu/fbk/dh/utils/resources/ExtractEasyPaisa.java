package eu.fbk.dh.utils.resources;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;


public class ExtractEasyPaisa {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractEasyPaisa.class);
    private static Set<Integer> allowedTypes = new HashSet<>();
    private static Set<Integer> textTypes = new HashSet<>();

    private static final int NUM_LINES = 500000;
    private static final int MIN_LEN = 6;
    private static final float MIN_LETTERS = 0.6f;

    public static void main(String[] args) {

        textTypes.add(1);
        textTypes.add(2);

        allowedTypes.add(1); // Text
        allowedTypes.add(2); // Text
        allowedTypes.add(12); // Space
        allowedTypes.add(20); // Symbol
        allowedTypes.add(21); // Symbol
        allowedTypes.add(22); // Symbol
        allowedTypes.add(24); // Symbol
        allowedTypes.add(25); // Symbol
        allowedTypes.add(26); // Symbol
        allowedTypes.add(27); // Symbol
        allowedTypes.add(28); // Symbol
        allowedTypes.add(29); // Symbol
        allowedTypes.add(30); // Symbol
        allowedTypes.add(9); // Number

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-easy-paisa")
                    .withHeader(
                            "Extract first N lines of Paisa' corpus")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent");
            pipeline.load();

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            Set<String> done = new HashSet<>();

            int i = 0;

            String line;
            mainloop:
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }

                String text = parts[1];

                if (text.length() < MIN_LEN) {
                    continue;
                }

                int chars = 0;
                int letters = 0;
                boolean start = true;
                StringBuffer cBuffer = new StringBuffer();

                for (char c : text.toCharArray()) {
                    int type = Character.getType(c);
//                    String name = Character.getName(type);
//                    System.out.println(c + " " + type + " " + name);
                    chars++;
                    boolean isLetter = textTypes.contains(type);
                    if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                        cBuffer.append(c);
                    }
                    if (isLetter) {
                        letters++;
                    }
                    if (start) {
                        start = false;
                        if (!isLetter) {
                            continue mainloop;
                        }
                    }

                    if (!allowedTypes.contains(type)) {
                        continue mainloop;
                    }
                }

                if (letters * 1.0 / chars <= MIN_LETTERS) {
                    continue;
                }

                String charString = cBuffer.toString().toLowerCase();
                if (done.contains(charString)) {
                    continue;
                }
                done.add(charString);

                if (++i > NUM_LINES) {
                    break;
                }

                Annotation annotation = pipeline.runRaw(text);
                StringBuffer buffer = new StringBuffer();
                for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
                    buffer.append(token.originalText()).append(" ");
                }

                writer.append(buffer.toString().trim()).append("\n");
                if (i % 1000 == 0) {
                    writer.flush();
                }
//                System.out.println(i + " ---  " + text);
            }

            writer.close();
            reader.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
