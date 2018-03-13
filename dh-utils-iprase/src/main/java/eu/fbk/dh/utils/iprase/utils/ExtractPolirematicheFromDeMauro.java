package eu.fbk.dh.utils.iprase.utils;

import com.google.common.base.Charsets;
import edu.stanford.nlp.ling.CoreLabel;
import eu.fbk.dh.tint.tokenizer.ItalianTokenizer;
import eu.fbk.utils.core.CommandLine;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


public class ExtractPolirematicheFromDeMauro {

    private static final Pattern strongPattern = Pattern.compile("<strong>([^<>]+)</strong>");
    private static final ItalianTokenizer tokenizer = new ItalianTokenizer();

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-polirematiche")
                    .withHeader(
                            "Extract polirematiche from De Mauro")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            Set<String> added = new HashSet<>();

            for (File letterFolder : inputFolder.listFiles()) {
                if (!letterFolder.isDirectory()) {
                    continue;
                }

                System.out.println("Letter: " + letterFolder.getName().toUpperCase());

                for (File file : letterFolder.listFiles()) {

                    InputStream stream;
                    if (file.getName().endsWith(".gz")) {
                        stream = new GZIPInputStream(new FileInputStream(file));
                    } else {
                        stream = new FileInputStream(file);
                    }

                    StringWriter stringWriter = new StringWriter();
                    IOUtils.copy(new InputStreamReader(stream, Charsets.UTF_8), stringWriter);
                    String html = stringWriter.toString();

                    Document doc = Jsoup.parse(html);

                    Elements polirematiche = doc.select("#polirematiche");
                    for (Element element : polirematiche) {
                        String content = element.html();
                        String[] parts = content.split("<br[^>]*>");

                        String word = "";
                        boolean inv = false;
                        for (String part : parts) {
                            Matcher strongMatcher = strongPattern.matcher(part);
                            if (strongMatcher.find()) {

                                // Save last word
                                if (word.length() > 0) {
                                    saveWord(writer, word, inv, added);
                                }

                                word = strongMatcher.group(1);
                                inv = false;
                                continue;
                            }

                            if (part.contains("inv.") || part.contains("loc.avv.") || part.contains("loc.prep.")) {
                                inv = true;
                            }
                        }

                        // Save last word
                        if (word.length() > 0) {
                            saveWord(writer, word, inv, added);
                        }
                    }

//                    Elements polirematiche = doc.select("#polirematiche strong");
//                    for (Element element : polirematiche) {
//                        String text = element.html();
//                        text = text.replaceAll("’", "'");
//                        writer.append(text).append("\n");
//                    }

                }

                writer.flush();
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    private static void saveWord(BufferedWriter writer, String word, boolean inv, Set<String> added) throws IOException {
        List<List<CoreLabel>> tokens = tokenizer.parse(word);
        StringBuffer buffer = new StringBuffer();
        for (List<CoreLabel> sentence : tokens) {
            for (CoreLabel token : sentence) {
                buffer.append(token.originalText()).append(" ");
            }
        }

        word = buffer.toString().trim();
        word = word.replaceAll("’", "'");
        if (added.contains(word)) {
            return;
        }

        if (inv) {
            writer.append("@");
        } else {
            if (word.contains("essere")) {
                writer.append("%");
            }
        }
        writer.append(word).append("\n");
        added.add(word);
    }
}
