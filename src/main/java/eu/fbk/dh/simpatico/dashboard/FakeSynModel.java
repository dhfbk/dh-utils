package eu.fbk.dh.simpatico.dashboard;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import eu.fbk.dh.tint.readability.GlossarioEntry;
import eu.fbk.dh.tint.readability.it.ItalianReadability;
import eu.fbk.dh.tint.readability.it.ItalianReadabilityModel;
import eu.fbk.dh.tint.runner.TintPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Created by alessio on 26/09/16.
 */
public class FakeSynModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItalianReadabilityModel.class);
    private static FakeSynModel ourInstance = null;
    private HashMap<String, GlossarioEntry> glossario = new HashMap<>();
//    File inputFile = new File("/Users/alessio/Documents/out-sinonimicontrari-lemmatized-noinvert.txt");

    public static FakeSynModel getInstance(Properties globalProperties, Properties localProperties) {
        if (ourInstance == null) {
            HashMap<String, GlossarioEntry> glossario = new HashMap<>();

            try {
                HashMap<String, LinkedTreeMap> glossarioTmp;

                String listFileName = localProperties.getProperty("list");
                File inputFile = new File(listFileName);

                Gson gson = new GsonBuilder().create();
                glossarioTmp = gson.fromJson(Files.toString(inputFile, Charsets.UTF_8), HashMap.class);

                List<String> glossarioKeys = new ArrayList<>(glossarioTmp.keySet());
                Collections.sort(glossarioKeys, new ItalianReadability.StringLenComparator());

                for (String form : glossarioKeys) {
                    LinkedTreeMap linkedTreeMap = glossarioTmp.get(form);

                    ArrayList<String> arrayList = (ArrayList<String>) linkedTreeMap.get("forms");
                    String[] strings = new String[arrayList.size()];
                    strings = arrayList.toArray(strings);
//                String[] strings = (String[]) linkedTreeMap.get("forms");
                    String description = (String) linkedTreeMap.get("description");
                    GlossarioEntry entry = new GlossarioEntry(strings, description);
                    glossario.put(form, entry);
                }

//                TintPipeline pipeline = new TintPipeline();
//                pipeline.loadDefaultProperties();
//                pipeline.setProperty("annotators", "ita_toksent, pos, ita_morpho, ita_lemma");
//                pipeline.load();
//
//                int lemmaIndex = 0;
//                HashMap<Integer, Integer> lemmaIndexes = new HashMap<>();
//                HashMap<Integer, Integer> tokenIndexes = new HashMap<>();
//                HashMap<Integer, Integer> endIndexes = new HashMap<>();
//
//                Annotation annotation = pipeline.runRaw(text);
//                StringBuffer lemmaText = new StringBuffer();
//                for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
//                    lemmaText.append(token.lemma()).append(" ");
//                    lemmaIndexes.put(lemmaText.length(), lemmaIndex);
//                    tokenIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), lemmaIndex);
//                    endIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
//                            token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));
//
//                    lemmaIndex++;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            ourInstance = new FakeSynModel(glossario);
        }
        return ourInstance;
    }

    private FakeSynModel(
            HashMap<String, GlossarioEntry> glossario) {
        this.glossario = glossario;
    }

    public HashMap<String, GlossarioEntry> getGlossario() {
        return glossario;
    }
}
