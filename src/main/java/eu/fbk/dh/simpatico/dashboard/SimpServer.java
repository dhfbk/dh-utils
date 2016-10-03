package eu.fbk.dh.simpatico.dashboard;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.core.PropertiesUtils;
import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: alessio
 * Date: 21/07/14
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */

public class SimpServer {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SimpServer.class);

    public static final String DEFAULT_HOST = "0.0.0.0";
    public static final Integer DEFAULT_PORT = 8011;

    public SimpServer(String host, Integer port, @Nullable String configFile) {
        LOGGER.info("Starting " + host + "\t" + port + " (" + new Date() + ")...");

        final HttpServer httpServer = new HttpServer();
        NetworkListener nl = new NetworkListener("dashboard", host, port);
        httpServer.addListener(nl);

        Properties props = new Properties();

        try {
            InputStream stream = null;
            if (configFile != null) {
                stream = new FileInputStream(configFile);
            }
            if (stream == null) {
                stream = SimpServer.class.getResourceAsStream("/simpatico-default.props");
            }
            props.load(stream);
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Properties enProps = PropertiesUtils.dotConvertedProperties(props, "en");
        Properties itProps = PropertiesUtils.dotConvertedProperties(props, "it");
        Properties esProps = PropertiesUtils.dotConvertedProperties(props, "es");

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

        // Post stuff

        LOGGER.info("Adding annotation handler");
        httpServer.getServerConfiguration().addHttpHandler(new SimpHandler(itProps, enProps, esProps), "/simp");

        LOGGER.info("Adding demo handler");
        httpServer.getServerConfiguration().addHttpHandler(
                new CLStaticHttpHandler(HttpServer.class.getClassLoader(), "webdemo/"), "/");

        // Fix
        // see: http://stackoverflow.com/questions/35123194/jersey-2-render-swagger-static-content-correctly-without-trailing-slash
//        httpServer.getServerConfiguration().addHttpHandler(
//                new CLStaticHttpHandler(HttpServer.class.getClassLoader(), "webdemo/static/"), "/static/");

        try {
            httpServer.start();
            Thread.currentThread().join();
        } catch (Exception e) {
            LOGGER.error("error running " + host + ":" + port);
        }
    }

    public static void main(String[] args) {

        try {
            final CommandLine cmd = CommandLine
                    .parser()
                    .withName("./tintop-server-ita")
                    .withHeader("Run the Tintop Server for Italian simplification")
                    .withOption("c", "config", "Configuration file", "FILE", CommandLine.Type.FILE_EXISTING, true,
                            false, false)
                    .withOption("p", "port", String.format("Host port (default %d)", DEFAULT_PORT), "NUM",
                            CommandLine.Type.INTEGER, true, false, false)
                    .withOption(null, "host", String.format("Host address (default %s)", DEFAULT_HOST), "NUM",
                            CommandLine.Type.STRING, true, false, false)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            String host = cmd.getOptionValue("host", String.class, DEFAULT_HOST);
            Integer port = cmd.getOptionValue("port", Integer.class, DEFAULT_PORT);
            String configFile = cmd.getOptionValue("config", String.class);

            SimpServer pipelineServer = new SimpServer(host, port, configFile);

        } catch (Exception e) {
            CommandLine.fail(e);
        }

    }
}
