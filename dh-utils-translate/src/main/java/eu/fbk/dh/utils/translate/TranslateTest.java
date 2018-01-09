package eu.fbk.dh.utils.translate;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslateTest {

    public static void main(String[] args) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        String text = "The quick brown fox jumped over the lazy dog.";
        Translation translation =
                translate.translate(
                        text,
                        Translate.TranslateOption.sourceLanguage("en"),
                        Translate.TranslateOption.targetLanguage("it"));

        System.out.printf("Text: %s%n", text);
        System.out.printf("Translation: %s%n", translation.getTranslatedText());

    }

}
