package eu.fbk.dh.simpatico.dashboard;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dkm.pikes.twm.MachineLinking;
import eu.fbk.utils.core.PropertiesUtils;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

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

public abstract class AbstractHandler extends HttpHandler {

    static Logger LOGGER = Logger.getLogger(AbstractHandler.class.getName());
    protected Properties itProps, enProps, esProps, allProps;
    protected static Set<String> supportedLanguages = Stream.of("it", "en", "es")
            .collect(Collectors.toCollection(HashSet::new));
    protected MachineLinking machineLinking;

    public void writeOutput(Response response, String contentType, String output) throws IOException {
        response.setContentType(contentType);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.getWriter().write(output);
    }

    public AbstractHandler(Properties allProps) {
        super();
        this.allProps = allProps;
        enProps = PropertiesUtils.dotConvertedProperties(allProps, "en");
        itProps = PropertiesUtils.dotConvertedProperties(allProps, "it");
        esProps = PropertiesUtils.dotConvertedProperties(allProps, "es");

        LOGGER.info("Loading English pipeline");
        StanfordCoreNLP enPipeline = new StanfordCoreNLP(enProps);

        LOGGER.info("Loading Spanish pipeline");
        StanfordCoreNLP esPipeline = new StanfordCoreNLP(esProps);

        LOGGER.info("Loading Italian pipeline");
        TintPipeline itPipeline = new TintPipeline();
        try {
            itPipeline.loadDefaultProperties();
            itPipeline.addProperties(itProps);
        } catch (IOException e) {
            e.printStackTrace();
        }
        itPipeline.load();

        Properties mlProperties = new Properties();
        mlProperties.setProperty("address", allProps.getProperty("ml_address"));
        mlProperties.setProperty("min_confidence", allProps.getProperty("ml_min_confidence"));
        machineLinking = new MachineLinking(mlProperties);
    }

    @Override public void service(Request request, Response response) throws Exception {
        LOGGER.debug("Starting service");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
    }
}
