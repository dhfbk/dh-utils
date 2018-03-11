package eu.fbk.dh.utils.iprase.annotations;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import eu.fbk.utils.gson.JSONLabel;

import java.util.List;
import java.util.Map;

/**
 * Created by giovannimoretti on 19/05/16.
 */
public class CatAnnotations {

    @JSONLabel("cat_tasks")
    public static class CatTasksAnnotation implements CoreAnnotation<List<Task>> {

        public Class<List<Task>> getType() {
            return ErasureUtils.uncheckedCast(List.class);
        }
    }

    @JSONLabel("statistics")
    public static class StatisticsAnnotation implements CoreAnnotation<Map<String, Integer>> {

        public Class<Map<String, Integer>> getType() {
            return ErasureUtils.uncheckedCast(Map.class);
        }
    }

}
