package eu.fbk.dh.utils.crfsuite;

import eu.fbk.dkm.utils.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConvertTabFile {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConvertTabFile.class);

    static final Integer LAST_CHARS = 5;
    static final Integer MINUS_LAST_CHARS = 5;

    static final String fstanPattern = "%s/bin/fstan/x86_64/fstan %s/models/italian-utf8.fsa";
    static final String DEFAULT_fstanFolder = "/Users/alessio/Documents/scripts/textpro/modules/MorphoPro/";

    static HashMap<String, Pattern> patterns = new HashMap<>();
    private static final Integer DEFAULT_WINDOW_SIZE = 3;
    private static HashSet<String> noWindowFeatures = new HashSet<>();

    static {
        patterns.put("NUMERIC", Pattern.compile("^[0-9]+$"));
//		patterns.put("UPPERCASE", Pattern.compile("^[A-Z]+$"));
//		patterns.put("SUPPERCASE", Pattern.compile("^[A-Z]"));
        noWindowFeatures.add("EOS");
        noWindowFeatures.add("BOS");
        noWindowFeatures.add("LAST");
    }

    private static class Token {
        String form, pos, lemma, out;

        public Token(String form, String pos, String lemma, String out) {
            this.form = form;
            this.pos = pos;
            this.lemma = lemma;
            this.out = out;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "form='" + form + '\'' +
                    ", pos='" + pos + '\'' +
                    ", lemma='" + lemma + '\'' +
                    ", out='" + out + '\'' +
                    '}';
        }

        public String getOut() {
            return out;
        }

        public void setOut(String out) {
            this.out = out;
        }

        public String getForm() {
            return form;
        }

        public void setForm(String form) {
            this.form = form;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public String getLemma() {
            return lemma;
        }

        public void setLemma(String lemma) {
            this.lemma = lemma;
        }
    }

    private static void writeFeatures(BufferedWriter writer, ArrayList<FeatureSet> features, Integer windowSize) throws IOException {
        for (int i = 0; i < features.size(); i++) {
            FeatureSet featureSet = new FeatureSet();
            featureSet.features.addAll(features.get(i).features);
            featureSet.value = features.get(i).value;

            if (i == 0) {
                featureSet.addFeature("[BOS]");
            }
            if (i == features.size() - 1) {
                featureSet.addFeature("[EOS]");
            }

            for (int j = i - windowSize; j <= i + windowSize; j++) {
                if (j == i || j >= features.size() || j < 0) {
                    continue;
                }

                FeatureSet thisFS = features.get(j);
                featureLoop:
                for (String feature : thisFS.features) {
                    for (String noWindowFeature : noWindowFeatures) {
                        if (feature.startsWith(noWindowFeature)) {
                            continue featureLoop;
                        }
                    }

                    featureSet.addFeature("[" + (j - i) + "]" + feature);
                }
            }

            writer.append(featureSet.toString()).append("\n");
        }

        writer.append("\n");
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("wikipedia-text-parser")
                    .withHeader("Analyze text dumps from Wikipedia")
                    .withOption("i", "input", "input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("s", "size", "window size", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            final File inputPath = cmd.getOptionValue("input", File.class);
            final File outputPath = cmd.getOptionValue("output", File.class);

            Integer windowSize = cmd.getOptionValue("size", Integer.class, DEFAULT_WINDOW_SIZE);

            // Parsing of input file
            List<Token> tokens = new ArrayList<>();
            List<String> lines = Files.readAllLines(inputPath.toPath());
            for (String line : lines) {
                line = line.trim();
                if (line.length() == 0) {
                    tokens.add(null);
                    continue;
                }

                String[] parts = line.split("\\s+");

                String form = parts[0];
                String lemma = parts[1];
                String pos = parts[2];

                String out = parts[3];

                Token token = new Token(form, pos, lemma, out);
                tokens.add(token);
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

            ArrayList<FeatureSet> features = new ArrayList<>();
            HashMap<String, Integer> inTokens = new HashMap<>();
            for (int i = 0; i < tokens.size(); i++) {
                Token token = tokens.get(i);

                if (token == null) {
                    // Reset
                    writeFeatures(writer, features, windowSize);
                    features = new ArrayList<>();
                    continue;
                }

                String form = token.getForm();
//				String lform = form.toLowerCase();
                String lemma = token.getLemma();
                String pos = token.getPos();

                FeatureSet set = new FeatureSet();
                set.addFeature("FORM." + form);
                set.addFeature("POS." + pos);
                set.addFeature("LEMMA." + lemma);
//				set.addFeature("LFORM." + lform);

                String firstChar = form.substring(0, 1);

                for (String key : patterns.keySet()) {
                    Pattern pattern = patterns.get(key);
                    Matcher matcher = pattern.matcher(form);
                    set.addFeature(key + "." + (matcher.find() ? "1" : "0"));
                }

                if (firstChar.toUpperCase().equals(firstChar)) {
                    set.addFeature("UPPERCASE1.1");
                } else {
                    set.addFeature("UPPERCASE1.0");
                }

                if (form.toUpperCase().equals(form)) {
                    set.addFeature("UPPERCASE.1");
                } else {
                    set.addFeature("UPPERCASE.0");
                }

                set.value = token.getOut();

                features.add(set);
            }

            writer.close();

        } catch (Throwable ex) {
            CommandLine.fail(ex);
        }
    }

    private static HashMap<String, HashSet<String[]>> loadFromIOBFile(File path, String key) throws IOException {
        HashMap<String, HashSet<String[]>> list = new HashMap<>();
        if (path != null) {
            List<String> lines = Files.readAllLines(path.toPath());
            ArrayList<String> tokens = new ArrayList<>();
            for (String line : lines) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                String[] types = parts[1].split("-");
                if (types.length < 2) {
                    continue;
                }
                if (!types[1].equals(key)) {
                    continue;
                }

                if (types[0].equals("B")) {
                    if (tokens.size() > 0) {
                        if (list.get(tokens.get(0)) == null) {
                            list.put(tokens.get(0), new HashSet<>());
                        }
                        String[] tokensArr = new String[tokens.size()];
                        tokensArr = tokens.toArray(tokensArr);

                        list.get(tokens.get(0)).add(tokensArr);
                    }
                    tokens = new ArrayList<>();
                }

                tokens.add(parts[0]);

//				if (list.get(parts[0]) == null) {
//					list.put(parts[0], new HashSet<>());
//				}
//				list.get(parts[0]).add(parts);
            }

            if (tokens.size() > 0) {
                if (list.get(tokens.get(0)) == null) {
                    list.put(tokens.get(0), new HashSet<>());
                }
                String[] tokensArr = new String[tokens.size()];
                tokensArr = tokens.toArray(tokensArr);

                list.get(tokens.get(0)).add(tokensArr);
            }

            LOGGER.info("{} items loaded", list.size());
        }
        return list;
    }

    private static HashMap<String, HashSet<String[]>> loadFromTextFile(File path) throws IOException {
        HashMap<String, HashSet<String[]>> list = new HashMap<>();
        if (path != null) {
            List<String> lines = Files.readAllLines(path.toPath());
            for (String line : lines) {
                if (line.trim().length() == 0) {
                    continue;
                }
                String[] parts = line.split("\\s+");

                if (list.get(parts[0]) == null) {
                    list.put(parts[0], new HashSet<>());
                }
                list.get(parts[0]).add(parts);
            }
            LOGGER.info("{} items loaded", list.size());
        }
        return list;
    }

}
