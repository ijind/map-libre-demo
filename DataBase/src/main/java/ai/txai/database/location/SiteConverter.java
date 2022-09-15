package ai.txai.database.location;

import org.greenrobot.greendao.converter.PropertyConverter;

import ai.txai.database.json.GsonManager;
import ai.txai.database.site.Site;

/**
 * Time: 2/23/22
 * Author Hay
 */
public class SiteConverter implements PropertyConverter<Site, String> {
    @Override
    public Site convertToEntityProperty(String databaseValue) {
        return GsonManager.getGson().fromJson(databaseValue, Site.class);
    }

    @Override
    public String convertToDatabaseValue(Site entityProperty) {
        return GsonManager.getGson().toJson(entityProperty);
    }
}
