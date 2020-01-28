package eu.fbk.dh.utils.youtube;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.*;
import java.util.*;

public class Test {
    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
    private static final String PROPERTIES_FILENAME = "youtube.properties";
    private static final long NUMBER_OF_VIDEOS_RETURNED = 10;
    private static final String BASE_FOLDER = "/Users/alessio/youtube-list";

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;

    /**
     * Initialize a YouTube object to search for videos on YouTube. Then
     * display the name and thumbnail image of each video in the result set.
     *
     * @param args command line args.
     */
    public static void main(String[] args) {

        Properties properties = new Properties();
        BufferedWriter writer;

        // Read the developer key from the properties file.
        try {
            InputStream in = YouTube.Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);

            // Prompt the user to enter the language.
            String language = getInputQuery();

            File listFile = new File(BASE_FOLDER + File.separator + language + "_list");
            if (!listFile.exists()) {
                throw new FileNotFoundException("List file not found");
            }
            List<String> words = Files.readLines(listFile, Charsets.UTF_8);

            File langFolder = new File(BASE_FOLDER + File.separator + language);
            langFolder.mkdirs();
            if (!langFolder.exists() || !langFolder.isDirectory()) {
                throw new FileNotFoundException("Folder not found");
            }

            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName("youtube-cmdline-search-sample").build();

            for (String word : words) {
                word = word.trim();
                if (word.length() == 0) {
                    continue;
                }
                word = word.toLowerCase();

                System.out.println("Starting " + word);

                String fName = word.replace("[^0-9a-zA-Z-_]", "");
                Set<String> alreadyFound = new HashSet<>();

                String fileName = BASE_FOLDER + File.separator + language + File.separator + fName + ".txt";

                File outFile = new File(fileName);
                if (outFile.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(outFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.length() == 0) {
                            continue;
                        }
                        alreadyFound.add(line);
                    }
                    reader.close();
                }

                writer = new BufferedWriter(new FileWriter(outFile));
                for (String foundVideo : alreadyFound) {
                    writer.append(foundVideo).append("\n");
                }


                Integer page = 0;
                String nextPage = "[none]";

                mainLoop:
                while (nextPage != null) {

                    page++;

                    // Define the API request for retrieving search results.
                    YouTube.Search.List search = youtube.search().list("id");

                    // Set your developer key from the {{ Google Cloud Console }} for
                    // non-authenticated requests. See:
                    // {{ https://cloud.google.com/console }}
                    String apiKey = properties.getProperty("youtube.apikey");
                    search.setKey(apiKey);
                    search.setQ(word);
                    search.setRelevanceLanguage(language);
                    search.setPublishedAfter(new DateTime("2018-11-01T00:00:00Z"));
                    search.setOrder("date");
                    if (!nextPage.equals("[none]")) {
                        search.setPageToken(nextPage);
                    }

                    // Restrict the search results to only include videos. See:
                    // https://developers.google.com/youtube/v3/docs/search/list#type
                    search.setType("video");

                    // To increase efficiency, only retrieve the fields that the
                    // application uses.
                    search.setFields("nextPageToken,pageInfo,items(id/videoId)");
                    search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

                    // Call the API and print results.
                    SearchListResponse searchResponse = search.execute();

                    System.out.println("Page: " + page);
                    System.out.println(searchResponse);

                    nextPage = searchResponse.getNextPageToken();
                    System.out.println("Next page token: " + nextPage);

                    List<SearchResult> items = searchResponse.getItems();
                    if (items.size() == 0) {
                        System.out.println("Breaking loop (items size is zero)");
                        break mainLoop;
                    }

                    for (SearchResult item : items) {
                        String videoId = item.getId().getVideoId();
                        if (alreadyFound.contains(videoId)) {
                            System.out.println("Breaking loop (already found)");
                            break mainLoop;
                        }
                        writer.append(videoId).append("\n");
                    }

                    if (page >= 30) {
                        break mainLoop;
                    }

                    writer.flush();
                    Thread.sleep(1000);

//            Integer totalResults = searchResponse.getPageInfo().getTotalResults();
//            System.out.println("Total results: " + totalResults);
//                List<SearchResult> searchResultList = searchResponse.getItems();
//                if (searchResultList != null) {
//                youtube.comments().list()
//                prettyPrint(searchResultList.iterator(), queryTerm);
//                }
                }

                writer.close();
            }


        } catch (GoogleJsonResponseException e) {
            System.err.println("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            System.err.println("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /*
     * Prompt the user to enter a query term and return the user-specified term.
     */
    private static String getInputQuery() throws IOException {

        String inputQuery = "";

        System.out.print("Please enter the language (it, fr, en): ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        return inputQuery;
    }

    /*
     * Prints out all results in the Iterator. For each result, print the
     * title, video ID, and thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Confirm that the result represents a video. Otherwise, the
            // item will not contain a video ID.
            if (rId.getKind().equals("youtube#video")) {
//                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();

                System.out.println(" Video Id" + rId.getVideoId());
//                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" URL: " + singleVideo.getId().getVideoId());
//                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}
