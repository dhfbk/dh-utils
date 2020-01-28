package eu.fbk.dh.utils.iprase.cat;

import com.google.common.collect.HashMultimap;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
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

public class OutputAnalyzerVerbs {

    static Pattern fileNamePattern = Pattern.compile(".*/([0-9]+)\\.txt\\.json$");

    public static void main(String[] args) {
        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./extract-verbs")
                    .withHeader("Extraction of verbs")
                    .withOption("i", "input", "Input folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("o", "output", "Output folder", "FOLDER", CommandLine.Type.DIRECTORY_EXISTING, true, false, true)
                    .withOption("y", "year", "Year file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFolder = cmd.getOptionValue("input", File.class);
            File outputFolder = cmd.getOptionValue("output", File.class);
            File yearFile = cmd.getOptionValue("year", File.class);

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

//            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            Map<String, FrequencyHashSet<String>> totals = new HashMap<>();

            Map<String, FrequencyHashSet<String>> aggregatedTotals = new HashMap<>();
            for (String value : years.values()) {
                if (value.trim().length() == 0) {
                    continue;
                }
                aggregatedTotals.putIfAbsent(value, new FrequencyHashSet<>());
            }

            Map<String, FrequencyHashSet<String>> typeTotals = new HashMap<>();
            for (String value : schools.values()) {
                if (value.trim().length() == 0) {
                    continue;
                }
                typeTotals.putIfAbsent(value, new FrequencyHashSet<>());
            }

//            Map<String, Map<String, Map<String, FrequencyHashSet<String>>>> aggTypeTotals = new HashMap<>();
//            for (String value : schools.values()) {
//                if (value.trim().length() == 0) {
//                    continue;
//                }
//                aggTypeTotals.putIfAbsent(value, new HashMap<>());
//                for (String year : years.values()) {
//                    if (year.trim().length() == 0) {
//                        continue;
//                    }
//                    aggTypeTotals.get(value).putIfAbsent(year, new HashMap<>());
//                }
//            }

            for (File contentFile : inputFolder.listFiles()) {
                String fileName = contentFile.getAbsolutePath();
                Matcher matcher = fileNamePattern.matcher(fileName);
                if (!matcher.matches()) {
                    System.out.println("ERROR!");
                    continue;
                }

                String idFile = matcher.group(1);
                totals.putIfAbsent(idFile, new FrequencyHashSet<>());
                String s = schools.get(idFile);
//                if (s != null) {
//                    typeTotals.get(s).putIfAbsent(idFile, new FrequencyHashSet<>());
//                }
                String y = years.get(idFile);
//                if (y != null) {
//                    aggregatedTotals.get(y).putIfAbsent(idFile, new FrequencyHashSet<>());
//                }
//                if (y != null && s != null) {
//                    aggTypeTotals.get(s).get(y).putIfAbsent(idFile, new FrequencyHashSet<>());
//                }

                JsonReader jsonReader = new JsonReader(new FileReader(fileName));
                JsonParser parser = new JsonParser();
                JsonArray sentences = parser.parse(jsonReader).getAsJsonObject().getAsJsonArray("sentences");
                for (JsonElement sentence : sentences) {
                    JsonArray tokens = sentence.getAsJsonObject().getAsJsonArray("tokens");
                    for (JsonElement token : tokens) {
                        String pos = token.getAsJsonObject().get("pos").getAsString();
                        if (pos.toUpperCase().startsWith("V")) {
                            if (pos.equals("VM") || pos.equals("VA")) {
                                continue;
                            }

                            String lemma = token.getAsJsonObject().get("lemma").getAsString();
                            if (y != null)
                                aggregatedTotals.get(y).add(lemma);
                            if (s != null)
                                typeTotals.get(s).add(lemma);
//                            if (s != null && y != null)
//                                aggTypeTotals.get(s).get(y).get(idFile).add(lemma);
                            totals.get(idFile).add(lemma);
                        }
                    }
                }
            }

//            System.out.println(typeTotals);
//            System.out.println(aggregatedTotals);

            for (String key : typeTotals.keySet()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder.getAbsolutePath() + File.separator + "school_" + key + ".tsv"));
                for (Map.Entry<String, Integer> entry : typeTotals.get(key).getSorted()) {
                    writer.append(entry.getKey()).append("\t").append(entry.getValue().toString()).append("\n");
                }
                writer.close();
            }

            for (String key : aggregatedTotals.keySet()) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFolder.getAbsolutePath() + File.separator + "year_" + key + ".tsv"));
                for (Map.Entry<String, Integer> entry : aggregatedTotals.get(key).getSorted()) {
                    writer.append(entry.getKey()).append("\t").append(entry.getValue().toString()).append("\n");
                }
                writer.close();
            }

//
//            Map<String, Map<String, Map<String, FrequencyHashSet<String>>>> aggTypeTotals = new HashMap<>();
//            for (String value : schools.values()) {
//                if (value.trim().length() == 0) {
//                    continue;
//                }
//                aggTypeTotals.putIfAbsent(value, new HashMap<>());
//                for (String year : years.values()) {
//                    if (year.trim().length() == 0) {
//                        continue;
//                    }
//                    aggTypeTotals.get(value).putIfAbsent(year, new HashMap<>());
//                }
//            }
//
//            for (String taskName : files.keySet()) {
//                if (taskToDo.size() > 0 && !taskToDo.contains(taskName)) {
//                    continue;
//                }
//                for (String year : aggregatedTotals.keySet()) {
//                    aggregatedTotals.get(year).putIfAbsent(taskName, new FrequencyHashSet<>());
//                }
//                for (String schoolType : typeTotals.keySet()) {
//                    typeTotals.get(schoolType).putIfAbsent(taskName, new FrequencyHashSet<>());
//                }
//                for (String schoolType : typeTotals.keySet()) {
//                    for (String year : aggregatedTotals.keySet()) {
//                        aggTypeTotals.get(schoolType).get(year).putIfAbsent(taskName, new FrequencyHashSet<>());
//                    }
//                }
//
//                System.out.println(taskName);
//                for (String fileName : files.get(taskName).keySet()) {
//                    Map<Integer, String> tokens = new HashMap<>();
//                    Map<Integer, Map<String, String>> markablesInfo = new HashMap<>();
//                    Map<Integer, Set<Integer>> markablesTokens = new HashMap<>();
//
//                    File file = files.get(taskName).get(fileName);
//                    int max = 0;
//
//                    Document xmlDocument = builder.parse(file);
//                    NodeList nodeList;
//
//                    nodeList = (NodeList) xPath.compile("/Document/token").evaluate(xmlDocument, XPathConstants.NODESET);
//                    Integer tCount = nodeList.getLength();
//                    tokenCount.putIfAbsent(fileName, tCount);
//                    for (int i = 0; i < nodeList.getLength(); i++) {
//                        Node node = nodeList.item(i);
//                        String id = node.getAttributes().getNamedItem("t_id").getNodeValue();
//                        int idInt = Integer.parseInt(id);
//                        String value = node.getTextContent();
//                        tokens.put(idInt, value);
//                        max = idInt;
//                    }
//
//                    nodeList = (NodeList) xPath.compile("/Document/Markables/*").evaluate(xmlDocument, XPathConstants.NODESET);
//                    for (int i = 0; i < nodeList.getLength(); i++) {
//                        Set<Integer> tokenList = new HashSet<>();
//                        Node node = nodeList.item(i);
//                        String id = node.getAttributes().getNamedItem("m_id").getNodeValue();
//                        int idInt = Integer.parseInt(id);
//                        markablesInfo.put(idInt, listAllAttributes((Element) node));
//
//                        Element eElement = (Element) node;
//                        NodeList anchors = eElement.getElementsByTagName("token_anchor");
//                        for (int j = 0; j < anchors.getLength(); j++) {
//                            tokenList.add(Integer.parseInt(anchors.item(j).getAttributes().getNamedItem("t_id").getNodeValue()));
//                        }
//
//                        markablesTokens.put(idInt, tokenList);
//                    }
//
////                    System.out.println(tokens);
////                    System.out.println(markablesInfo);
////                    System.out.println(markablesTokens);
//
//                    FrequencyHashSet<String> totals;
//                    switch (taskName) {
//                        case "01_MONOSILLABI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("Tipo_Errore");
//                                totals.add("[totale]");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "03_MAIUSCOLE":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("Tipo");
//                                totals.add("[totale]");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "05_LORO":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("LORO_al_posto_di_A_LORO");
//                                totals.add("[totale]");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "06_GLI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("Scorretto");
//                                String significato = markablesInfo.get(id).get("Significato");
//                                totals.add("[totale]");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                if (significato.length() == 0) {
//                                    significato = "[null]";
//                                }
//                                totals.add(tipo + "|" + significato);
//                                writeLine(writer, fileName, taskName, significato, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "07_QUESTO":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("Equivale_a_CIO_CHE_HO_DETTO");
//                                totals.add("[totale]");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "09_INDICATIVO_IMPERFETTO":
//                        case "10_GERUNDIO":
//                        case "11_INDICATIVO_PRESENTE":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("Uso");
//                                totals.add("[totale]");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "13_AFFISSI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("Tipo");
//                                totals.add("[totale]");
//                                totals.add(tipo);
//                                String prefisso = markablesInfo.get(id).get("Prefisso");
//                                String suffisso = markablesInfo.get(id).get("Suffisso");
//                                String output = tipo + "|" + prefisso + "|" + suffisso;
//                                totals.add(output);
//                                writeLine(writer, fileName, taskName, output, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "14_FRASI_NOMINALI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                String tipo = markablesInfo.get(id).get("Titolo");
//                                if (tipo.length() > 0) {
//                                    continue;
//                                }
//                                totals.add("[totale]");
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "15_CONNETTIVI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                totals.add("[totale]");
//                                String che_polivalente = markablesInfo.get(id).get("CHE_polivalente");
//                                String dove_polivalente = markablesInfo.get(id).get("DOVE_polivalente");
//                                String siccome_al_posto_di_poiche = markablesInfo.get(id).get("SICCOME_al_posto_di_poiche");
//                                String uso_solo_per_infatti_cioe_allora_dunque_quindi = markablesInfo.get(id).get("Uso_solo_per_INFATTI_CIOE_ALLORA_DUNQUE_QUINDI");
//                                StringBuffer buffer = new StringBuffer();
//                                if (che_polivalente.length() > 0) {
//                                    totals.add("CHE_polivalente");
//                                    buffer.append("CHE_polivalente=").append(che_polivalente).append(" ");
//                                }
//                                if (dove_polivalente.length() > 0) {
//                                    totals.add("DOVE_polivalente");
//                                    buffer.append("DOVE_polivalente=").append(dove_polivalente).append(" ");
//                                }
//                                if (siccome_al_posto_di_poiche.length() > 0) {
//                                    totals.add("SICCOME_al_posto_di_poiche");
//                                    buffer.append("SICCOME_al_posto_di_poiche=").append(siccome_al_posto_di_poiche).append(" ");
//                                }
//                                if (uso_solo_per_infatti_cioe_allora_dunque_quindi.length() > 0) {
//                                    totals.add("Uso_solo_per_INFATTI_CIOE_ALLORA_DUNQUE_QUINDI");
//                                    buffer.append("Uso_solo_per_INFATTI_CIOE_ALLORA_DUNQUE_QUINDI=").append(uso_solo_per_infatti_cioe_allora_dunque_quindi).append(" ");
//                                }
//                                String output = buffer.toString().trim();
//                                if (output.length() == 0) {
//                                    continue;
//                                }
//                                output = output.replace(' ', '|');
//                                totals.add(output);
//                                writeLine(writer, fileName, taskName, output, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "17_PUNTEGGIATURA":
//                        case "27_LI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                totals.add("[totale]");
//                                String tipo = markablesInfo.get(id).get("Scorretto");
////                                StringBuffer buffer = new StringBuffer();
////                                for (Integer tokenID : markablesTokens.get(id)) {
////                                    buffer.append(tokens.get(tokenID)).append(" ");
////                                }
////                                String content = buffer.toString().trim();
////                                totals.add(content);
////                                totals.add(content + " - " + (tipo.length() > 0 ? tipo : "Corretto"));
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "18_PERCHE_QUANDO":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                totals.add("[totale]");
//                                String tipo = markablesInfo.get(id).get("Testuale");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "19_REGISTRO_INFORMALE":
//                        case "21_POLITICAMENTE_CORRETTO":
//                        case "22_POLIREMATICHE":
//                        case "23_PLASTISMI":
//                        case "25_FRASI_SCISSE":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                totals.add("[totale]");
//                                String tipo = "[null]";
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "20_ANGLICISMI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                totals.add("[totale]");
//                                String tipo = markablesInfo.get(id).get("Tipo");
//                                String traducibile = markablesInfo.get(id).get("Traducibile");
//                                if (tipo.length() == 0) {
//                                    continue;
//                                }
//                                totals.add(tipo);
//                                if (traducibile.length() > 0) {
//                                    tipo += "|trad";
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                        case "24_DISLOCAZIONI":
//                            totals = new FrequencyHashSet<>();
//                            for (Integer id : markablesInfo.keySet()) {
//                                totals.add("[totale]");
//                                String tipo = markablesInfo.get(id).get("Tipo");
//                                if (tipo.length() == 0) {
//                                    tipo = "[null]";
//                                }
//                                totals.add(tipo);
//                                writeLine(writer, fileName, taskName, tipo, markablesTokens.get(id), tokens, max);
//                            }
//                            writeLines(countWriter, fileName, taskName, totals, aggregatedTotals, years, typeTotals, schools, aggTypeTotals);
//                            break;
//                    }
//                }
////                break;
//
//            }
//
//            for (String key : tokenCount.keySet()) {
//                countWriter.append(key).append("\t");
//                countWriter.append("00_COUNT").append("\t");
//                countWriter.append("-").append("\t");
//                countWriter.append(tokenCount.get(key).toString()).append("\n");
//                boolean y = false, s = false;
//                if (years.containsKey(key)) {
//                    y = true;
//                    aggregatedTotals.get(years.get(key)).putIfAbsent("00_COUNT", new FrequencyHashSet<>());
//                    aggregatedTotals.get(years.get(key)).get("00_COUNT").add("total", Integer.parseInt(tokenCount.get(key).toString()));
//                }
//                if (schools.containsKey(key)) {
//                    s = true;
//                    typeTotals.get(schools.get(key)).putIfAbsent("00_COUNT", new FrequencyHashSet<>());
//                    typeTotals.get(schools.get(key)).get("00_COUNT").add("total", Integer.parseInt(tokenCount.get(key).toString()));
//                }
//                if (y && s) {
//                    aggTypeTotals.get(schools.get(key)).get(years.get(key)).putIfAbsent("00_COUNT", new FrequencyHashSet<>());
//                    aggTypeTotals.get(schools.get(key)).get(years.get(key)).get("00_COUNT").add("total", Integer.parseInt(tokenCount.get(key).toString()));
//                }
//            }
//
//
//            countWriter.close();
//            writer.close();
//
//            HashMultimap<String, String> multimap = HashMultimap.create();
//            for (String year : aggregatedTotals.keySet()) {
//                for (String task : aggregatedTotals.get(year).keySet()) {
//                    if (task.equals("00_COUNT")) {
//                        continue;
//                    }
//                    for (String key : aggregatedTotals.get(year).get(task).keySet()) {
//                        multimap.put(task, key);
//                    }
//                }
//            }
//
//            // Aggregati per anno
//            BufferedWriter yWriter = new BufferedWriter(new FileWriter(outYearFile));
//            yWriter.append("\t");
//            for (String year : yearsOrder) {
//                yWriter.append("\t").append(year);
//            }
//            yWriter.append("\n");
//            for (String task : multimap.keySet()) {
//                for (String key : multimap.get(task)) {
//                    yWriter.append(task).append("\t").append(key);
//                    for (String year : yearsOrder) {
//                        double count = aggregatedTotals.get(year).get("00_COUNT").get("total") * 1.0;
//                        int val = 0;
//                        try {
//                            val = aggregatedTotals.get(year).get(task).get(key);
//                        } catch (Exception e) {
//                            // ignored
//                        }
//                        yWriter.append("\t").append(Double.toString((val / count) * 10000));
//                    }
//                    yWriter.append("\n");
//                }
//            }
//
//            yWriter.append("\n");
//
//            for (String schoolLabel : schoolsOrder.keySet()) {
//                yWriter.append(schoolLabel).append("\n");
//                yWriter.append("\t");
//                for (String year : yearsOrder) {
//                    yWriter.append("\t").append(year);
//                }
//                yWriter.append("\n");
//                for (String task : multimap.keySet()) {
//                    for (String key : multimap.get(task)) {
//                        yWriter.append(task).append("\t").append(key);
//                        for (String year : yearsOrder) {
//                            double count = 0;
//                            for (String school : schoolsOrder.get(schoolLabel)) {
//                                count += aggTypeTotals.get(school).get(year).get("00_COUNT").get("total") * 1.0;
//                            }
//                            int val = 0;
//                            for (String school : schoolsOrder.get(schoolLabel)) {
//                                try {
//                                    val += aggTypeTotals.get(school).get(year).get(task).get(key);
//                                } catch (Exception e) {
//                                    // ignored
//                                }
//                            }
//                            yWriter.append("\t").append(Double.toString((val / count) * 10000));
//                        }
//                        yWriter.append("\n");
//                    }
//                }
//                yWriter.append("\n");
//            }
//
//            yWriter.append("\n");
//
//            for (String year : aggregatedTotals.keySet()) {
//                for (String task : aggregatedTotals.get(year).keySet()) {
//                    for (String key : aggregatedTotals.get(year).get(task).keySet()) {
//                        yWriter.append(year).append("\t");
//                        yWriter.append(task).append("\t");
//                        yWriter.append(key).append("\t");
//                        yWriter.append(aggregatedTotals.get(year).get(task).get(key).toString()).append("\n");
//                    }
//                }
//            }
//            yWriter.close();
//
//            // Aggregati per scuola
//            BufferedWriter sWriter = new BufferedWriter(new FileWriter(outSchoolsFile));
//            sWriter.append("\t");
//            for (String school : schoolsOrder.keySet()) {
//                sWriter.append("\t").append(school);
//            }
//            sWriter.append("\n");
//            for (String task : multimap.keySet()) {
//                for (String key : multimap.get(task)) {
//                    sWriter.append(task).append("\t").append(key);
//                    for (String schoolLabel : schoolsOrder.keySet()) {
//                        double count = 0;
//                        for (String school : schoolsOrder.get(schoolLabel)) {
//                            count += typeTotals.get(school).get("00_COUNT").get("total") * 1.0;
//                        }
//
//                        int val = 0;
//                        for (String school : schoolsOrder.get(schoolLabel)) {
//                            try {
//                                val += typeTotals.get(school).get(task).get(key);
//                            } catch (Exception e) {
//                                // ignored
//                            }
//                        }
//                        sWriter.append("\t").append(Double.toString((val / count) * 10000));
//                    }
//
//                    sWriter.append("\n");
//                }
//            }
//
//            sWriter.append("\n");
//
//            for (String schoolType : typeTotals.keySet()) {
//                for (String task : typeTotals.get(schoolType).keySet()) {
//                    for (String key : typeTotals.get(schoolType).get(task).keySet()) {
//                        sWriter.append(schoolType).append("\t");
//                        sWriter.append(task).append("\t");
//                        sWriter.append(key).append("\t");
//                        sWriter.append(typeTotals.get(schoolType).get(task).get(key).toString()).append("\n");
//                    }
//                }
//            }
//            sWriter.close();


        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
