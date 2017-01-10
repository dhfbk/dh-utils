package eu.fbk.dh.simpatico.dashboard;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.ArraySet;
import eu.fbk.dh.tint.readability.DescriptionForm;
import eu.fbk.dh.tint.readability.GlossarioEntry;
import eu.fbk.dh.tint.readability.it.ItalianReadability;
import eu.fbk.utils.core.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alessio on 19/12/16.
 */

public class FakeSynAnnotator implements Annotator {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeSynAnnotator.class);
    private FakeSynModel model;
    private static Pattern firstLinePattern = Pattern.compile("1. ([^2]+)");
    private static Pattern firstResPattern = Pattern.compile("1. ([^,]+)");
    private boolean onlyOne = false;

    public FakeSynAnnotator(String annotatorName, Properties props) {
        Properties globalProperties = props;
        Properties localProperties = PropertiesUtils.dotConvertedProperties(props, annotatorName);
        this.model = FakeSynModel.getInstance(globalProperties, localProperties);
        this.onlyOne = PropertiesUtils.getBoolean(localProperties.getProperty("only_one", "false"), false);
    }

    @Override public void annotate(Annotation annotation) {

        List<LexensteinAnnotator.Simplification> simplificationList = new ArrayList<>();

        int lemmaIndex = 0;
        HashMap<Integer, Integer> lemmaIndexes = new HashMap<>();
        HashMap<Integer, Integer> tokenIndexes = new HashMap<>();
        HashMap<Integer, Integer> endIndexes = new HashMap<>();

        String text = annotation.get(CoreAnnotations.TextAnnotation.class);
        StringBuffer lemmaText = new StringBuffer();
        for (CoreLabel token : annotation.get(CoreAnnotations.TokensAnnotation.class)) {
            lemmaText.append(token.lemma()).append(" ");
            lemmaIndexes.put(lemmaText.length(), lemmaIndex);
            tokenIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class), lemmaIndex);
            endIndexes.put(token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class),
                    token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));

            lemmaIndex++;
        }

        HashMap<String, GlossarioEntry> glossario = model.getGlossario();
        List<String> glossarioKeys = new ArrayList<>(glossario.keySet());
        TreeMap<Integer, DescriptionForm> forms = new TreeMap<>();

        for (String form : glossarioKeys) {

            if (form.length() < 4) {
                continue;
            }

            int numberOfTokens = form.split("\\s+").length;
            List<Integer> allOccurrences = ItalianReadability.findAllOccurrences(text, form);
//                List<Integer> allLemmaOccurrences = ItalianReadability
//                        .findAllOccurrences(lemmaText.toString().trim(), form);
//                if (allLemmaOccurrences.size() > 0) {
//                    System.out.println(form);
//                    System.out.println(allLemmaOccurrences);
//                }

            for (Integer occurrence : allOccurrences) {
                ItalianReadability
                        .addDescriptionForm(form, tokenIndexes, occurrence, numberOfTokens, forms, annotation,
                                glossario);
            }
//                for (Integer occurrence : allLemmaOccurrences) {
//                    ItalianReadability
//                            .addDescriptionForm(form, lemmaIndexes, occurrence, numberOfTokens, forms, annotation,
//                                    glossario);
//                }
        }

        for (Integer integer : forms.keySet()) {
            Integer end = endIndexes.get(integer);
            if (end == null) {
                continue;
            }

            Integer tokenIndex = tokenIndexes.get(integer);
            CoreLabel token = annotation.get(CoreAnnotations.TokensAnnotation.class).get(tokenIndex);

            if (token.word().equals("socio")) {
                continue;
            }

            //firstResPattern
            String simplifiedVersion = forms.get(integer).getDescription().getDescription();
            Matcher matcher;
            if (onlyOne) {
                matcher = firstResPattern.matcher(simplifiedVersion);
            } else {
                matcher = firstLinePattern.matcher(simplifiedVersion);
            }
            if (matcher.find()) {
                simplifiedVersion = matcher.group(1).trim();
            }

            String[] strings = simplifiedVersion.split(",");
            LinkedHashSet<String> results = new LinkedHashSet<>();
            for (String string : strings) {
                if (string.contains(" ")) {
                    continue;
                }
                results.add(string);
            }
            for (String string : strings) {
                results.add(string);
            }

            StringBuffer buffer = new StringBuffer();
            for (String result : results) {
                buffer.append(" ").append(result).append(",");
            }
            simplifiedVersion = buffer.delete(buffer.length() - 1, buffer.length()).toString().trim();

            LexensteinAnnotator.Simplification simplification = new LexensteinAnnotator.Simplification(
                    token.beginPosition(),
                    token.endPosition(),
                    simplifiedVersion
            );
            simplification.setOriginalValue(token.word());
            simplificationList.add(simplification);
            token.set(SimpaticoAnnotations.SimplifiedAnnotation.class, simplifiedVersion);
        }

        annotation.set(SimpaticoAnnotations.SimplificationsAnnotation.class, simplificationList);
    }

    @Override public Set<Requirement> requirementsSatisfied() {
        return Collections.emptySet();
    }

    @Override public Set<Requirement> requires() {
        return Collections.unmodifiableSet(new ArraySet(new Annotator.Requirement[] {
                TOKENIZE_REQUIREMENT, SSPLIT_REQUIREMENT, POS_REQUIREMENT, LEMMA_REQUIREMENT
        }));
    }
}
