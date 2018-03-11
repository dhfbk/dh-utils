package eu.fbk.dh.utils.iprase;

import eu.fbk.utils.core.CommandLine;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SplitFiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplitFiles.class);

    static void shuffleArray(File[] ar) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            File a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./split-files")
                    .withHeader(
                            "Split files for annotators")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FILE", CommandLine.Type.DIRECTORY, true, false, true)
                    .withOption(null, "annotators", "Number of annotators", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, true)
                    .withOption(null, "seniors", "Number of annotators", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, true)
                    .withOption(null, "senior-articles", "Number of annotators", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, true)
                    .withOption(null, "pairs-for-agreement", "Number of annotators", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, true)
                    .withOption(null, "agreement-articles", "Number of annotators", "NUM", CommandLine.Type.NON_NEGATIVE_INTEGER, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            Integer annotators = cmd.getOptionValue("annotators", Integer.class);
            Integer seniors = cmd.getOptionValue("seniors", Integer.class);
            Integer seniorArticles = cmd.getOptionValue("senior-articles", Integer.class);
            Integer pairs = cmd.getOptionValue("pairs-for-agreement", Integer.class);
            Integer agreementArticles = cmd.getOptionValue("agreement-articles", Integer.class);

//            int annotators = 17;
//            int smallAnnotators = 2;
//            int smallAmount = 30;
//            int pairs = 3;
//            int amountTest = 10;

            if (outputFolder.exists()) {
                FileUtils.deleteDirectory(outputFolder);
            }
            outputFolder.mkdirs();

            int fileIndex = 0;

            File[] files = inputFolder.listFiles();
            shuffleArray(files);

            for (int i = 0; i < seniors; i++) {
                String folderName = "senior" + (i + 1);
                File thisOutFolder = new File(outputFolder.getAbsolutePath() + File.separator + folderName);
                thisOutFolder.mkdirs();

                for (int j = 0; j < seniorArticles; j++) {
                    File fileToCopy = files[fileIndex];
                    File thisOutFile = new File(thisOutFolder + File.separator + fileToCopy.getName());
                    FileUtils.copyFile(fileToCopy, thisOutFile);

                    fileIndex++;
                }
            }

            int fileLeft = files.length - fileIndex; // leave before commonGroups

            File[][] commonGroups = new File[pairs * 2][agreementArticles];
            for (int i = 0; i < pairs; i++) {
                for (int j = 0; j < agreementArticles; j++) {
                    commonGroups[2 * i][j] = files[fileIndex];
                    commonGroups[2 * i + 1][j] = files[fileIndex];
                    fileIndex++;
                }
            }

            int[] split = new int[annotators];
            for (int i = 0; i < annotators; i++) {
                split[i] = 0;
            }
            fileLeft += pairs * agreementArticles;

            for (int i = 0; i < fileLeft; i++) {
                split[i % annotators]++;
            }

            for (int i = 0; i < annotators; i++) {
                String folderName = "annotator" + (i + 1);
                File thisOutFolder = new File(outputFolder.getAbsolutePath() + File.separator + folderName);
                thisOutFolder.mkdirs();

                int start = 0;
                if (i < (pairs * 2)) {
                    for (File file : commonGroups[i]) {
                        File thisOutFile = new File(thisOutFolder + File.separator + file.getName());
                        FileUtils.copyFile(file, thisOutFile);
                    }

                    start = agreementArticles;
                }

                for (int j = start; j < split[i]; j++) {
                    File fileToCopy = files[fileIndex];
                    File thisOutFile = new File(thisOutFolder + File.separator + fileToCopy.getName());
                    FileUtils.copyFile(fileToCopy, thisOutFile);

                    fileIndex++;
                }
            }

            LOGGER.info("Expected files: {}", files.length);
            LOGGER.info("Actual files: {}", fileIndex);

        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
