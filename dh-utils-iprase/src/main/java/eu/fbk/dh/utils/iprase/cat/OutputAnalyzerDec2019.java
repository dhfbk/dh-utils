package eu.fbk.dh.utils.iprase.cat;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.TreeMultimap;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.FrequencyHashSet;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputAnalyzerDec2019 {

    static Pattern fileNamePattern = Pattern.compile(".*Iprase_([0-9]+)/([^/]+)/([^/]+)/([0-9]+)\\.txt\\.json\\.xml$");
    static Integer spanSize = 5;

    public static Map<String, String> listAllAttributes(Element element) {
        Map<String, String> ret = new HashMap<>();
        NamedNodeMap attributes = element.getAttributes();
        int numAttrs = attributes.getLength();
        for (int i = 0; i < numAttrs; i++) {
            Attr attr = (Attr) attributes.item(i);
            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();
            ret.put(attrName, attrValue);
        }
        return ret;
    }

    public static void parseFolder(File thisFolder, Map<String, Map<String, File>> files, int depth) {
        if (thisFolder.getAbsolutePath().contains("annotatori_rimossi")) {
            return;
        }
        if (!thisFolder.isDirectory()) {
            return;
        }
        if (depth > 10) {
            return;
        }
        for (File contentFile : thisFolder.listFiles()) {
            String fileName = contentFile.getAbsolutePath();
            if (contentFile.isDirectory()) {
                parseFolder(contentFile, files, depth + 1);
            }
            if (contentFile.isFile()) {
                Matcher matcher = fileNamePattern.matcher(fileName);
                if (matcher.matches()) {
                    String task = matcher.group(3);
                    files.putIfAbsent(task, new HashMap<>());

                    int idTemaBase = Integer.parseInt(matcher.group(4));
                    String idTema = Integer.toString(idTemaBase);
                    int prog = 0;
                    while (files.get(task).keySet().contains(idTema)) {
                        idTema = idTemaBase + "_" + ++prog;
                    }
                    files.get(task).put(idTema, contentFile);
                }
            }
        }

    }

    private static void writeLine(Writer writer, String fileName, String taskName, String label, Set<Integer> tokenIDs, Map<Integer, String> tokens, int max) throws IOException {
        writer.append(fileName).append("\t");
        writer.append(taskName).append("\t");
        writer.append(label).append("\t");
        Integer firstToken = Collections.min(tokenIDs);
        Integer lastToken = Collections.max(tokenIDs);
        int start = Math.max(1, firstToken - spanSize);
        int end = Math.min(max, lastToken + spanSize);
        StringBuffer buffer = new StringBuffer();
        for (int i = start; i < end; i++) {
            if (i == firstToken) {
                buffer.append("*** ");
            }
            buffer.append(tokens.get(i)).append(" ");
            if (i == lastToken) {
                buffer.append("*** ");
            }
        }
        writer.append(buffer.toString().trim()).append("\n");
    }

    private static void writeLines(Writer writer, String fileName, String taskName, FrequencyHashSet<String> values,
                                   Map<String, Map<String, FrequencyHashSet<String>>> aggregatedTotals, Map<String, String> years,
                                   Map<String, Map<String, FrequencyHashSet<String>>> typeTotals, Map<String, String> schools,
                                   Map<String, Map<String, Map<String, FrequencyHashSet<String>>>> aggTypeTotals) throws IOException {
        for (String key : values.keySet()) {
            writer.append(fileName).append("\t");
            writer.append(taskName).append("\t");
            writer.append(key).append("\t");
            writer.append(values.get(key).toString()).append("\n");
        }
        boolean y = false;
        boolean s = false;
        if (years.containsKey(fileName)) {
            y = true;
            for (String key : values.keySet()) {
                aggregatedTotals.get(years.get(fileName)).get(taskName).add(key, values.get(key));
            }
        }
        if (schools.containsKey(fileName)) {
            s = true;
            for (String key : values.keySet()) {
                typeTotals.get(schools.get(fileName)).get(taskName).add(key, values.get(key));
            }
        }
        if (s && y) {
            for (String key : values.keySet()) {
                aggTypeTotals.get(schools.get(fileName)).get(years.get(fileName)).get(taskName).add(key, values.get(key));
            }
        }
    }

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./output-analyzer")
                    .withHeader("Analyze the CAT output")
                    .withOption("i", "input", "Input folder", "FILE", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("c", "count", "Output count file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("y", "year", "Year file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("w", "out-year", "Output year statistics file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("s", "out-schools", "Output schools statistics file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            File countFile = cmd.getOptionValue("count", File.class);
            File yearFile = cmd.getOptionValue("year", File.class);
            File outYearFile = cmd.getOptionValue("out-year", File.class);
            File outSchoolsFile = cmd.getOptionValue("out-schools", File.class);

            Set<String> stringsBeforeAfter = new HashSet<>();
            stringsBeforeAfter.add(".");
            stringsBeforeAfter.add(",");
            Set<String> stringsToSkipBeforeAfter = new HashSet<>();
            stringsToSkipBeforeAfter.add("che");
            stringsToSkipBeforeAfter.add("dove");
            stringsToSkipBeforeAfter.add("siccome");

            Set<String> taskToDo = new HashSet<>();
//            taskToDo.add("06_GLI");
//            taskToDo.add("11_INDICATIVO_PRESENTE");
//            taskToDo.add("13_AFFISSI");
//            taskToDo.add("14_FRASI_NOMINALI");
            taskToDo.add("15_CONNETTIVI");
//            taskToDo.add("17_PUNTEGGIATURA");
//            taskToDo.add("18_PERCHE_QUANDO");
//            taskToDo.add("20_ANGLICISMI");
//            taskToDo.add("22_POLIREMATICHE");
//            taskToDo.add("27_LI");

            LinkedHashMap<String, Set<String>> schoolsOrder = new LinkedHashMap<>();
            schoolsOrder.put("classico+scientifico", new HashSet<>());
            schoolsOrder.get("classico+scientifico").add("liceo scientifico");
            schoolsOrder.get("classico+scientifico").add("liceo classico");
            schoolsOrder.put("ling+su+les+tecnici", new HashSet<>());
            schoolsOrder.get("ling+su+les+tecnici").add("altri licei");
            schoolsOrder.get("ling+su+les+tecnici").add("tecnici");
            schoolsOrder.put("istprofessionale", new HashSet<>());
            schoolsOrder.get("istprofessionale").add("istruzione professionale");
            schoolsOrder.put("licei", new HashSet<>());
            schoolsOrder.get("licei").add("liceo scientifico");
            schoolsOrder.get("licei").add("liceo classico");
            schoolsOrder.get("licei").add("altri licei");
            schoolsOrder.put("tecnici", new HashSet<>());
            schoolsOrder.get("tecnici").add("tecnici");

            List<String> yearsOrder = new ArrayList<>();
            yearsOrder.add("2000-01");
            yearsOrder.add("03-04");
            yearsOrder.add("06-07");
            yearsOrder.add("09-10");
            yearsOrder.add("12-13");
            yearsOrder.add("15-16");

            Map<String, String> years = new HashMap<>();
            Map<String, String> schools = new HashMap<>();
            Set<String> schoolTypes = new HashSet<>();
            FrequencyHashSet<String> documentsPerYear = new FrequencyHashSet<>();

            BufferedReader reader = new BufferedReader(new FileReader(yearFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length < 2) {
                    continue;
                }
                years.put(parts[0], parts[1]);
                if (parts.length < 5) {
                    continue;
                }
                String schoolType = parts[4];
                if (schoolType.startsWith("temi")) {
                    continue;
                }
                if (schoolType.trim().length() == 0) {
                    continue;
                }
                schools.put(parts[0], schoolType);
                schoolTypes.add(schoolType);
            }
            reader.close();

//            System.out.println(schoolTypes);
//            System.exit(1);

            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            BufferedWriter countWriter = new BufferedWriter(new FileWriter(countFile));

            Map<String, Integer> tokenCount = new TreeMap<>();

            Map<String, Map<String, File>> files = new HashMap<>();
            parseFolder(inputFolder, files, 0);

            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            XPath xPath = XPathFactory.newInstance().newXPath();

            Map<String, Map<String, FrequencyHashSet<String>>> aggregatedTotals = new TreeMap<>();
            for (String value : years.values()) {
                if (value.trim().length() == 0) {
                    continue;
                }
                aggregatedTotals.putIfAbsent(value, new TreeMap<>());
            }

            Map<String, Map<String, FrequencyHashSet<String>>> typeTotals = new TreeMap<>();
            for (String value : schools.values()) {
                if (value.trim().length() == 0) {
                    continue;
                }
                typeTotals.putIfAbsent(value, new TreeMap<>());
            }

            Map<String, Map<String, Map<String, FrequencyHashSet<String>>>> aggTypeTotals = new TreeMap<>();
            for (String value : schools.values()) {
                if (value.trim().length() == 0) {
                    continue;
                }
                aggTypeTotals.putIfAbsent(value, new TreeMap<>());
                for (String year : years.values()) {
                    if (year.trim().length() == 0) {
                        continue;
                    }
                    aggTypeTotals.get(value).putIfAbsent(year, new TreeMap<>());
                }
            }

            for (String taskName : files.keySet()) {
                if (taskToDo.size() > 0 && !taskToDo.contains(taskName)) {
                    continue;
                }
                for (String year : aggregatedTotals.keySet()) {
                    aggregatedTotals.get(year).putIfAbsent(taskName, new FrequencyHashSet<>());
                }
                for (String schoolType : typeTotals.keySet()) {
                    typeTotals.get(schoolType).putIfAbsent(taskName, new FrequencyHashSet<>());
                }
                for (String schoolType : typeTotals.keySet()) {
                    for (String year : aggregatedTotals.keySet()) {
                        aggTypeTotals.get(schoolType).get(year).putIfAbsent(taskName, new FrequencyHashSet<>());
                    }
                }

                System.out.println(taskName);
                for (String fileName : files.get(taskName).keySet()) {
                    Map<Integer, String> tokens = new TreeMap<>();
                    Map<Integer, Map<String, String>> markablesInfo = new TreeMap<>();
                    Map<Integer, Set<Integer>> markablesTokens = new TreeMap<>();

                    File file = files.get(taskName).get(fileName);
//                    System.out.println(file.getAbsolutePath());
                    int max = 0;

                    Document xmlDocument = builder.parse(file);
                    NodeList nodeList;

                    nodeList = (NodeList) xPath.compile("/Document/token").evaluate(xmlDocument, XPathConstants.NODESET);
                    Integer tCount = nodeList.getLength();
                    tokenCount.putIfAbsent(fileName, tCount);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        String id = node.getAttributes().getNamedItem("t_id").getNodeValue();
                        int idInt = Integer.parseInt(id);
                        String value = node.getTextContent();
                        tokens.put(idInt, value);
                        max = idInt;
                    }

                    nodeList = (NodeList) xPath.compile("/Document/Markables/*").evaluate(xmlDocument, XPathConstants.NODESET);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Set<Integer> tokenList = new HashSet<>();
                        Node node = nodeList.item(i);
                        String id = node.getAttributes().getNamedItem("m_id").getNodeValue();
                        int idInt = Integer.parseInt(id);
                        markablesInfo.put(idInt, listAllAttributes((Element) node));

                        Element eElement = (Element) node;
                        NodeList anchors = eElement.getElementsByTagName("token_anchor");
                        for (int j = 0; j < anchors.getLength(); j++) {
                            tokenList.add(Integer.parseInt(anchors.item(j).getAttributes().getNamedItem("t_id").getNodeValue()));
                        }

                        markablesTokens.put(idInt, tokenList);
                    }

//                    System.out.println(tokens);
//                    System.out.println(markablesInfo);
//                    System.out.println(markablesTokens);

                    FrequencyHashSet<String> totals;
                    switch (taskName) {
                        case "15_CONNETTIVI":
                            totals = new FrequencyHashSet<>();
                            for (Integer id : markablesInfo.keySet()) {
                                totals.add("[totale]");
                                String che_polivalente = markablesInfo.get(id).get("CHE_polivalente");
                                String dove_polivalente = markablesInfo.get(id).get("DOVE_polivalente");
                                String siccome_al_posto_di_poiche = markablesInfo.get(id).get("SICCOME_al_posto_di_poiche");
                                String uso_solo_per_infatti_cioe_allora_dunque_quindi = markablesInfo.get(id).get("Uso_solo_per_INFATTI_CIOE_ALLORA_DUNQUE_QUINDI");

                                Set<Integer> tokenIDs = markablesTokens.get(id);
                                Integer firstToken = Collections.min(tokenIDs);
                                Integer lastToken = Collections.max(tokenIDs);

                                StringBuffer buffer = new StringBuffer();
                                StringBuffer text = new StringBuffer();

                                for (int i = firstToken; i < lastToken + 1; i++) {
                                    text.append(tokens.get(i)).append(" ");
                                }

                                String stringText = text.toString().trim();
                                stringText = stringText.replace(' ', '_');
                                stringText = stringText.toLowerCase();

                                if (stringText.equals("all'_ora")) {
                                    stringText = "allora";
                                }

                                if (!stringText.equals("che")) {
                                    che_polivalente = "";
                                } else {
                                    uso_solo_per_infatti_cioe_allora_dunque_quindi = "";
                                }
                                if (!stringText.equals("dove")) {
                                    dove_polivalente = "";
                                } else {
                                    uso_solo_per_infatti_cioe_allora_dunque_quindi = "";
                                }
                                if (!stringText.equals("siccome")) {
                                    siccome_al_posto_di_poiche = "";
                                } else {
                                    uso_solo_per_infatti_cioe_allora_dunque_quindi = "";
                                }

                                int bufferCount = 0; // Used to remove wrong (duplicated) annotations

                                if (che_polivalente.length() > 0) {
//                                    totals.add("CHE_polivalente");
//                                    buffer.append("CHE_polivalente=").append(che_polivalente).append(" ");
                                    buffer.append(stringText).append("/").append("polivalente").append(" ");
                                    bufferCount++;
                                } else {
                                    if (stringText.equals("che")) {
                                        buffer.append(stringText).append("/").append("corretto").append(" ");
                                        bufferCount++;
                                    }
                                }
                                if (dove_polivalente.length() > 0) {
//                                    totals.add("DOVE_polivalente");
//                                    buffer.append("DOVE_polivalente=").append(dove_polivalente).append(" ");
                                    buffer.append(stringText).append("/").append("polivalente").append(" ");
                                    bufferCount++;
                                } else {
                                    if (stringText.equals("dove")) {
                                        buffer.append(stringText).append("/").append("corretto").append(" ");
                                        bufferCount++;
                                    }
                                }
                                if (siccome_al_posto_di_poiche.length() > 0) {
//                                    totals.add("SICCOME_al_posto_di_poiche");
//                                    buffer.append("SICCOME_al_posto_di_poiche=").append(siccome_al_posto_di_poiche).append(" ");
                                    buffer.append(stringText).append("/").append("scorretto").append(" ");
                                    bufferCount++;
                                } else {
                                    if (stringText.equals("siccome")) {
                                        buffer.append(stringText).append("/").append("corretto").append(" ");
                                        bufferCount++;
                                    }
                                }
                                if (uso_solo_per_infatti_cioe_allora_dunque_quindi.length() > 0) {
//                                    totals.add("Uso_solo_per_INFATTI_CIOE_ALLORA_DUNQUE_QUINDI");
//                                    buffer.append("Uso_solo_per_INFATTI_CIOE_ALLORA_DUNQUE_QUINDI=").append(uso_solo_per_infatti_cioe_allora_dunque_quindi).append(" ");
                                    buffer.append(stringText).append("/").append(uso_solo_per_infatti_cioe_allora_dunque_quindi).append(" ");
                                    bufferCount++;
                                }

                                String before = "";
                                String after = "";

                                try {
                                    String beforeTemp = tokens.get(firstToken - 1);
                                    if (stringsBeforeAfter.contains(beforeTemp)) {
                                        before = beforeTemp;
                                    }
                                } catch (Exception e) {
                                    // ignore
                                }
                                try {
                                    String afterTemp = tokens.get(lastToken + 1);
                                    if (stringsBeforeAfter.contains(afterTemp)) {
                                        after = afterTemp;
                                    }
                                } catch (Exception e) {
                                    // ignore
                                }

                                if (before.length() > 0) {
                                    before = "Prima:" + before;
                                }
                                if (after.length() > 0) {
                                    after = "Dopo:" + after;
                                }
                                if (before.length() > 0 || after.length() > 0) {
                                    if (!stringsToSkipBeforeAfter.contains(stringText)) {
                                        totals.add("--- " + before + "|" + after);
                                    }
                                }

                                String output = buffer.toString().trim();
                                if (output.length() == 0 || bufferCount != 1) {
                                    continue;
                                }
                                output = output.replace(' ', '|');
                                totals.add(output);
                                writeLine(writer, fileName, taskName, output, tokenIDs, tokens, max);
                            }
                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
                            break;
                    }
                }
            }

            for (String key : tokenCount.keySet()) {
                countWriter.append(key).append("\t");
                countWriter.append("00_COUNT").append("\t");
                countWriter.append("-").append("\t");
                countWriter.append(tokenCount.get(key).toString()).append("\n");
                boolean y = false, s = false;
                if (years.containsKey(key)) {
                    y = true;
                    aggregatedTotals.get(years.get(key)).putIfAbsent("00_COUNT", new FrequencyHashSet<>());
                    aggregatedTotals.get(years.get(key)).get("00_COUNT").add("total", Integer.parseInt(tokenCount.get(key).toString()));
                }
                if (schools.containsKey(key)) {
                    s = true;
                    typeTotals.get(schools.get(key)).putIfAbsent("00_COUNT", new FrequencyHashSet<>());
                    typeTotals.get(schools.get(key)).get("00_COUNT").add("total", Integer.parseInt(tokenCount.get(key).toString()));
                }
                if (y && s) {
                    aggTypeTotals.get(schools.get(key)).get(years.get(key)).putIfAbsent("00_COUNT", new FrequencyHashSet<>());
                    aggTypeTotals.get(schools.get(key)).get(years.get(key)).get("00_COUNT").add("total", Integer.parseInt(tokenCount.get(key).toString()));
                }
            }


            countWriter.close();
            writer.close();

            TreeMultimap<String, String> multimap = TreeMultimap.create();
            for (String year : aggregatedTotals.keySet()) {
                for (String task : aggregatedTotals.get(year).keySet()) {
                    if (task.equals("00_COUNT")) {
                        continue;
                    }
                    for (String key : aggregatedTotals.get(year).get(task).keySet()) {
                        multimap.put(task, key);
                    }
                }
            }

            // Aggregati per anno
            BufferedWriter yWriter = new BufferedWriter(new FileWriter(outYearFile));
            yWriter.append("\t");
            yWriter.append("\t");
            for (String year : yearsOrder) {
                yWriter.append("\t").append(year);
            }
            yWriter.append("\n");
            for (String task : multimap.keySet()) {
                for (String key : multimap.get(task)) {
                    yWriter.append(task).append("\t").append("-").append("\t").append(key);
                    for (String year : yearsOrder) {
                        double count = aggregatedTotals.get(year).get("00_COUNT").get("total") * 1.0;
                        int val = 0;
                        try {
                            val = aggregatedTotals.get(year).get(task).get(key);
                        } catch (Exception e) {
                            // ignored
                        }
                        yWriter.append("\t").append(Double.toString((val / count) * 10000));
                    }
                    yWriter.append("\n");
                }
            }

            yWriter.append("\n");

            for (String schoolLabel : schoolsOrder.keySet()) {
                yWriter.append(schoolLabel).append("\n");
                yWriter.append("\t");
                yWriter.append("\t");
                for (String year : yearsOrder) {
                    yWriter.append("\t").append(year);
                }
                yWriter.append("\n");
                for (String task : multimap.keySet()) {
                    for (String key : multimap.get(task)) {
                        yWriter.append(task).append("\t").append("-").append("\t").append(key);
                        for (String year : yearsOrder) {
                            double count = 0;
                            for (String school : schoolsOrder.get(schoolLabel)) {
                                count += aggTypeTotals.get(school).get(year).get("00_COUNT").get("total") * 1.0;
                            }
                            int val = 0;
                            for (String school : schoolsOrder.get(schoolLabel)) {
                                try {
                                    val += aggTypeTotals.get(school).get(year).get(task).get(key);
                                } catch (Exception e) {
                                    // ignored
                                }
                            }
                            yWriter.append("\t").append(Double.toString((val / count) * 10000));
                        }
                        yWriter.append("\n");
                    }
                }
                yWriter.append("\n");
            }

            yWriter.append("\n");

            for (String year : aggregatedTotals.keySet()) {
                for (String task : aggregatedTotals.get(year).keySet()) {
                    aggregatedTotals.get(year).get(task).keySet().stream().sorted().forEach(key -> {
                        try {
                            yWriter.append(year).append("\t");
                            yWriter.append(task).append("\t");
                            yWriter.append(key).append("\t");
                            yWriter.append(aggregatedTotals.get(year).get(task).get(key).toString()).append("\n");
                        } catch (Exception e) {
                            // ignored
                        }
                    });
                }
            }
            yWriter.close();

            // Aggregati per scuola
            BufferedWriter sWriter = new BufferedWriter(new FileWriter(outSchoolsFile));
            sWriter.append("\t");
            sWriter.append("\t");
            for (String school : schoolsOrder.keySet()) {
                sWriter.append("\t").append(school);
            }
            sWriter.append("\n");
            for (String task : multimap.keySet()) {
                for (String key : multimap.get(task)) {
                    sWriter.append(task).append("\t").append("-").append("\t").append(key);
                    for (String schoolLabel : schoolsOrder.keySet()) {
                        double count = 0;
                        for (String school : schoolsOrder.get(schoolLabel)) {
                            count += typeTotals.get(school).get("00_COUNT").get("total") * 1.0;
                        }

                        int val = 0;
                        for (String school : schoolsOrder.get(schoolLabel)) {
                            try {
                                val += typeTotals.get(school).get(task).get(key);
                            } catch (Exception e) {
                                // ignored
                            }
                        }
                        sWriter.append("\t").append(Double.toString((val / count) * 10000));
                    }

                    sWriter.append("\n");
                }
            }

            sWriter.append("\n");

            for (String schoolType : typeTotals.keySet()) {
                for (String task : typeTotals.get(schoolType).keySet()) {
                    typeTotals.get(schoolType).get(task).keySet().stream().sorted().forEach(key -> {
                        try {
                            sWriter.append(schoolType).append("\t");
                            sWriter.append(task).append("\t");
                            sWriter.append(key).append("\t");
                            sWriter.append(typeTotals.get(schoolType).get(task).get(key).toString()).append("\n");
                        } catch (Exception e) {
                            // ignored
                        }
                    });
//                    for (String key : typeTotals.get(schoolType).get(task).keySet()) {
//                        sWriter.append(schoolType).append("\t");
//                        sWriter.append(task).append("\t");
//                        sWriter.append(key).append("\t");
//                        sWriter.append(typeTotals.get(schoolType).get(task).get(key).toString()).append("\n");
//                    }
                }
            }
            sWriter.close();

//            for (String id : years.keySet()) {
//                documentsPerYear.add(years.get(id));
//            }

//            System.out.println(aggregatedTotals);
//            System.out.println(documentsPerYear);
//            for (String temaID : years.keySet()) {
//                if (years.get(temaID).equals("12-13")) {
//                    System.out.println(temaID);
//                }
//            }


        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
