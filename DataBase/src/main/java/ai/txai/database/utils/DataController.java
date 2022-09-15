package ai.txai.database.utils;

import ai.txai.database.common.KeyValue;
import ai.txai.database.json.GsonManager;
import ai.txai.greendao.DaoSession;
import ai.txai.greendao.KeyValueDao;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public abstract class DataController {

    public DataController() {
    }

    public void put(String key, String value) {
        KeyValue keyValue = new KeyValue(key, value);
        getDaoSession().getKeyValueDao().deleteByKey(key);
        getDaoSession().getKeyValueDao().insert(keyValue);
    }

    public String get(String key) {
        KeyValueDao keyValueDao = getDaoSession().getKeyValueDao();
        KeyValue load = keyValueDao.load(key);
        return load == null ? null : load.getValue();
    }

    public <T> T get(String key, Class<T> tClass) {
        final String json = get(key);
        return json == null ? null : GsonManager.getGson().fromJson(json, tClass);
    }

    public void put(String key, Object value) {
        String strValue = GsonManager.getGson().toJson(value);
        put(key, strValue);
    }

    public abstract DaoSession getDaoSession();

}
