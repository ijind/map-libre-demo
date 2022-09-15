package ai.txai.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import ai.txai.database.user.User;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER".
*/
public class UserDao extends AbstractDao<User, String> {

    public static final String TABLENAME = "USER";

    /**
     * Properties of entity User.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Uid = new Property(0, String.class, "uid", true, "UID");
        public final static Property Nickname = new Property(1, String.class, "nickname", false, "NICKNAME");
        public final static Property PhoneNumber = new Property(2, long.class, "phoneNumber", false, "PHONE_NUMBER");
        public final static Property CountryCode = new Property(3, int.class, "countryCode", false, "COUNTRY_CODE");
        public final static Property Email = new Property(4, String.class, "email", false, "EMAIL");
        public final static Property Avatar = new Property(5, String.class, "avatar", false, "AVATAR");
        public final static Property ApiToken = new Property(6, String.class, "apiToken", false, "API_TOKEN");
        public final static Property PushToken = new Property(7, String.class, "pushToken", false, "PUSH_TOKEN");
        public final static Property UpdateToken = new Property(8, String.class, "updateToken", false, "UPDATE_TOKEN");
        public final static Property IsoCode = new Property(9, String.class, "isoCode", false, "ISO_CODE");
    }


    public UserDao(DaoConfig config) {
        super(config);
    }
    
    public UserDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER\" (" + //
                "\"UID\" TEXT PRIMARY KEY NOT NULL ," + // 0: uid
                "\"NICKNAME\" TEXT," + // 1: nickname
                "\"PHONE_NUMBER\" INTEGER NOT NULL ," + // 2: phoneNumber
                "\"COUNTRY_CODE\" INTEGER NOT NULL ," + // 3: countryCode
                "\"EMAIL\" TEXT," + // 4: email
                "\"AVATAR\" TEXT," + // 5: avatar
                "\"API_TOKEN\" TEXT," + // 6: apiToken
                "\"PUSH_TOKEN\" TEXT," + // 7: pushToken
                "\"UPDATE_TOKEN\" TEXT," + // 8: updateToken
                "\"ISO_CODE\" TEXT);"); // 9: isoCode
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, User entity) {
        stmt.clearBindings();
 
        String uid = entity.getUid();
        if (uid != null) {
            stmt.bindString(1, uid);
        }
 
        String nickname = entity.getNickname();
        if (nickname != null) {
            stmt.bindString(2, nickname);
        }
        stmt.bindLong(3, entity.getPhoneNumber());
        stmt.bindLong(4, entity.getCountryCode());
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(5, email);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(6, avatar);
        }
 
        String apiToken = entity.getApiToken();
        if (apiToken != null) {
            stmt.bindString(7, apiToken);
        }
 
        String pushToken = entity.getPushToken();
        if (pushToken != null) {
            stmt.bindString(8, pushToken);
        }
 
        String updateToken = entity.getUpdateToken();
        if (updateToken != null) {
            stmt.bindString(9, updateToken);
        }
 
        String isoCode = entity.getIsoCode();
        if (isoCode != null) {
            stmt.bindString(10, isoCode);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, User entity) {
        stmt.clearBindings();
 
        String uid = entity.getUid();
        if (uid != null) {
            stmt.bindString(1, uid);
        }
 
        String nickname = entity.getNickname();
        if (nickname != null) {
            stmt.bindString(2, nickname);
        }
        stmt.bindLong(3, entity.getPhoneNumber());
        stmt.bindLong(4, entity.getCountryCode());
 
        String email = entity.getEmail();
        if (email != null) {
            stmt.bindString(5, email);
        }
 
        String avatar = entity.getAvatar();
        if (avatar != null) {
            stmt.bindString(6, avatar);
        }
 
        String apiToken = entity.getApiToken();
        if (apiToken != null) {
            stmt.bindString(7, apiToken);
        }
 
        String pushToken = entity.getPushToken();
        if (pushToken != null) {
            stmt.bindString(8, pushToken);
        }
 
        String updateToken = entity.getUpdateToken();
        if (updateToken != null) {
            stmt.bindString(9, updateToken);
        }
 
        String isoCode = entity.getIsoCode();
        if (isoCode != null) {
            stmt.bindString(10, isoCode);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public User readEntity(Cursor cursor, int offset) {
        User entity = new User( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // uid
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // nickname
            cursor.getLong(offset + 2), // phoneNumber
            cursor.getInt(offset + 3), // countryCode
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // email
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // avatar
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // apiToken
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // pushToken
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // updateToken
            cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9) // isoCode
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, User entity, int offset) {
        entity.setUid(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setNickname(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPhoneNumber(cursor.getLong(offset + 2));
        entity.setCountryCode(cursor.getInt(offset + 3));
        entity.setEmail(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAvatar(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setApiToken(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setPushToken(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setUpdateToken(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setIsoCode(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
     }
    
    @Override
    protected final String updateKeyAfterInsert(User entity, long rowId) {
        return entity.getUid();
    }
    
    @Override
    public String getKey(User entity) {
        if(entity != null) {
            return entity.getUid();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(User entity) {
        return entity.getUid() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}