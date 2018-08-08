package eu.fbk.dh.utils.ff;

import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.eval.ConfusionMatrix;
import eu.fbk.utils.eval.PrecisionRecall;
import eu.fbk.utils.svm.Classifier;
import eu.fbk.utils.svm.LabelledVector;
import eu.fbk.utils.svm.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alessio on 26/04/17.
 */

public class Experiments {

    private static final Logger LOGGER = LoggerFactory.getLogger(Experiments.class);
    private static final int SKID_ID = 1;

    /*
        - Type
            0: linear
            1: polinomial
            2: RBF
        - Number of labels
        - c
        - gamma
    */

    public static void main(String[] args) {
        try {

            final CommandLine cmd = CommandLine.parser().withName("experiment-list")
                    .withOption("i", "vectors", "Input file with vectors", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("e", "experiments", "Input file with experiments", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("t", "test", "Input file with gold vectors", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File outputFile = cmd.getOptionValue("output", File.class);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            File testFile = cmd.getOptionValue("test", File.class);

            File trainFile = cmd.getOptionValue("vectors", File.class);

//            HashMap<String, LabelledVector> vectorIndex = new HashMap<>();
            List<LabelledVector> trainingVectors = loadFromFile(trainFile);
            List<LabelledVector> testVectors = null;
            if (testFile != null) {
                testVectors = loadFromFile(testFile);
            }

            File experimentsFile = cmd.getOptionValue("experiments", File.class);
            List<String> configLines = Files.readAllLines(experimentsFile.toPath());
            for (int i1 = 0; i1 < configLines.size(); i1++) {
                String configLine = configLines.get(i1);

                System.out.println(String.format("Line %d/%d", i1 + 1, configLines.size()));

                configLine = configLine.trim();
                if (configLine.startsWith("#")) {
                    continue;
                }
                if (configLine.length() == 0) {
                    continue;
                }
                String[] configParts = configLine.split("\\s+");

                Integer numLabels = Integer.parseInt(configParts[1]);

                Float c = null;
                try {
                    c = Float.parseFloat(configParts[2]);
                } catch (Exception e) {
                    // ignored
                }
                Float gamma = null;
                try {
                    gamma = Float.parseFloat(configParts[3]);
                } catch (Exception e) {
                    // ignored
                }

                int numWeights = configParts.length - 4;
                if (numWeights != numLabels) {
                    throw new Exception("Incoherent information about weights");
                }

                float[] weights = new float[numWeights];
                for (int i = 4; i < configParts.length; i++) {
                    weights[i - 4] = Float.parseFloat(configParts[i]);
                }

                Classifier.Parameters parameters;
                String type = configParts[0];
                if (type.equals("0")) {
                    parameters = Classifier.Parameters.forSVMLinearKernel(numLabels, weights, c);
                } else if (type.equals("1")) {
                    parameters = Classifier.Parameters.forSVMPolyKernel(numLabels, weights, c, gamma, null, null);
                } else if (type.equals("2")) {
                    parameters = Classifier.Parameters.forSVMRBFKernel(numLabels, weights, c, gamma);
                } else {
                    throw new Exception("No type specified");
                }

                if (testVectors != null) {
                    Classifier classifier = Classifier.train(parameters, trainingVectors);
                    List<LabelledVector> predict = classifier.predict(true, testVectors);
                    int tp = 0, fp = 0, fn = 0, tn = 0;
                    for (int i = 0; i < predict.size(); i++) {
                        LabelledVector labelledVector = predict.get(i);
                        LabelledVector testVector = testVectors.get(i);
                        if (labelledVector.getLabel() == 1) {
                            if (testVector.getLabel() == 1) {
                                tp++;
                            } else {
                                fp++;
                            }
                        } else {
                            if (testVector.getLabel() == 1) {
                                fn++;
                            } else {
                                tn++;
                            }
                        }
                    }

                    PrecisionRecall precisionRecall = PrecisionRecall.forCounts(tp, fp, fn, tn);
                    writer.append(configLine).append("\n");
                    writer.append("Precision: ").append(Double.toString(precisionRecall.getPrecision())).append("\n");
                    writer.append("Recall: ").append(Double.toString(precisionRecall.getRecall())).append("\n");
                    writer.append("F1: ").append(Double.toString(precisionRecall.getF1())).append("\n");
                    writer.append("Accuracy: ").append(Double.toString(precisionRecall.getAccuracy())).append("\n");
                } else {
                    HashMap<String, Integer> results = new HashMap<>();
                    ConfusionMatrix confusionMatrix = Classifier.crossValidate(parameters, trainingVectors, 10, results);
                    writer.append(configLine).append("\n");
                    writer.append(confusionMatrix.toString()).append("\n");
                    writer.append(Integer.toString(results.size())).append("\n");
                    for (String key : results.keySet()) {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append(key).append("\t");
//                        buffer.append(vectorIndex.get(key).getLabel()).append("\t");
                        buffer.append(results.get(key));
                        writer.append(buffer.toString()).append("\n");
                    }
                }
            }

            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }

    private static List<LabelledVector> loadFromFile(File vectorFile) throws IOException {
        return loadFromFile(vectorFile, null);
    }

    private static List<LabelledVector> loadFromFile(File vectorFile, HashMap<String, LabelledVector> vectorIndex) throws IOException {
        List<String> lines;
        lines = Files.readAllLines(vectorFile.toPath());
        List<LabelledVector> vectors = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split("\\s+");
            final Vector.Builder builder = Vector.builder();

            String id = null;
            if (SKID_ID == 0) {
                id = parts[0];
                builder.setID(id);
            }
            Integer label = Integer.parseInt(parts[1 - SKID_ID]);
            for (int i = 2 - SKID_ID; i < parts.length; i++) {
                String part = parts[i];
                String[] splitted = part.split(":");
                String featName = "feat" + splitted[0];
                Float featValue = Float.parseFloat(splitted[1]);
                builder.set(featName, featValue);
            }
            LabelledVector vector = builder.build().label(label);
            vectors.add(vector);
            if (vectorIndex != null && id != null) {
                vectorIndex.put(id, vector);
            }
        }

        return vectors;
    }
}
