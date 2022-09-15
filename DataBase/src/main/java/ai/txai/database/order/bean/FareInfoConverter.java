package ai.txai.database.order.bean;

import android.text.TextUtils;

import org.greenrobot.greendao.converter.PropertyConverter;

import ai.txai.database.json.GsonManager;

/**
 * Time: 2/23/22
 * Author Hay
 */
public class FareInfoConverter implements PropertyConverter<FareInfoBean, String> {
    @Override
    public FareInfoBean convertToEntityProperty(String databaseValue) {
        if (TextUtils.isEmpty(databaseValue)) {
            return null;
        }
        return GsonManager.getGson().fromJson(databaseValue, FareInfoBean.class);
    }

    @Override
    public String convertToDatabaseValue(FareInfoBean entityProperty) {
        if (entityProperty == null) {
            return "{}";
        } else {
            return GsonManager.getGson().toJson(entityProperty);
        }
    }
}
