package eu.fbk.dh.utils.covid;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.corenlp.outputters.JSONOutputter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


public class RunTint {

    static SimpleDateFormat completeDateFormat = new SimpleDateFormat("yyyyMMdd-hh.mm.ss.SSS");
    static SimpleDateFormat dateOnlyDateFormat = new SimpleDateFormat("yyyyMMdd");
    static Integer MIN_CHARS = 4;

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./run-tint")
                    .withHeader("Run Tint analysis for Covid")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("c", "columns", "Columns", "STRING", CommandLine.Type.STRING, true, false, true)
                    .withOption("o", "output", "Output folder", "FOLDER", CommandLine.Type.DIRECTORY, true, false, true)
                    .withOption(null, "min-chars", "Minimum number of characters for considering the cell (default " + MIN_CHARS + ")", "NUM", CommandLine.Type.INTEGER, true, false, false)
                    .withOption(null, "include-first-row", "Include first row of the file")
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            String columnsString = cmd.getOptionValue("columns", String.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            Integer minChars = cmd.getOptionValue("min-chars", Integer.class, MIN_CHARS);
            Boolean skipFirstRow = !cmd.getOptionValue("include-first-row", Boolean.class, false);

            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            Properties tintProperties = new Properties();
            tintProperties.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma, readability, mood");
            tintProperties.setProperty("customAnnotatorClass.mood", "eu.fbk.fcw.depechemood.DepecheMoodAnnotator");
            tintProperties.setProperty("mood.language", "it");

            TintPipeline pipeline = new TintPipeline(true);
            pipeline.addProperties(tintProperties);
            pipeline.load();

            String[] parts = columnsString.trim().split("\\s+");
            Set<String> columns = new HashSet<>();
            for (String part : parts) {
                columns.add(part.toUpperCase());
            }

            FileInputStream fis = new FileInputStream(inputFile);
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);

            for (int i = mySheet.getFirstRowNum() + (skipFirstRow ? 1 : 0); i <= mySheet.getLastRowNum(); i++) {
                XSSFRow row = mySheet.getRow(i);
                Date formDate = row.getCell(0).getDateCellValue();
                String dateTime = completeDateFormat.format(formDate);
                String date = dateOnlyDateFormat.format(formDate);
                File thisFolder = new File(outputFolder.getAbsolutePath() + File.separator + date);
                if (!thisFolder.exists()) {
                    thisFolder.mkdir();
                }

                for (Cell cell : row) {
                    CellReference ref = new CellReference(cell);
                    String cellLetter = ref.getCellRefParts()[2];
                    if (!columns.contains(cellLetter)) {
                        continue;
                    }
                    String text = cell.getStringCellValue();
                    String rawText = text.replaceAll("[^A-Za-z]", "");
                    if (rawText.length() < minChars) {
                        continue;
                    }
                    String cellNumber = ref.getCellRefParts()[1];
                    cellNumber = Strings.padStart(cellNumber, 5, '0');
                    String simpleName = cellNumber + "_" + dateTime + "_" + cellLetter + ".json";
                    File thisFile = new File(thisFolder.getAbsolutePath() + File.separator + simpleName);
                    System.out.println(thisFile.getName());
                    System.out.println(text);
                    try {
                        Annotation annotation = pipeline.runRaw(text);
                        Files.write(JSONOutputter.jsonPrint(annotation), thisFile, Charsets.UTF_8);
                    }
                    catch (Exception e) {
                        // ignored
                    }
                }

            }
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
