package ai.txai.database;

import android.app.Application;
import android.text.TextUtils;

import org.greenrobot.greendao.database.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import ai.txai.database.listener.DbListener;
import ai.txai.database.lock.BlockingObject;
import ai.txai.database.migration.MyOpenHelper;
import ai.txai.database.user.User;
import ai.txai.database.utils.CommonData;
import ai.txai.greendao.DaoMaster;
import ai.txai.greendao.DaoSession;

/**
 * Time: 2/21/22
 * Author Hay
 */
public class GreenDaoHelper {
    private static final String DB_NAME_COMMON = "common";
    private static final String DB_NAME_USER = "user_";
    private static final String KEY_CLIENT_ID = "client_id";

    private Application application;
    private boolean isDebug;
    private String key;//for user db

    private final Map<String, DaoMaster> daoMasters = new HashMap<>();
    private final Map<String, DaoSession> daoSessions = new HashMap<>();

    private Database userDb;

    private BlockingObject<Boolean> commonDbLock = new BlockingObject();
    private BlockingObject<Boolean> userDbLock = new BlockingObject();

    private DbListener dbListener;

    private GreenDaoHelper() {
    }

    private static class Holder {
        private static GreenDaoHelper instance = new GreenDaoHelper();
    }

    public static GreenDaoHelper getInstance() {
        return Holder.instance;
    }


    public void init(Application application, boolean isDebug, String clientId, DbListener listener) {
        this.application = application;
        this.isDebug = isDebug;
        this.dbListener = listener;
        initCommonDb(clientId);
    }

    public void initCommonDb(String clientId) {
        String realDbName = formatDbName(DB_NAME_COMMON);
        DaoMaster.OpenHelper openHelper = new MyOpenHelper(application, realDbName);
        Database writableDb = openHelper.getWritableDb();
        DaoMaster daoMaster = new DaoMaster(writableDb);
        DaoSession daoSession = daoMaster.newSession();
        daoMasters.put(realDbName, daoMaster);
        daoSessions.put(realDbName, daoSession);
        commonDbLock.set(Boolean.TRUE);
        boolean hasPwd = !TextUtils.isEmpty(clientId) && clientId.length() > 8;
        User user = CommonData.getInstance().currentUser();
        if (user != null) {
            initUserDb(user.getUid(), clientId);
        } else {
            if (!hasPwd) {
                String password = UUID.randomUUID().toString().replace("-", "");
                CommonData.getInstance().put(KEY_CLIENT_ID, password);
            }
        }
    }

    /**
     * called when login
     *
     * @param key
     * @param clientId
     */
    public void initUserDb(String key, String clientId) {
        String s = CommonData.getInstance().get(KEY_CLIENT_ID);
        String password;
        if (!TextUtils.isEmpty(s)) {
            password = s.substring(0, 8);
        } else {
            password = clientId.substring(0, 8);
        }
        this.key = key;
        String realDbName = formatDbName(String.format("%s%s", DB_NAME_USER, key));
        DaoMaster.OpenHelper openHelper = new MyOpenHelper(application, realDbName);
        userDb = openHelper.getEncryptedWritableDb(password);
        DaoMaster daoMaster = new DaoMaster(userDb);
        DaoSession daoSession = daoMaster.newSession();
        daoMasters.put(realDbName, daoMaster);
        daoSessions.put(realDbName, daoSession);
        userDbLock.set(Boolean.TRUE);
        if (dbListener != null) {
            dbListener.userDBInit(CommonData.getInstance().currentUser());
        }
    }

    public void closeUserDb(String key) {
        userDbLock = new BlockingObject<>();
        this.key = key;
        String realDbName = formatDbName(String.format("%s%s", DB_NAME_USER, key));
        daoMasters.remove(realDbName);
        daoSessions.remove(realDbName);
        userDb.close();

        userDb = null;
    }

    public DaoMaster commonDaoMaster() {
        String realDbName = formatDbName(DB_NAME_COMMON);
        return daoMasters.get(realDbName);
    }

    public DaoMaster userDaoMaster() {
        String realDbName = formatDbName(String.format("%s%s", DB_NAME_USER, key));
        return daoMasters.get(realDbName);
    }

    public DaoSession commonDaoSession() {
        String realDbName = formatDbName(DB_NAME_COMMON);
        return daoSessions.get(realDbName);
    }

    public DaoSession userDaoSession() {
        String realDbName = formatDbName(String.format("%s%s", DB_NAME_USER, key));
        return daoSessions.get(realDbName);
    }

    private static String formatDbName(String name) {
        return String.format("%s%s", getInstance().isDebug ? "t_db_" : "db_", name);
    }

    public void waitCommonDb() {
        Boolean aBoolean = commonDbLock.get(1, TimeUnit.MINUTES);
    }

    public boolean isCommonInit() {
        return Boolean.TRUE == commonDbLock.getImmediately();
    }

    public void waitUserDb() {
        Boolean aBoolean = userDbLock.get();
    }

    public boolean isUserDbInit() {
        return Boolean.TRUE == userDbLock.getImmediately();
    }
}
