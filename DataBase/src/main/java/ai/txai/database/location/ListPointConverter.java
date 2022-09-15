package ai.txai.database.location;

import android.util.Log;

import com.google.gson.reflect.TypeToken;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;

import ai.txai.database.json.GsonManager;

/**
 * Time: 2/23/22
 * Author Hay
 */
public class ListPointConverter implements PropertyConverter<List<Point>, String> {
    @Override
    public List<Point> convertToEntityProperty(String databaseValue) {
        if(databaseValue == null) {
            return null;
        }
        TypeToken<List<Point>> typeToken = new TypeToken<List<Point>>(){};
        Log.i("ListPoint", databaseValue);
        return GsonManager.getGson().fromJson(databaseValue, typeToken.getType());
    }

    @Override
    public String convertToDatabaseValue(List<Point> entityProperty) {
        if (entityProperty == null||entityProperty.size()==0) {
            return "[]";
        } else {
            String sb = GsonManager.getGson().toJson(entityProperty);
            Log.i("ListPoint1", sb);
            return sb;
        }
    }
}
