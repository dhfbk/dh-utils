package eu.fbk.dh.utils.deportati;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.openrdf.model.*;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.slf4j.LoggerFactory;
import eu.fbk.utils.core.CommandLine;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static Pattern idPattern = Pattern.compile("([0-9]+)$");

    static ValueFactory factory = ValueFactoryImpl.getInstance();
    static URI pointClass = factory.createURI("http://www.w3.org/2003/01/geo/wgs84_pos#Point");
    static URI pointProperty = factory.createURI("http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/point");
    static URI latProperty = factory.createURI("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
    static URI longProperty = factory.createURI("http://www.w3.org/2003/01/geo/wgs84_pos#long");

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./parse-json-deportati")
                    .withHeader("Import deportati from JSON file")
                    .withOption("i", "input", "Input file", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withOption("c", "csv", "CSV file with places", "FILE", CommandLine.Type.FILE_EXISTING, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File inputFile = cmd.getOptionValue("input", File.class);
            File outputFile = cmd.getOptionValue("output", File.class);
            File csvPlaceFile = cmd.getOptionValue("csv", File.class);

            Map<String, URI> places = new HashMap<>();
            if (csvPlaceFile != null) {
                Reader in = new FileReader(csvPlaceFile);
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(in);
                for (CSVRecord record : records) {
                    URI uri = factory.createURI(record.get("n"));
                    String description = record.get("l");
                    places.put(description.replaceAll("\\s+", "").toLowerCase(), uri);
                }
            }

            Gson gson = new Gson();
            List<Statement> statementList = new ArrayList<>();

            JsonObject myJson = gson.fromJson(Files.toString(inputFile, Charsets.UTF_8), JsonObject.class);

            URI bioPosition = factory.createURI("http://purl.org/vocab/bio/0.1/position");
//            URI bioKeywords = factory.createURI("http://purl.org/vocab/bio/0.1/keywords");

            URI survivor = factory.createURI("http://dati.cdec.it/lod/bio-ext/shoahSurvivor");
            URI dateOfBirth = factory.createURI("http://dati.cdec.it/lod/bio-ext/dateOfBirth");
            URI dateOfDeath = factory.createURI("http://dati.cdec.it/lod/bio-ext/dateOfDeath");
            URI birthPlace = factory.createURI("http://dati.cdec.it/lod/bio-ext/birthPlace");
            URI deathPlace = factory.createURI("http://dati.cdec.it/lod/bio-ext/deathPlace");
            URI detentionPlace = factory.createURI("http://dati.cdec.it/lod/shoah/detentionPlace");
            URI fromPrison = factory.createURI("http://dati.cdec.it/lod/shoah/fromPrison");
            URI toPrison = factory.createURI("http://dati.cdec.it/lod/shoah/toPrison");
            URI toNaziCamp = factory.createURI("http://dati.cdec.it/lod/shoah/toNaziCamp");
            URI fromNaziCamp = factory.createURI("http://dati.cdec.it/lod/shoah/fromNaziCamp");
            URI toNaziCampLabel = factory.createURI("http://dati.cdec.it/lod/shoah/toNaziCampLabel");
            URI arrivalDate = factory.createURI("http://dati.cdec.it/lod/shoah/arrivalDate");
            URI transferDate = factory.createURI("http://dati.cdec.it/lod/shoah/transferDate");
            URI deathDescription = factory.createURI("http://dati.cdec.it/lod/shoah/deathDescription");

            URI persecutionType = factory.createURI("http://dati.cdec.it/lod/shoah/Persecution");
            URI liberationType = factory.createURI("http://dati.cdec.it/lod/shoah/Liberation");
            URI liberationProperty = factory.createURI("http://dati.cdec.it/lod/shoah/liberation");
            URI naziCampType = factory.createURI("http://dati.cdec.it/lod/shoah/NaziCamp");
            URI naziCampTransferType = factory.createURI("http://dati.cdec.it/lod/shoah/NaziCampTransfer");
            URI naziCampTransferProperty = factory.createURI("http://dati.cdec.it/lod/shoah/naziCampTransfer");
            URI detentionPlaceTransferType = factory.createURI("http://dati.cdec.it/lod/shoah/DetentionPlaceTransfer");
            URI detentionPlaceTransferProperty = factory.createURI("http://dati.cdec.it/lod/shoah/detentionPlaceTransfer");
            URI placeType = factory.createURI("http://dati.cdec.it/lod/shoah/Place");
            URI persecutionProperty = factory.createURI("http://dati.cdec.it/lod/shoah/persecution");
            URI arrestPlace = factory.createURI("http://dati.cdec.it/lod/shoah/arrestPlace");
            URI arrestDate = factory.createURI("http://dati.cdec.it/lod/shoah/arrestDate");
            URI returnPlace = factory.createURI("http://dati.cdec.it/lod/shoah/returnPlace");
            URI returnDate = factory.createURI("http://dati.cdec.it/lod/shoah/returnDate");

            URI deathPoint = factory.createURI("http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/deathPoint");
            URI birthPoint = factory.createURI("http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/birthPoint");
            URI returnPoint = factory.createURI("http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/birthPoint");
            URI naziCampPoint = factory.createURI("http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/birthPoint");

            Set<String> uniqueMovements = new HashSet<>();
            uniqueMovements.add("Arrest");
            uniqueMovements.add("Birth");
            uniqueMovements.add("Death");
            uniqueMovements.add("Return after liberation");
            uniqueMovements.add("To Nazi Camp");
            // Multiple values: Detention, Transfer

            Map<String, String> naziCampUris = new HashMap<>();
            naziCampUris.put("Neuengamme", "http://dati.cdec.it/lod/shoah/camp/97");
            naziCampUris.put("Dora Mittelbau", "http://dati.cdec.it/lod/shoah/camp/30");
            naziCampUris.put("Auschwitz", "http://dati.cdec.it/lod/shoah/camp/4");
            naziCampUris.put("Ravensbrueck", "http://dati.cdec.it/lod/shoah/camp/117");
            naziCampUris.put("Natzweiler", "http://dati.cdec.it/lod/shoah/camp/96");
            naziCampUris.put("Sachsenhausen", "http://dati.cdec.it/lod/shoah/camp/123");
            naziCampUris.put("Reichenau", "http://dati.cdec.it/lod/shoah/camp/172");
            naziCampUris.put("Buchenwald", "http://dati.cdec.it/lod/shoah/camp/19");
            naziCampUris.put("Mauthausen", "http://dati.cdec.it/lod/shoah/camp/88");
            naziCampUris.put("Dachau", "http://dati.cdec.it/lod/shoah/camp/27");
            naziCampUris.put("Gusen", "http://dati.cdec.it/lod/shoah/camp/48");
            naziCampUris.put("Flossenbürg", "http://dati.cdec.it/lod/shoah/camp/157");
            naziCampUris.put("Ohrdruff", "http://dati.cdec.it/lod/shoah/camp/108");
            naziCampUris.put("Nordhausen", "http://dati.cdec.it/lod/shoah/camp/100");
            naziCampUris.put("Melk", "http://dati.cdec.it/lod/shoah/camp/89");
            naziCampUris.put("Leitmeritz", "http://dati.cdec.it/lod/shoah/camp/75");
            naziCampUris.put("Hersbruck", "http://dati.cdec.it/lod/shoah/camp/54");
            naziCampUris.put("Allach", "http://dati.cdec.it/lod/shoah/camp/1");
            naziCampUris.put("Porschdorf", "http://dati.cdec.it/lod/shoah/camp/112");
            naziCampUris.put("Linz", "http://dati.cdec.it/lod/shoah/camp/79");

//            int persecutionID = 0;
            AtomicInteger pointID = new AtomicInteger(0);
            int naziCampID = 0;

            for (JsonElement deportato : myJson.get("data").getAsJsonArray()) {

                JsonObject deportatoObj = deportato.getAsJsonObject();
                String url = deportatoObj.get("web_reference").getAsString();
                String name = deportatoObj.get("name").getAsString();
                String employment = deportatoObj.get("employment").getAsString();
                String discipline = deportatoObj.get("discipline").getAsString();
                String gender = deportatoObj.get("gender").getAsString().toUpperCase();

                String id = null;
                Matcher matcher = idPattern.matcher(url);
                if (matcher.find()) {
                    id = matcher.group(1);
                }

                URI uri = factory.createURI(url);

//                persecutionID++; // only one persecution for each person
                String persecutionUrl = "http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/persecuzione/" + id;
                URI persecutionUri = factory.createURI(persecutionUrl);

                statementList.add(factory.createStatement(persecutionUri, RDF.TYPE, persecutionType));
                statementList.add(factory.createStatement(persecutionUri, RDFS.LABEL, factory.createLiteral("Persecution of " + name)));
                statementList.add(factory.createStatement(uri, persecutionProperty, persecutionUri));

                statementList.add(factory.createStatement(uri, RDFS.LABEL, factory.createLiteral(name)));
                statementList.add(factory.createStatement(uri, RDF.TYPE, FOAF.PERSON));
                statementList.add(factory.createStatement(uri, DCTERMS.SOURCE, uri));
                statementList.add(factory.createStatement(uri, bioPosition, factory.createLiteral(employment)));
                statementList.add(factory.createStatement(uri, FOAF.GENDER, factory.createLiteral(gender)));

                boolean survived = discipline.equals("Survivor");
                statementList.add(factory.createStatement(uri, survivor, factory.createLiteral(survived)));
                if (!survived) {
                    statementList.add(factory.createStatement(persecutionUri, deathDescription, factory.createLiteral(discipline)));
                }

                Map<String, List<JsonObject>> movements = new HashMap<>();
                Map<Double, Map<Double, URI>> points = new HashMap<>();

                for (JsonElement movement : deportatoObj.get("movements").getAsJsonArray()) {
                    JsonObject movementObj = movement.getAsJsonObject();

                    String place = movementObj.get("place").getAsString();
                    int date = movementObj.get("date").getAsInt();
                    double lat = movementObj.get("latitude").getAsDouble();
                    double lon = movementObj.get("longitude").getAsDouble();

                    String resource_frame = movementObj.get("resource_frame").getAsString();
                    movements.putIfAbsent(resource_frame, new ArrayList<>());
                    movements.get(resource_frame).add(movementObj);

                    switch (resource_frame) {
                        case "Birth":
                            statementList.add(factory.createStatement(uri, birthPlace, factory.createLiteral(place)));
                            statementList.add(factory.createStatement(uri, dateOfBirth, factory.createLiteral(date)));
                            statementList.add(factory.createStatement(uri, birthPoint, pointUri(lat, lon, statementList, pointID, points)));
                            break;
                        case "Death":
                            statementList.add(factory.createStatement(uri, deathPlace, factory.createLiteral(place)));
                            statementList.add(factory.createStatement(uri, dateOfDeath, factory.createLiteral(date)));
                            statementList.add(factory.createStatement(uri, deathPoint, pointUri(lat, lon, statementList, pointID, points)));
                            break;
                        case "Arrest":
                            URI placeUri = createPlace(place, places);
                            Literal dateLiteral = createItalianDate(date);
                            statementList.add(factory.createStatement(persecutionUri, arrestPlace, placeUri));
                            statementList.add(factory.createStatement(placeUri, RDF.TYPE, placeType));
                            statementList.add(factory.createStatement(placeUri, RDFS.LABEL, factory.createLiteral(place)));
                            statementList.add(factory.createStatement(placeUri, pointProperty, pointUri(lat, lon, statementList, pointID, points)));
                            statementList.add(factory.createStatement(persecutionUri, arrestDate, dateLiteral));
                            break;
                        case "Return after liberation":
                            String liberationUrl = "http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/liberazione/" + id;
                            URI liberationURI = factory.createURI(liberationUrl);
                            statementList.add(factory.createStatement(liberationURI, RDF.TYPE, liberationType));
                            statementList.add(factory.createStatement(persecutionUri, liberationProperty, liberationURI));
                            statementList.add(factory.createStatement(liberationURI, RDFS.LABEL, factory.createLiteral("Liberation of " + name)));
                            statementList.add(factory.createStatement(liberationURI, returnDate, factory.createLiteral(date)));
                            statementList.add(factory.createStatement(liberationURI, returnPlace, factory.createLiteral(place)));
                            statementList.add(factory.createStatement(uri, returnPoint, pointUri(lat, lon, statementList, pointID, points)));
                            break;
                    }
                }

                for (String uniqueMovement : uniqueMovements) {
                    if (movements.get(uniqueMovement) != null && movements.get(uniqueMovement).size() > 1) {
                        System.out.println(String.format("Error, %s movement has frequency %d in %s", uniqueMovement, movements.get(uniqueMovement).size(), name));
                    }
                }

                Set<String> naziCamps = new HashSet<>();

                if (movements.get("To Nazi Camp") != null) {
                    for (JsonObject to_nazi_camp : movements.get("To Nazi Camp")) {
                        String ncName = to_nazi_camp.get("place").getAsString();
                        if (!naziCampUris.containsKey(ncName)) {
                            naziCamps.add(ncName);
                        }
                    }
                }
                if (movements.get("Transfer") != null) {
                    for (JsonObject to_nazi_camp : movements.get("Transfer")) {
                        String ncName = to_nazi_camp.get("place").getAsString();
                        if (!naziCampUris.containsKey(ncName)) {
                            naziCamps.add(ncName);
                        }
                    }
                }

                for (String naziCamp : naziCamps) {
                    naziCampID++; // only one persecution for each person
                    String naziCampUrl = "http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/campo/" + naziCampID;
                    naziCampUris.put(naziCamp, naziCampUrl);
                    System.out.println("Added camp " + naziCamp);
                    URI naziCampUri = factory.createURI(naziCampUrl);
                    statementList.add(factory.createStatement(naziCampUri, RDF.TYPE, naziCampType));
                    statementList.add(factory.createStatement(naziCampUri, RDFS.LABEL, factory.createLiteral(naziCamp)));
                }

                if (movements.get("Detention") != null) {
                    int movementID = 0;
                    int detentionPlaceTransferID = 0;
                    URI previousURI = null;
                    for (JsonObject movement : movements.get("Detention")) {
                        JsonObject movementObj = movement.getAsJsonObject();
                        String place = movementObj.get("place").getAsString();
                        double lat = movementObj.get("latitude").getAsDouble();
                        double lon = movementObj.get("longitude").getAsDouble();
                        URI placeUri = createPlace(place, places);

                        if (movementID > 0) {
                            detentionPlaceTransferID++;
                            String detentionPlaceTransferUrl = persecutionUrl + "_" + detentionPlaceTransferID;
                            URI detentionPlaceTransferUri = factory.createURI(detentionPlaceTransferUrl);
                            statementList.add(factory.createStatement(detentionPlaceTransferUri, RDF.TYPE, detentionPlaceTransferType));
                            statementList.add(factory.createStatement(detentionPlaceTransferUri, RDFS.LABEL, factory.createLiteral(String.format("Transfer of %s to %s", name, place))));
                            statementList.add(factory.createStatement(persecutionUri, detentionPlaceTransferProperty, detentionPlaceTransferUri));
                            statementList.add(factory.createStatement(detentionPlaceTransferUri, fromPrison, previousURI));
                            statementList.add(factory.createStatement(detentionPlaceTransferUri, toPrison, placeUri));
                        }

                        statementList.add(factory.createStatement(persecutionUri, detentionPlace, placeUri));
                        statementList.add(factory.createStatement(placeUri, RDF.TYPE, placeType));
                        statementList.add(factory.createStatement(placeUri, RDFS.LABEL, factory.createLiteral(place)));
                        statementList.add(factory.createStatement(placeUri, pointProperty, pointUri(lat, lon, statementList, pointID, points)));
                        previousURI = factory.createURI(placeUri.toString());
                        movementID++;
                    }

                }

                String previousPlace = null;
                if (movements.get("To Nazi Camp") != null) {
                    for (JsonObject movement : movements.get("To Nazi Camp")) {
                        JsonObject movementObj = movement.getAsJsonObject();
                        String place = movementObj.get("place").getAsString();
                        int date = movementObj.get("date").getAsInt();
                        double lat = movementObj.get("latitude").getAsDouble();
                        double lon = movementObj.get("longitude").getAsDouble();
                        Literal dateLiteral = createItalianDate(date);

                        previousPlace = naziCampUris.get(place);
                        statementList.add(factory.createStatement(persecutionUri, toNaziCamp, factory.createURI(previousPlace)));
                        statementList.add(factory.createStatement(persecutionUri, arrivalDate, dateLiteral));
                        statementList.add(factory.createStatement(persecutionUri, toNaziCampLabel, factory.createLiteral(place)));
                        statementList.add(factory.createStatement(persecutionUri, naziCampPoint, pointUri(lat, lon, statementList, pointID, points)));
                    }
                }

                if (movements.get("Transfer") != null) {

                    int transferID = 0;
                    for (JsonObject movement : movements.get("Transfer")) {
                        JsonObject movementObj = movement.getAsJsonObject();
                        String place = movementObj.get("place").getAsString();
                        String thisPlace = naziCampUris.get(place);
                        int date = movementObj.get("date").getAsInt();
                        double lat = movementObj.get("latitude").getAsDouble();
                        double lon = movementObj.get("longitude").getAsDouble();
                        Literal dateLiteral = createItalianDate(date);

                        transferID++;
                        String transferUrl = persecutionUrl + "_" + transferID;
                        URI transferUri = factory.createURI(transferUrl);
                        statementList.add(factory.createStatement(transferUri, RDF.TYPE, naziCampTransferType));
                        statementList.add(factory.createStatement(transferUri, RDFS.LABEL, factory.createLiteral(String.format("Transfer of %s to %s", name, place))));
                        statementList.add(factory.createStatement(persecutionUri, naziCampTransferProperty, transferUri));
                        try {
                            statementList.add(factory.createStatement(transferUri, fromNaziCamp, factory.createURI(previousPlace)));
                        } catch (Exception e) {
                            System.out.println(name);
                        }
                        statementList.add(factory.createStatement(transferUri, toNaziCamp, factory.createURI(thisPlace)));
                        statementList.add(factory.createStatement(transferUri, toNaziCampLabel, factory.createLiteral(place)));
                        statementList.add(factory.createStatement(transferUri, transferDate, dateLiteral));
                        statementList.add(factory.createStatement(transferUri, naziCampPoint, pointUri(lat, lon, statementList, pointID, points)));
                    }
                }
            }

            FileWriter outputWriter = new FileWriter(outputFile);
            RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, outputWriter);
            writer.startRDF();
            for (Statement statement : statementList) {
                writer.handleStatement(statement);
            }
            writer.endRDF();

            outputWriter.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }

    private static URI pointUri(double lat, double lon, List<Statement> statementList, AtomicInteger pointID, Map<Double, Map<Double, URI>> points) {
        if (points.containsKey(lat)) {
            if (points.get(lat).containsKey(lon)) {
                return points.get(lat).get(lon);
            }
        }

        URI pointUrl = factory.createURI("http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/point/" + pointID.incrementAndGet());
        statementList.add(factory.createStatement(pointUrl, RDF.TYPE, pointClass));
        statementList.add(factory.createStatement(pointUrl, latProperty, factory.createLiteral(lat)));
        statementList.add(factory.createStatement(pointUrl, longProperty, factory.createLiteral(lon)));

        points.putIfAbsent(lat, new HashMap<>());
        points.get(lat).putIfAbsent(lon, pointUrl);

        return pointUrl;
    }

    private static Literal createItalianDate(int date) {
        String dateStr = Integer.toString(date);
        return factory.createLiteral(dateStr.substring(6) + "/" +
                dateStr.substring(4, 6) + "/" + dateStr.substring(0, 4));
    }

    private static URI createPlace(String place, Map<String, URI> placeMap) {
        String placeLc = place.replaceAll("\\s+", "").toLowerCase();
        if (placeMap.containsKey(placeLc)) {
            return placeMap.get(placeLc);
        }
        String prefix = "http://dati.cdec.it/lod/shoah/place/";
        place = place.replaceAll("\\s+", "_");
//        String placeUrl = "http://www.labstoriarovereto.it/archivi/trentini_deportati_nei_Lager_del_3_Reich/luogo/" + place;
        return factory.createURI(prefix + place);
    }
}
