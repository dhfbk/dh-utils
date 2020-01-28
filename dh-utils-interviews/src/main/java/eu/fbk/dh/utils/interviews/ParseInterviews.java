package eu.fbk.dh.utils.interviews;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.verb.VerbAnnotations;
import eu.fbk.dh.tint.verb.VerbMultiToken;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class ParseInterviews {

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./parse-interviews")
                    .withHeader("Parse interviews")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
//                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);

            File outputFolder = new File(inputFolder.getAbsolutePath() + File.separator + "out");
            File statsFile = new File(outputFolder.getAbsolutePath() + File.separator + "_stats.tsv");

            Set<String> moods = new HashSet<>();
            moods.add("Ind");
            moods.add("Cnd");
            moods.add("Conj");

            BufferedWriter writer = new BufferedWriter(new FileWriter(statsFile));

            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ner, readability, ita_verb");
            pipeline.load();
//            File outputFile = cmd.getOptionValue("output", File.class);

            for (File file : inputFolder.listFiles()) {
                if (file.isDirectory()) {
                    continue;
                }
                if (file.getName().startsWith(".")) {
                    continue;
                }

                writer.append(file.getName()).append("\t");

                System.out.println(file.getAbsolutePath());

                String content = Files.toString(file, Charsets.UTF_8);

                Integer talks = 0;
                String[] parts = content.split("\n");
                for (String part : parts) {
                    part = part.trim();
                    if (part.length() == 0) {
                        continue;
                    }
                    talks++;
                }


                Annotation json = pipeline.runRaw(content);

                writer.append(Integer.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getTokenCount())).append("\t");
                writer.append(Integer.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getWordCount())).append("\t");
                writer.append(Integer.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getContentWordSize())).append("\t");
                writer.append(Double.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getMeasures().get("main"))).append("\t");
                writer.append(Double.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getMeasures().get("level1"))).append("\t");
                writer.append(Double.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getMeasures().get("level2"))).append("\t");
                writer.append(Double.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getMeasures().get("level3"))).append("\t");
                writer.append(Double.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getTtrValue())).append("\t");
                writer.append(Double.toString(json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getDensity())).append("\t");
                writer.append(talks.toString()).append("\t");

                Double count = 0.0d;
                count = (json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getGenericPosStats().get("S") +
                        json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getGenericPosStats().get("V")) * 1.0 /
                        (json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getGenericPosStats().get("A") +
                                json.get(ReadabilityAnnotations.ReadabilityAnnotation.class).getGenericPosStats().get("B"));

                writer.append(Double.toString(count)).append("\n");

                File verbOutputFile = new File(outputFolder.getAbsolutePath() + File.separator + file.getName() + ".verb.tsv");
                BufferedWriter verbWriter = new BufferedWriter(new FileWriter(verbOutputFile));
                for (CoreMap sentence : json.get(CoreAnnotations.SentencesAnnotation.class)) {
                    for (VerbMultiToken verbMultiToken : sentence.get(VerbAnnotations.VerbsAnnotation.class)) {
                        if (moods.contains(verbMultiToken.getMood())) {
                            verbWriter.append(verbMultiToken.getString());
                            verbWriter.append("\t").append(Boolean.toString(verbMultiToken.isPassive()));
                            verbWriter.append("\t").append(verbMultiToken.getMood());
                            verbWriter.append("\t").append(verbMultiToken.getTense());
                            verbWriter.append("\t").append(verbMultiToken.getPerson().toString());
                            verbWriter.append("\t").append(verbMultiToken.getNumber());
                            verbWriter.append("\t").append(verbMultiToken.getGender());
                            verbWriter.append("\n");
                        }
                    }

                }

                verbWriter.close();

                File outputFile = new File(outputFolder.getAbsolutePath() + File.separator + file.getName() + ".json");
                Files.write(JSONOutputter.jsonPrint(json), outputFile, Charsets.UTF_8);
            }

            writer.close();

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
