package eu.fbk.dh.simpatico.ner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.simpatico.wikipediasimp.WikipediaText;
import eu.fbk.utils.core.CommandLine;
import org.fbk.cit.hlt.thewikimachine.ExtractorParameters;
import org.fbk.cit.hlt.thewikimachine.xmldump.AbstractWikipediaExtractor;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 15/07/16.
 */

public class CollectTraining extends AbstractWikipediaExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectTraining.class);
    private static Pattern LINK_PATTERN = Pattern.compile("<a href=\"([^\"]+)\">([^<>]*)</a>");
    private static Pattern WIKI_PATTERN = Pattern.compile("http://www\\.mywiki\\.com/wiki/(.*)");
    private static Set<String> properNounPos = new HashSet<>();
    private static int MIN_SENT_SIZE = 15;

    private static int DEFAULT_THREADS = 1;
    private static double DEFAULT_THRESHOLD = 0.2;

    private BufferedWriter writer;

    private HashSet<String> allowed = new HashSet<>();
    private HashSet<String> per = new HashSet<>();
    private HashSet<String> org = new HashSet<>();
    private HashSet<String> loc = new HashSet<>();

    static {
        properNounPos.add("SP");
    }

    private Properties props;
    private double threshold;

    public CollectTraining(int numThreads, int numPages, Locale locale, String posModel) {
        super(numThreads, numPages, locale);

        props = new Properties();
        props.setProperty("annotators", "ita_toksent, pos");
        props.setProperty("pos.model", posModel);
        props.setProperty("customAnnotatorClass.ita_toksent",
                "eu.fbk.dkm.pikes.tintop.ita.annotators.ItalianTokenizerAnnotator");
        props.setProperty("ita_toksent.newlineIsSentenceBreak", "1");

    }

    public static void main(String[] args) throws IOException {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./collect-training")
                    .withHeader("Collect training data for Italian NER")
                    .withOption("d", "dump", "Wikipedia XML dump", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("a", "airpedia", "Airpedia CSV file", "FILE",
                            CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withOption("p", "posmodel", "POS Italian model", "FILE",
                            CommandLine.Type.FILE, true, false, true)
                    .withOption("t", "threshold",
                            String.format("Threshold for random including non-annotated sentences, default %f",
                                    DEFAULT_THRESHOLD), "NUM",
                            CommandLine.Type.FLOAT, true, false, false)
                    .withOption("n", "threads", String.format("Number of threads, default %d", DEFAULT_THREADS), "NUM",
                            CommandLine.Type.INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

//            String xmlDump = "/Users/alessio/Desktop/itwiki-20150121-pages-articles.xml";
//            String airpediaFile = "/Users/alessio/Desktop/it.csv";
//            String outputFile = "/Users/alessio/Desktop/it-ner.txt";
//            String posModel = "/Users/alessio/Documents/Resources/ita-models/italian5.tagger";
            String xmlDump = cmd.getOptionValue("dump", String.class);
            String airpediaFile = cmd.getOptionValue("airpedia", String.class);
            String outputFile = cmd.getOptionValue("output", String.class);
            String posModel = cmd.getOptionValue("posmodel", String.class);

            int threads = cmd.getOptionValue("threads", Integer.class, DEFAULT_THREADS);
            double threshold = cmd.getOptionValue("threshold", Double.class, DEFAULT_THRESHOLD);

            CollectTraining collectTraining = new CollectTraining(threads, Integer.MAX_VALUE, Locale.ITALIAN, posModel);
            collectTraining.start(xmlDump, airpediaFile, outputFile, threshold);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    public void start(String xmlFile, String airpediaFile, String outputfile, double threshold) {

        StanfordCoreNLP ITApipeline = new StanfordCoreNLP(props);
        this.threshold = threshold;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(airpediaFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                String page = parts[0];
                HashSet<String> info = new HashSet<>();
                for (int i = 1; i < parts.length; i++) {
                    info.add(parts[i]);
                }

                if (info.contains("Place")) {
                    loc.add(page);
                    allowed.add(page);
                }
                if (info.contains("Person")) {
                    per.add(page);
                    allowed.add(page);
                }
                if (info.contains("Organisation")) {
                    org.add(page);
                    allowed.add(page);
                }
            }
            reader.close();

            writer = new BufferedWriter(new FileWriter(outputfile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        startProcess(xmlFile);
    }

    @Override public void start(ExtractorParameters extractorParameters) {

    }

    @Override public void endProcess() {
        try {
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void disambiguationPage(String s, String s1, int i) {

    }

    @Override public void categoryPage(String s, String s1, int i) {

    }

    @Override public void templatePage(String s, String s1, int i) {

    }

    @Override public void redirectPage(String s, String s1, int i) {

    }

    @Override public void contentPage(String s, String s1, int i) {

        String wikiText = s;
        String title = s1;

        WikipediaText wikipediaText = new WikipediaText();

        Whitelist whitelist = Whitelist.none();
        whitelist.addAttributes("a", "href");

        String rawText = wikipediaText.parse(wikiText.toString(), whitelist);

        Map<Integer, String> pageLinks = new LinkedHashMap<>();
        Map<Integer, String> pageTypes = new LinkedHashMap<>();
        Map<Integer, Integer> linkEnds = new LinkedHashMap<>();

        Matcher matcher = LINK_PATTERN.matcher(rawText);
        StringBuilder builder = new StringBuilder();
        int linkOffset = 0;
        int startOffset = 0;
        while (matcher.find()) {
            Matcher wikiMatcher = WIKI_PATTERN.matcher(matcher.group(1));
            String text = matcher.group(2);
            if (wikiMatcher.find()) {
                String link = wikiMatcher.group(1);
                if (allowed.contains(link)) {
                    int start = matcher.start() - linkOffset;
                    pageLinks.put(start, link);
                    linkEnds.put(start, start + text.length());

                    if (per.contains(link)) {
                        pageTypes.put(start, "PER");
                    } else if (org.contains(link)) {
                        pageTypes.put(start, "ORG");
                    } else if (loc.contains(link)) {
                        pageTypes.put(start, "LOC");
                    } else {
                        LOGGER.error("This should not happen...");
                    }
                }
            }
            linkOffset += matcher.end() - matcher.start() - text.length();
            builder.append(rawText.substring(startOffset, matcher.start())).append(text);
            startOffset = matcher.end();
        }

        Set<Integer> okOffsets = new HashSet<>();
        for (Integer offset : pageLinks.keySet()) {
            for (int j = offset; j < linkEnds.get(offset); j++) {
                okOffsets.add(j);
            }
        }

        rawText = builder.toString();

        Annotation annotation = new Annotation(rawText);
        StanfordCoreNLP ITApipeline = new StanfordCoreNLP(props);
        ITApipeline.annotate(annotation);

//        System.out.println(okOffsets);
//        System.out.println(pageLinks);
//        System.out.println(pageTypes);
//        System.out.println(linkEnds);

//            System.out.println("### " + title);
        List<CoreMap> sents = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap thisSent : sents) {
            boolean ok = true;
            int size = thisSent.get(CoreAnnotations.TokensAnnotation.class).size();
            if (size < MIN_SENT_SIZE) {
                continue;
            }

            boolean hasAtLeastOne = false;
            List<CoreLabel> get = thisSent.get(CoreAnnotations.TokensAnnotation.class);
            for (int i1 = 0; i1 < get.size(); i1++) {
                CoreLabel token = get.get(i1);
                int begin = token.beginPosition();
//                    System.out.println(token + " --- " + begin);
                if (okOffsets.contains(begin)) {
                    hasAtLeastOne = true;
                    continue;
                }

                if (i1 == 0) {
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                    if (!properNounPos.contains(pos)) {
                        continue;
                    }
                }

                String form = token.originalText();
                if (Character.isUpperCase(form.charAt(0))) {
                    ok = false;
                }
            }

            if (ok) {

                boolean doIt = true;
                if (!hasAtLeastOne) {
                    double v = Math.random();
                    if (v > threshold) {
                        doIt = false;
                    }
                }
                if (doIt) {
                    synchronized (this) {
                        try {
                            List<CoreLabel> tokens = thisSent.get(CoreAnnotations.TokensAnnotation.class);
                            Integer isInNer = null;
                            for (CoreLabel token : tokens) {
                                String ner = "O";
                                int start = token.beginPosition();
                                int end = token.endPosition();
                                if (pageLinks.containsKey(start)) {
                                    ner = pageTypes.get(start);
                                    isInNer = start;
                                } else {
                                    if (isInNer != null) {
                                        if (end > linkEnds.get(isInNer)) {
                                            isInNer = null;
                                        } else {
                                            ner = pageTypes.get(isInNer);
                                        }
                                    }
                                }

                                writer.append(token.originalText());
                                writer.append("\t").append(ner).append("\n");
                            }
                            writer.append("\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
//                System.out.println("--- " + thisSent.get(CoreAnnotations.TextAnnotation.class));
//                System.out.println("----- " + ok);
//                System.out.println();
        }
//            System.out.println();

//        System.out.println(pageLinks);

    }

    @Override public void portalPage(String s, String s1, int i) {

    }

    @Override public void projectPage(String s, String s1, int i) {

    }

    @Override public void filePage(String s, String s1, int i) {

    }
}
