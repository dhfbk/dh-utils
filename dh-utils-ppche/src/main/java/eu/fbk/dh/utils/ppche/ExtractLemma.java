package eu.fbk.dh.utils.ppche;

import com.google.common.base.Charsets;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.Tree;
import eu.fbk.utils.core.CommandLine;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.lucene.util.IOUtils.close;

public class ExtractLemma {

    //    static Pattern hwPattern = Pattern.compile("\\(HEADWORD ([^)]+)\\)");
//    static Pattern orthoPattern = Pattern.compile("\\(ORTHO ([^)]+)\\)");
    static Pattern numericPattern = Pattern.compile("^[0-9]+$");
    static Pattern comPattern = Pattern.compile("^\\{com:[A-Za-z0-9_']+\\}$");
    static Set<String> skipPos = new HashSet<>();
    static Integer maxMultiwordSize = 5;

    static {
        skipPos.add("ID");
        skipPos.add("CODE");
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-lemma")
                    .withHeader("Extract lemma for lemmatizer")
                    .withOption("l", "input-lemma", "Input folder with lemmas", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("p", "input-pos", "Input folder with pos tags", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("u", "output-underscore", "Output file for underscore", "FILE", CommandLine.Type.FILE, true, false, false)
                    .withOption("t", "output-train", "Output file for POS training", "FILE", CommandLine.Type.FILE, true, false, false)
                    .withOption("r", "replacements", "Replacements file", "FILE", CommandLine.Type.FILE, true, false, false)
                    .withOption("e", "examples", "Examples file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputLemma = cmd.getOptionValue("input-lemma", File.class);
            File inputPos = cmd.getOptionValue("input-pos", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            File outputUnderscoreFile = cmd.getOptionValue("output-underscore", File.class);
            File outputTrainFile = cmd.getOptionValue("output-train", File.class);
            File replacementsFile = cmd.getOptionValue("replacements", File.class);
            File examplesFile = cmd.getOptionValue("examples", File.class);

            BufferedWriter uWriter = null;
            if (outputUnderscoreFile != null) {
                uWriter = new BufferedWriter(new FileWriter(outputUnderscoreFile));
            }
            BufferedWriter pWriter = null;
            if (outputTrainFile != null) {
                pWriter = new BufferedWriter(new FileWriter(outputTrainFile));
            }

            Set<String> globalPosSet = new HashSet<>();

            Map<String, String> posReplaceMap = new HashMap<>();
//            int total = 0, first = 0, noFirst = 0;

            for (String line : java.nio.file.Files.readAllLines(examplesFile.toPath(), Charsets.UTF_8)) {
                line = line.trim();
                if (!line.startsWith("###")) {
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    continue;
                }

                if (skipPos.contains(parts[1])) {
                    continue;
                }

//                total++;

                if (parts.length > 2) {
                    posReplaceMap.put(parts[1], parts[2]);

//                    String[] plusParts = parts[1].split("\\+");
//                    if (plusParts[0].equals(parts[2])) {
//                        first++;
//                    } else {
//                        noFirst++;
//                    }
                }
            }

            System.out.println(posReplaceMap);

            Set<String> mergeSet = new HashSet<>();
            Map<String, String> replaceMap = new HashMap<>();
            if (replacementsFile != null) {
                BufferedReader rReader = new BufferedReader(new FileReader(replacementsFile));
                String line;
                while ((line = rReader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    if (line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split("\t");
                    if (parts.length == 1) {
                        mergeSet.add(parts[0].toLowerCase());
                    } else {
                        replaceMap.put(parts[0], parts[1]);
                    }

                }
                rReader.close();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            for (File file : inputLemma.listFiles()) {

                System.out.println("*** " + file.getAbsolutePath());

                File posFile = new File(inputPos.getAbsolutePath() + File.separator + file.getName().replace(".psd", ".pos"));
                if (!posFile.exists()) {
                    System.out.println("ERROR: File " + posFile.getName() + " does not exist");
                    continue;
                }

                HashMultimap posMap = HashMultimap.create();

                String posString = Files.toString(posFile, Charsets.UTF_8);
                String[] sentences = posString.split("\n\n");
                for (String sentence : sentences) {

                    Queue<String> tokenQueue = EvictingQueue.create(maxMultiwordSize);
                    Queue<String> posQueue = EvictingQueue.create(maxMultiwordSize);

                    String[] words = sentence.split("\n");
                    for (String word : words) {
                        String[] parts = word.split("/");
                        if (parts.length < 2) {
                            continue;
                        }
                        if (skipPos.contains(parts[1])) {
                            continue;
                        }

                        String token = parts[0];
                        String pos = parts[1];

                        Matcher comMatcher = comPattern.matcher(parts[0].toLowerCase());
                        if (token.contains("_") && !comMatcher.find()) {
                            token = token.replace('_', '-');
                        }

                        tokenQueue.add(token.toLowerCase());
                        posQueue.add(pos);

//                        System.out.println(word);

                        for (int i = 0; i < tokenQueue.size() - 1; i++) {
                            for (int j = i + 1; j < tokenQueue.size(); j++) {
                                String tokens = String.join("_", Arrays.copyOfRange(tokenQueue.toArray(new String[0]), i, j));
                                String poss = String.join("_", Arrays.copyOfRange(posQueue.toArray(new String[0]), i, j));
                                posMap.put(tokens, poss);
                            }
                        }
                    }
//                    System.out.println();
                }

                writer.append("### ").append(file.getName()).append("\n");

//                Set<String> mergeSet = new HashSet<>();
//                mergeSet.add("to_day");
//                mergeSet.add("to_morrow");
//                mergeSet.add("my_self");
//                mergeSet.add("it_self");
//                mergeSet.add("your_self");
//                mergeSet.add("your_selves");
//                mergeSet.add("our_selves");
//                mergeSet.add("her_self");
//                mergeSet.add("thy_self");
//                mergeSet.add("him_self");
//                mergeSet.add("mean_while");
//                mergeSet.add("under_ground");
//                mergeSet.add("good_bye");
//                mergeSet.add("with_out");
//                mergeSet.add("now_a_days");
//                mergeSet.add("head_quarters");
//                mergeSet.add("to_night");
//                mergeSet.add("may_be");
//                mergeSet.add("a_cross");

                BufferedReader br = new BufferedReader(new FileReader(file));
                PennTreeReader reader = new PennTreeReader(br);
                Tree tree;
                while ((tree = reader.readTree()) != null) {
                    StringBuffer posTrainBuffer = new StringBuffer();
                    leaves:
                    for (Tree leaf : tree.getLeaves()) {
                        try {
                            Tree parent = leaf.parent(tree);
                            if (parent.label().value().equals("ORTHO")) {
                                Tree base = parent.parent(tree);
                                String pos = base.label().value();
                                String token = leaf.label().value();
                                String lemma = base.getLeaves().get(1).label().value();

                                String lToken = token.toLowerCase();
                                Matcher comMatcher = comPattern.matcher(lToken);
                                if (comMatcher.find()) {
                                    continue;
                                }

                                if (lToken.contains("_")) {

                                    if (uWriter != null) {
                                        uWriter.append(lToken).append("\n");
                                    }

                                    if (replaceMap.containsKey(lToken)) {
                                        lToken = replaceMap.get(lToken);
                                    }

//                                    Matcher numericMatcher = numericPattern.matcher(lemma);
//                                    if (numericMatcher.find()) {
//                                        String[] parts = token.split("_");
//                                        for (String part : parts) {
//                                            writer.append(part).append("\t").append(pos).append("\t").append(part).append("\n");
//                                        }
//                                        continue;
//                                    }

                                    if (mergeSet.contains(lToken)) {
                                        token = token.charAt(0) + lemma.substring(1);
                                    } else {
                                        Set<String> posSet = posMap.get(lToken);
                                        if (posSet == null) {
                                            System.out.println("WARNING: set for " + lToken + " is null");
                                        } else {
                                            String[] tokenParts = lToken.split("_");

                                            // todo: get frequencies
                                            String[] posParts = posSet.stream().findFirst().get().split("_");

                                            for (int i = 0; i < tokenParts.length; i++) {
                                                String tokenPart = tokenParts[i];
                                                String posPart = posParts[i];

                                                if (posReplaceMap.containsKey(posPart)) {
                                                    posPart = posReplaceMap.get(posPart);
                                                }
                                                posPart = posPart.replaceAll("[0-9]+$", "");
                                                posPart = posPart.replaceAll("-$", "");

                                                writer.append(tokenPart).append("\t").append(posPart).append("\t").append(tokenPart).append("\n");
                                                posTrainBuffer.append(tokenPart.replace('/', '-')).append("/").append(posPart).append(" ");

                                                globalPosSet.add(posPart);
                                                continue leaves;
                                            }

                                        }
                                    }

                                }

                                if (posReplaceMap.containsKey(pos)) {
                                    pos = posReplaceMap.get(pos);
                                }
                                pos = pos.replaceAll("[0-9]+$", "");
                                pos = pos.replaceAll("-$", "");

                                writer.append(token).append("\t").append(pos).append("\t").append(lemma).append("\n");
                                posTrainBuffer.append(token.replace('/', '-')).append("/").append(pos).append(" ");

                                globalPosSet.add(pos);
                            }
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                    writer.append("\n");
                    pWriter.append(posTrainBuffer.toString().trim()).append("\n");
                }
                br.close();
            }
            writer.close();

            if (uWriter != null) {
                uWriter.close();
            }
            if (pWriter != null) {
                pWriter.close();
            }

            System.out.println("POS size: " + globalPosSet.size());

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
