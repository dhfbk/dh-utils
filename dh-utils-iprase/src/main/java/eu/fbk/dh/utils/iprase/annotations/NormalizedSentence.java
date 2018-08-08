package eu.fbk.dh.utils.iprase.annotations;

import java.util.List;

public class NormalizedSentence {
    private List<Integer> tokenIDs;
    private List<Integer> offsets;
    private List<String> tokens;
    private String normalizedText;

    public NormalizedSentence(List<Integer> tokenIDs, List<Integer> offsets, List<String> tokens, String normalizedText) {
        this.tokenIDs = tokenIDs;
        this.offsets = offsets;
        this.tokens = tokens;
        this.normalizedText = normalizedText;
    }

    public List<Integer> getTokenIDs() {
        return tokenIDs;
    }

    public List<Integer> getOffsets() {
        return offsets;
    }

    public List<String> getTokens() {
        return tokens;
    }

    public String getNormalizedText() {
        return normalizedText;
    }
}
