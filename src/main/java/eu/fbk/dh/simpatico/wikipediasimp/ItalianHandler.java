package eu.fbk.dh.simpatico.wikipediasimp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by alessio on 23/09/16.
 */

public class ItalianHandler extends DumpHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItalianHandler.class);

    public ItalianHandler(File outputPath) {
        super(outputPath);
    }

    @Override boolean isGoodPage(String title) {
        boolean isGoodPage = true;
        if (title.startsWith("Categoria:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Discussioni utente:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Utente:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Wikipedia:")) {
            isGoodPage = false;
        }
        if (title.startsWith("File:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Portale:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Aiuto:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Template:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Discussione:")) {
            isGoodPage = false;
        }
        if (title.startsWith("Discussioni MediaWiki:")) {
            isGoodPage = false;
        }
        return isGoodPage;
    }

    @Override boolean commentMeansSimplification(String comment) {
        boolean doIt = true;
        if (comment.startsWith("Ha protetto")) {
            doIt = false;
        }
        if (comment.startsWith("Nuova pagina")) {
            doIt = false;
        }
        if (comment.contains("procedura semplificata")) {
            doIt = false;
        }
        if (comment.contains("[[WP:RB|Annullata]]")) {
            doIt = false;
        }
        if (!comment.replaceAll("/\\*.*\\*./", "").contains("semplif")) {
            doIt = false;
        }
        if (comment.contains("template")) {
            doIt = false;
        }
        return doIt;
    }
}
