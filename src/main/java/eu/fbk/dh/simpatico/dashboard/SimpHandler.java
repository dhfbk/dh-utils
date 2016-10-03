package eu.fbk.dh.simpatico.dashboard;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;
import eu.fbk.dkm.pikes.twm.MachineLinking;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created with IntelliJ IDEA.
 * User: alessio
 * Date: 21/07/14
 * Time: 15:30
 * This class convert a raw NAF to a parsed one
 */

public class SimpHandler extends HttpHandler {

    static Logger LOGGER = Logger.getLogger(SimpHandler.class.getName());
    private Properties itProps, enProps, esProps;
    private static Set<String> supportedLanguages = Stream.of("it", "en", "es")
            .collect(Collectors.toCollection(HashSet::new));

    public void writeOutput(Response response, String contentType, String output) throws IOException {
        response.setContentType(contentType);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write(output);
    }

    public SimpHandler(Properties itProps, Properties enProps, Properties esProps) {
        super();
        this.itProps = itProps;
        this.enProps = enProps;
        this.esProps = esProps;
    }

    @Override
    public void service(Request request, Response response) throws Exception {

        Annotation annotation = null;

        Properties mlProperties = new Properties();
        mlProperties.setProperty("address", "http://ml.apnetwork.it/annotate");
        mlProperties.setProperty("min_confidence", "0.25");
        MachineLinking machineLinking = new MachineLinking(mlProperties);

        StanfordCoreNLP enPipeline = new StanfordCoreNLP(enProps);
        StanfordCoreNLP esPipeline = new StanfordCoreNLP(esProps);
        TintPipeline itPipeline = new TintPipeline();
        try {
            itPipeline.loadDefaultProperties();
            itPipeline.addProperties(itProps);
        } catch (IOException e) {
            e.printStackTrace();
        }
        itPipeline.load();

        LOGGER.debug("Starting service");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String text = request.getParameter("text");
        String lang = request.getParameter("lang");

        if (lang == null || !supportedLanguages.contains(lang)) {
            lang = machineLinking.lang(text);
        }

        switch (lang) {
        case "it":
            annotation = itPipeline.runRaw(text);
            break;
        case "es":
            annotation = new Annotation(text);
            esPipeline.annotate(annotation);
            break;
        case "en":
            annotation = new Annotation(text);
            enPipeline.annotate(annotation);
            break;
        }

        String json = "";
        if (annotation == null) {
            response.setStatus(HttpStatus.NOT_IMPLEMENTED_501);
        } else {
            try {
                json = JSONOutputter.jsonPrint(annotation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        writeOutput(response, "text/json", json);
    }

}
