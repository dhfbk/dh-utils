package eu.fbk.dh.utils.tint;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.readability.Readability;
import eu.fbk.dh.tint.readability.ReadabilityAnnotations;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.TintRunner;
import eu.fbk.dh.utils.iprase.annotations.CatAnnotations;
import eu.fbk.dh.utils.iprase.annotations.Task;
import eu.fbk.dh.utils.iprase.utils.IpraseProperties;
import eu.fbk.utils.core.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GenovaExtractor {

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./genova-extractor")
                    .withHeader("Genova extractor")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);

            BufferedWriter writer = null;
            ConcurrentHashMap map = new ConcurrentHashMap();
            List<String> labels = new ArrayList<>();

            if (outputFile != null) {
                writer = new BufferedWriter(new FileWriter(outputFile));
                labels.add("File name");
                labels.add("Token count");
                labels.add("Gulpease");
                labels.add("Level 1");
                labels.add("Level 2");
                labels.add("Level 3");
                labels.add("Proposition avg");
                labels.add("Subordinate ratio");
                labels.add("Ttr value");
                labels.add("Density");
                labels.add("Words avg");
                labels.add("Depth avg");
            }


            TintPipeline pipeline = new TintPipeline();
            pipeline.loadDefaultProperties();
            pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, ita_verb, ita_derivatario, depparse, readability, " +
                    "trivial, affissi, connettivi, congiunzioni, perche_quando, gergale, anglicismi, politicamente_corretto, polirematiche, frasi_scisse" +
                    "");

            pipeline.setProperty("customAnnotatorClass.ita_derivatario", "eu.fbk.dh.tint.derived.DerivationAnnotator");
            pipeline.addProperties(IpraseProperties.properties);
            pipeline.load();

            final AtomicBoolean isLabelsVarEmpty = new AtomicBoolean(true);

            java.nio.file.Files.walk(inputFolder.toPath())
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().endsWith("."))
                    .filter(path -> path.getFileName().toString().endsWith(".txt"))
                    .forEach(path -> {
                        String outFileName = path.toString() + ".json";
                        System.out.println(outFileName);
                        try {
                            String s = Files.toString(path.toFile(), Charsets.UTF_8);
                            OutputStream outputStream = null;
                            outputStream = new FileOutputStream(outFileName);
                            Annotation annotation = pipeline.run(s, outputStream, TintRunner.OutputFormat.JSON);

                            StringWriter stringWriter = new StringWriter();
                            CSVPrinter csvPrinter = new CSVPrinter(stringWriter, CSVFormat.newFormat(';').withRecordSeparator('\n'));

                            Readability readability = annotation.get(ReadabilityAnnotations.ReadabilityAnnotation.class);
                            List<Task> tasks = annotation.get(CatAnnotations.CatTasksAnnotation.class);
                            List<String> row = new ArrayList<>();
                            row.add(path.toFile().getName());
                            row.add(Integer.toString(readability.getTokenCount()));
                            row.add(readability.getMeasures().get("main").toString());
                            row.add(readability.getMeasures().get("level1").toString());
                            row.add(readability.getMeasures().get("level2").toString());
                            row.add(readability.getMeasures().get("level3").toString());
                            row.add(readability.getPropositionsAvg().toString());
                            row.add(readability.getSubordinateRatio().toString());
                            row.add(readability.getTtrValue().toString());
                            row.add(readability.getDensity().toString());
                            row.add(readability.getWordsAvg().toString());
                            row.add(readability.getDeepAvg().toString());

                            for (Task task : tasks) {
                                if (isLabelsVarEmpty.get()) {
                                    labels.add(task.getTaskName());
                                }
                                row.add(Integer.toString(task.getEvents().size()));
                            }
                            isLabelsVarEmpty.set(false);

                            csvPrinter.printRecord(row);
                            map.put(path.toFile().getAbsolutePath(), stringWriter.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

            if (writer != null) {
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.newFormat(';').withRecordSeparator('\n'));
                csvPrinter.printRecord(labels);
                for (Object value : map.values()) {
                    writer.write(value.toString());
                }
                writer.close();
            }
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
