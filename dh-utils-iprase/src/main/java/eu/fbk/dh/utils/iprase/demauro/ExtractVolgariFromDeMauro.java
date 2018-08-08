package eu.fbk.dh.utils.iprase.demauro;

import com.google.common.base.Charsets;
import eu.fbk.utils.core.CommandLine;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;


public class ExtractVolgariFromDeMauro {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtractVolgariFromDeMauro.class);

    private static final Set<String> skipList = new HashSet<>();

    static {

    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-volgari")
                    .withHeader(
                            "Extract volgari from De Mauro")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            Set<String> added = new HashSet<>();
            added.addAll(skipList);

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
                    Elements meta = doc.select("meta");

                    String title = null;
                    String description = null;

                    for (Element m : meta) {
                        String name = m.attr("name");
                        String content = m.attr("content");

                        if (name == null || content == null) {
                            continue;
                        }

                        if (name.equals("keywords")) {
                            title = URLDecoder.decode(content, "UTF-8");
                        }
                        if (name.equals("description")) {
                            try {
                                description = URLDecoder.decode(content, "UTF-8");
                            }
                            catch (Exception e) {
                                // ignore
                            }
                        }

                    }

                    if (title != null && description != null) {
                        if (description.contains("volg.")) {
                            writer.append(title).append("\n");
                        }
                    }
                }

                writer.flush();
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
