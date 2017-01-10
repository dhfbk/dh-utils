package eu.fbk.dh.simpatico.dashboard;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by alessio on 25/05/15.
 */

public class LexensteinModel {

    private static LexensteinModel instance;
    private Set<String> lemmaList;
    private static final Logger LOGGER = LoggerFactory.getLogger(LexensteinModel.class);

    private LexensteinModel(String listFile) {
        LOGGER.trace("Loading model for Lexenstein");
        lemmaList = new HashSet<>();

        if (listFile != null) {
            try {
                List<String> lines = Files.readLines(new File(listFile), Charsets.UTF_8);
                for (String line : lines) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    lemmaList.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static LexensteinModel getInstance(String listFile) {
        if (instance == null) {
            instance = new LexensteinModel(listFile);
        }

        return instance;
    }

    public Set<String> getLemmaList() {
        return lemmaList;
    }
}
