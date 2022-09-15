package ai.txai.database.location;

import org.greenrobot.greendao.converter.PropertyConverter;

import ai.txai.database.json.GsonManager;


/**
 * Time: 2/23/22
 * Author Hay
 */
public class PointConverter implements PropertyConverter<Point, String> {
    @Override
    public Point convertToEntityProperty(String databaseValue) {
        return GsonManager.getGson().fromJson(databaseValue, Point.class);
    }

    @Override
    public String convertToDatabaseValue(Point entityProperty) {
        return GsonManager.getGson().toJson(entityProperty);
    }
}
