package eu.fbk.dh.utils.resources;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.primitives.UnsignedBytes;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class TestChar {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestChar.class);

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./remove-strange-chars")
                    .withHeader(
                            "Remove strange characters from a text")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                line = line.replace('\u0085', ' ');
                line = line.replaceAll("\\s+", " ");
                writer.append(line).append("\n");
            }

            reader.close();
            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }


//        try {
//            String file = "/Users/alessio/Desktop/test.txt";
//            String s = Files.toString(new File(file), Charsets.UTF_8);
//            s = s.replace('\u0085', ' ');
//
//            for (byte b : s.getBytes()) {
//                System.out.print(Integer.toHexString(UnsignedBytes.toInt(b)));
//                System.out.print(" ");
//                System.out.print(UnsignedBytes.toInt(b));
//                System.out.println();
//            }
//
//            for (char ch : s.toCharArray()) {
//                System.out.println("Do something with *** " + ch + " *** " + Integer.toHexString(ch));
//            }


//            int length = s.length();
//            for (int offset = 0; offset < length; ) {
//                final int codepoint = s.codePointAt(offset);
//
//                // do something with the codepoint
//                System.out.print(s.charAt(offset));
//                System.out.print(" " + Character.charCount(codepoint) + " ");
//                System.out.println(codepoint);
//
//                offset += Character.charCount(codepoint);
//            }


//            BufferedReader reader = new BufferedReader(new FileReader(file));

//            int r;
//            while ((r = reader.read()) != -1) {
//                char ch = (char) r;
//                System.out.println("Do something with *** " + ch + " *** " + Integer.toHexString(ch));
//            }

//            reader.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
