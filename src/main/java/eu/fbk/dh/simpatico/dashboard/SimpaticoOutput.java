package eu.fbk.dh.simpatico.dashboard;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.languagetool.rules.RuleMatch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

import eu.fbk.dkm.pikes.twm.LinkingTag;

/**
 * Created by alessio on 10/01/17.
 */

public class SimpaticoOutput {

    List<RuleMatch> languagetool;
    SimpaticoReadability readability;
    String docDate;
    String timings;
    List<LinkingTag> linkings;
    List<LexensteinAnnotator.Simplification> simplifications;
    List<String> sentenceTexts;
    public List<RuleMatch> getLanguagetool() {
        return languagetool;
    }
    public void setLanguagetool(List<RuleMatch> languagetool) {
        this.languagetool = languagetool;
    }
    public SimpaticoReadability getReadability() {
        return readability;
    }
    public void setReadability(SimpaticoReadability readability) {
        this.readability = readability;
    }
    public String getDocDate() {
        return docDate;
    }
    public void setDocDate(String docDate) {
        this.docDate = docDate;
    }
    public String getTimings() {
        return timings;
    }
    public void setTimings(String timings) {
        this.timings = timings;
    }
    public List<LinkingTag> getLinkings() {
        return linkings;
    }
    public void setLinkings(List<LinkingTag> linkings) {
        this.linkings = linkings;
    }
    public List<LexensteinAnnotator.Simplification> getSimplifications() {
        return simplifications;
    }
    public void setSimplifications(List<LexensteinAnnotator.Simplification> simplifications) {
        this.simplifications = simplifications;
    }
    public List<String> getSentenceTexts() {
        return sentenceTexts;
    }
    public void setSentenceTexts(List<String> sentenceTexts) {
        this.sentenceTexts = sentenceTexts;
    }


    public static class SimpaticoReadability {
        private String language = null;
        private int contentWordSize = 0, contentEasyWordSize = 0, wordCount = 0;
        private int docLenWithSpaces = 0, docLenWithoutSpaces = 0, docLenLettersOnly = 0;
        private int sentenceCount = 0, tokenCount = 0;
        private int hyphenCount = 0;
        private int hyphenWordCount = 0;

        protected Map<String, Double> measures = new HashMap<>();

        protected HashMap<String, String> genericPosDescription = new HashMap<>();
        protected HashMap<String, String> posDescription = new HashMap<>();

        Set<Integer> tooLongSentences = new HashSet<>();
        Stats posStats = new Stats();
        Stats genericPosStats = new Stats();
        public String getLanguage() {
            return language;
        }
        public void setLanguage(String language) {
            this.language = language;
        }
        public int getContentWordSize() {
            return contentWordSize;
        }
        public void setContentWordSize(int contentWordSize) {
            this.contentWordSize = contentWordSize;
        }
        public int getContentEasyWordSize() {
            return contentEasyWordSize;
        }
        public void setContentEasyWordSize(int contentEasyWordSize) {
            this.contentEasyWordSize = contentEasyWordSize;
        }
        public int getWordCount() {
            return wordCount;
        }
        public void setWordCount(int wordCount) {
            this.wordCount = wordCount;
        }
        public int getDocLenWithSpaces() {
            return docLenWithSpaces;
        }
        public void setDocLenWithSpaces(int docLenWithSpaces) {
            this.docLenWithSpaces = docLenWithSpaces;
        }
        public int getDocLenWithoutSpaces() {
            return docLenWithoutSpaces;
        }
        public void setDocLenWithoutSpaces(int docLenWithoutSpaces) {
            this.docLenWithoutSpaces = docLenWithoutSpaces;
        }
        public int getDocLenLettersOnly() {
            return docLenLettersOnly;
        }
        public void setDocLenLettersOnly(int docLenLettersOnly) {
            this.docLenLettersOnly = docLenLettersOnly;
        }
        public int getSentenceCount() {
            return sentenceCount;
        }
        public void setSentenceCount(int sentenceCount) {
            this.sentenceCount = sentenceCount;
        }
        public int getTokenCount() {
            return tokenCount;
        }
        public void setTokenCount(int tokenCount) {
            this.tokenCount = tokenCount;
        }
        public int getHyphenCount() {
            return hyphenCount;
        }
        public void setHyphenCount(int hyphenCount) {
            this.hyphenCount = hyphenCount;
        }
        public int getHyphenWordCount() {
            return hyphenWordCount;
        }
        public void setHyphenWordCount(int hyphenWordCount) {
            this.hyphenWordCount = hyphenWordCount;
        }
        public Map<String, Double> getMeasures() {
            return measures;
        }
        public void setMeasures(Map<String, Double> measures) {
            this.measures = measures;
        }
        public HashMap<String, String> getGenericPosDescription() {
            return genericPosDescription;
        }
        public void setGenericPosDescription(HashMap<String, String> genericPosDescription) {
            this.genericPosDescription = genericPosDescription;
        }
        public HashMap<String, String> getPosDescription() {
            return posDescription;
        }
        public void setPosDescription(HashMap<String, String> posDescription) {
            this.posDescription = posDescription;
        }
        public Set<Integer> getTooLongSentences() {
            return tooLongSentences;
        }
        public void setTooLongSentences(Set<Integer> tooLongSentences) {
            this.tooLongSentences = tooLongSentences;
        }
        public Stats getPosStats() {
            return posStats;
        }
        public void setPosStats(Stats posStats) {
            this.posStats = posStats;
        }
        public Stats getGenericPosStats() {
            return genericPosStats;
        }
        public void setGenericPosStats(Stats genericPosStats) {
            this.genericPosStats = genericPosStats;
        }

    }

    public static class Stats {
        private HashMap<String, Integer> support = new HashMap<String, Integer>();

        public HashMap<String, Integer> getSupport() {
            return support;
        }

        public void setSupport(HashMap<String, Integer> support) {
            this.support = support;
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        // configure mapper, if necessary, then create schema generator
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        JsonSchema schema = schemaGen.generateSchema(SimpaticoOutput.class);
        System.err.println(mapper.writeValueAsString(schema));
    }
}
