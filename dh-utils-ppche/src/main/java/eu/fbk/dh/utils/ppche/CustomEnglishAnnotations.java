package eu.fbk.dh.utils.ppche;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

public class CustomEnglishAnnotations {

    @JSONLabel("stanfordpos")
    public static class StanfordPos implements CoreAnnotation<String> {
        @Override
        public Class<String> getType() {
            return ErasureUtils.uncheckedCast(String.class);
        }
    }

    @JSONLabel("original_lemma")
    public static class OriginalLemma implements CoreAnnotation<String> {
        @Override
        public Class<String> getType() {
            return ErasureUtils.uncheckedCast(String.class);
        }
    }
}
