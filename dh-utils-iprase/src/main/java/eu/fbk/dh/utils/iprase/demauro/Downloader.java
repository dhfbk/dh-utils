package eu.fbk.dh.utils.iprase.demauro;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.scraping.SinglePageParser;
import eu.fbk.utils.scraping.URLpage;
import jodd.jerry.Jerry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * Created by alessio on 10/03/16.
 */

public class Downloader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Downloader.class);

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./download-demauro")
                    .withHeader(
                            "Download De Mauro online dictionary")
                    .withOption("o", "output", "Output folder", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File outputFolder = cmd.getOptionValue("output", File.class);
            if (!outputFolder.mkdirs()) {
                LOGGER.error("Unable to create folder {}", outputFolder.getAbsolutePath());
            }

            // Remove DEBUG log from jodd
            // ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger("jodd")).setLevel(Level.ERROR);

            for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {

                File pageFolder = new File(outputFolder.getAbsolutePath() + File.separator + alphabet);
                if (!pageFolder.mkdirs()) {
                    LOGGER.error("Unable to create folder {}", pageFolder.getAbsolutePath());
                }

                int i = 1;
                while (true) {

                    String startingURL = "https://dizionario.internazionale.it/lettera/" + alphabet;
                    if (i > 1) {
                        startingURL += String.format("-%d/", i);
                    }

                    LOGGER.info("Downloading {}", startingURL);

                    URLpage p = new URLpage(startingURL);
                    String content = p.getContent();

                    if (content == null) {
                        break;
                    }

                    Jerry document = Jerry.jerry(content);
                    Jerry jerry = document.$("a.serp-lemma-title");
                    if (jerry.size() == 0) {
                        break;
                    }

                    for (Jerry j : jerry) {
                        String link = j.attr("href");
                        URLpage thisPage = new URLpage(startingURL, link);
                        URL url = thisPage.getMyURL();
//                        LOGGER.info("Parsing {}", url);

                        File thisFile = new File(pageFolder.getAbsolutePath() + File.separator + url.toString()
                                .replaceAll("[^a-zA-Z0-9-]", ""));

                        if (thisFile.exists()) {
//                            LOGGER.info("File {} exists, skipping", thisFile.getAbsolutePath());
                            continue;
                        }

                        String article = SinglePageParser.parseContent(url.toString());
                        if (article == null) {
                            LOGGER.warn("Article is null, skipping");
                            continue;
                        }

                        Files.write(article, thisFile, Charsets.UTF_8);
                        Thread.sleep(100);
                    }

                    Thread.sleep(200);
                    i++;
                }
            }
        }
        catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
