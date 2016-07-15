package eu.fbk.dh.simpatico.ner;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import eu.fbk.dh.simpatico.wikipediasimp.WikipediaText;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 15/07/16.
 */

public class CollectTraining {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectTraining.class);
    private static Pattern LINK_PATTERN = Pattern.compile("<a href=\"([^\"]+)\">([^<>]*)</a>");
    private static Pattern WIKI_PATTERN = Pattern.compile("http://www\\.mywiki\\.com/wiki/(.*)");

    public static void main(String[] args) throws IOException {
        String fileName = "/Users/alessio/Documents/Resources/pantheon/it/example.wiki";
        List<String> lines = Files.readLines(new File(fileName), Charsets.UTF_8);

        Properties props = new Properties();

        StringBuilder wikiText = new StringBuilder();
        for (String line : lines) {
            wikiText.append(line.trim()).append("\n");
        }

        WikipediaText wikipediaText = new WikipediaText();

        Whitelist whitelist = Whitelist.none();
        whitelist.addAttributes("a", "href");

        String rawText = wikipediaText.parse(wikiText.toString(), whitelist);
        Map<Integer, String> pageLinks = new LinkedHashMap<>();

        Matcher matcher = LINK_PATTERN.matcher(rawText);
        StringBuilder builder = new StringBuilder();
        int linkOffset = 0;
        int startOffset = 0;
        while (matcher.find()) {
            Matcher wikiMatcher = WIKI_PATTERN.matcher(matcher.group(1));
            String text = matcher.group(2);
            if (wikiMatcher.find()) {
                pageLinks.put(matcher.start() - linkOffset, wikiMatcher.group(1));
            }
            linkOffset += matcher.end() - matcher.start() - text.length();
            builder.append(rawText.substring(startOffset, matcher.start())).append(text);
            startOffset = matcher.end();
        }

        System.out.println(pageLinks);
        System.out.println(builder.toString());
    }
}
