package eu.fbk.dh.utils.ff;

import com.google.common.base.Charsets;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.eval.PrecisionRecall;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class Statistics {

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./statistics")
                    .withHeader("Extract statistics")
                    .withOption("i", "input-test", "Input classified file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("g", "input-gold", "Input gold file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
//                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputClassifiedFile = cmd.getOptionValue("input-test", File.class);
            File inputGoldFile = cmd.getOptionValue("input-gold", File.class);
//            File outputFile = cmd.getOptionValue("output", File.class);

            List<String> goldLines = Files.readAllLines(inputGoldFile.toPath(), Charsets.UTF_8);
            List<String> classLines = Files.readAllLines(inputClassifiedFile.toPath(), Charsets.UTF_8);

            if (goldLines.size() != classLines.size()) {
                System.err.println("Error in size");
                System.exit(1);
            }

            int tp = 0, fp = 0, tn = 0, fn = 0;
            for (int i = 0; i < goldLines.size(); i++) {
                String goldLine = goldLines.get(i).replaceAll("\\s+.*", "");
                String classLine = classLines.get(i).replaceAll("\\s+.*", "");

                if (goldLine.equals("co")) {
                    goldLine = "0";
                }
                if (goldLine.equals("ff")) {
                    goldLine = "1";
                }

                if (goldLine.equals("0")) {
                    if (goldLine.equals(classLine)) {
                        tn++;
                    } else {
                        fp++;
                    }
                } else {
                    if (goldLine.equals(classLine)) {
                        tp++;
                    } else {
                        fn++;
                    }
                }
            }

//            System.out.println(tp);
//            System.out.println(fp);
//            System.out.println(tn);
//            System.out.println(fn);

            PrecisionRecall precisionRecall = PrecisionRecall.forCounts(tp, fp, fn, tn);
            System.out.println("Precision: " +precisionRecall.getPrecision());
            System.out.println("Recall: " +precisionRecall.getRecall());
            System.out.println("F1: " +precisionRecall.getF1());
            System.out.println("Accuracy: " +precisionRecall.getAccuracy());

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
