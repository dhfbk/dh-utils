package eu.fbk.dh.simpatico.dashboard;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import eu.fbk.dh.tint.runner.TintPipeline;
import eu.fbk.dh.tint.runner.outputters.JSONOutputter;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;

import java.io.IOException;
import java.util.Properties;

/**
 * Created with IntelliJ IDEA.
 * User: alessio
 * Date: 21/07/14
 * Time: 15:30
 * This class convert a raw NAF to a parsed one
 */

public class DashboardHandler extends AbstractHandler {

    public DashboardHandler(Properties allProps) {
        super(allProps);
    }

    @Override
    public void service(Request request, Response response) throws Exception {

        super.service(request, response);
        Annotation annotation = null;

//        boolean doLex = PropertiesUtils.getBoolean(request.getParameter("lex"), false);
        boolean doLex = false;

        StanfordCoreNLP enPipeline = new StanfordCoreNLP(enProps);
        StanfordCoreNLP esPipeline = new StanfordCoreNLP(esProps);
        TintPipeline itPipeline = new TintPipeline();
        try {
            itPipeline.loadDefaultProperties();
            itPipeline.addProperties(itProps);
            String annotators = itPipeline.getProperty("annotators");
            if (doLex && !annotators.contains("lexenstein")) {
                itPipeline.setProperty("annotators", itProps.getProperty("annotators") + ", lexenstein");
                System.out.println("Annotators: " + itPipeline.getProperty("annotators"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        itPipeline.load();

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
