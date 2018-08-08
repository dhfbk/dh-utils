package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.utils.scraping.URLpage;
import jodd.jerry.Jerry;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;

public class Scraper {
    public static void main(String[] args) {
        String startingPage = "http://www.ling.upenn.edu/hist-corpora/PPCEME-RELEASE-3/philological_info.html";
        String outputFolder = "/Users/alessio/Desktop/PPCHE_4.0_corpus_files/web";

        File htmlFolder = new File(outputFolder + File.separator + "html");
        File webFolder = new File(outputFolder);
        htmlFolder.mkdirs();


        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(webFolder.getAbsolutePath() + File.separator + "genres"));

            URLpage p = new URLpage(startingPage);
            String content = p.getContent();

            Jerry document = Jerry.jerry(content);
            Jerry jerry = document.$("a");
            if (jerry.size() > 0) {
                for (Jerry j : jerry) {
                    String link = j.attr("href");
                    if (link == null) {
                        continue;
                    }
                    if (!link.startsWith("info/")) {
                        continue;
                    }

                    URLpage thisPage = new URLpage(startingPage, link);
                    String fileName = Paths.get(thisPage.getMyURL().toString()).getFileName().toString();
                    File fToWrite = new File(htmlFolder.getAbsolutePath() + File.separator + fileName);
                    String pageContent = null;
                    if (fToWrite.exists()) {
                        pageContent = Files.toString(fToWrite, Charsets.UTF_8);
                    } else {
                        pageContent = thisPage.getContent();
                        Files.write(pageContent, fToWrite, Charsets.UTF_8);
                    }

                    Jerry insideDocument = Jerry.jerry(pageContent);
                    Jerry insideJerry = insideDocument.$("tr");

                    for (Jerry trJerry : insideJerry) {
                        Jerry thJerry = trJerry.$("th");
                        String title = thJerry.text();
                        if (title == null) {
                            continue;
                        }
                        title = title.trim();
                        if (title.toLowerCase().equals("genre")) {
                            Jerry tdJerry = trJerry.$("td");
                            String value = tdJerry.text();
                            if (value == null) {
                                continue;
                            }
                            value = value.trim();
                            writer.append(fileName.replaceAll("\\..*", ""));
                            writer.append("\t").append(value).append("\n");
                        }
//                        System.out.println(title);
                    }

//                    break;
//                    System.out.println(link);
//                    System.out.println(fileName);
//                    System.out.println();
                }

                writer.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
