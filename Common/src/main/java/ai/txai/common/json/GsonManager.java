package ai.txai.common.json;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Time: 2/23/22
 * Author Hay
 */
public class GsonManager {
    private static Gson gson = getGson();

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .enableComplexMapKeySerialization()
                    .registerTypeAdapter(new TypeToken<Map<String, Object>>(){}.getType(), new DataTypeAdapter())
                    .disableHtmlEscaping()
                    .create();
        }
        return gson;
    }

    /**
     * 转成json
     *
     * @param object
     * @return
     */
    public static String GsonString(Object object) {
        if (object == null) {
            return "";
        }
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }

    /**
     * 转成bean
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> T fromJsonObject(String gsonString, Class<T> cls) {
        if (TextUtils.isEmpty(gsonString)) {
            return null;
        }
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }
        return t;
    }


    /**
     * 转成list
     * 解决泛型问题
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> fromJsonArray(String json, Class<T> cls) {
        if (TextUtils.isEmpty(json)) {
            return new ArrayList<T>();
        }
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        if (gson != null) {
            for (final JsonElement elem : array) {
                list.add(gson.fromJson(elem, cls));
            }
        }
        return list;
    }


    /**
     * 转成list中有map的
     *
     * @param gsonString
     * @return
     */
    public static <T> List<Map<String, T>> GsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        if (gson != null) {
            list = gson.fromJson(gsonString, new TypeToken<List<Map<String, T>>>() {
            }.getType());
        }
        return list;
    }

    /**
     * 转成map的
     *
     * @param gsonString
     * @return
     */
    public static Map<String, Object> GsonToMaps(String gsonString) {
        if (TextUtils.isEmpty(gsonString)) {
            return new HashMap<>();
        }
        if (gson != null) {
            return gson.fromJson(gsonString, new TypeToken<Map<String,Object>>(){}.getType());
        }
        return new HashMap<>();
    }


    public static <T> ParameterizedType type(final Class<T> raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }


    public static JsonObject parseJson(String jsonStr) {
        return fromJsonObject(jsonStr, JsonObject.class);
    }

}
