package ai.txai.database.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Time: 2/23/22
 * Author Hay
 */
public class GsonManager {
    private static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .enableComplexMapKeySerialization()
                    .serializeNulls()
                    .create();
        }
        return gson;
    }
}
