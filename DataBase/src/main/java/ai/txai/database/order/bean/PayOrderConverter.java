package ai.txai.database.order.bean;

import android.text.TextUtils;

import org.greenrobot.greendao.converter.PropertyConverter;

import ai.txai.database.json.GsonManager;

/**
 * Time: 2/23/22
 * Author Hay
 */
public class PayOrderConverter implements PropertyConverter<PayOrderInfoBean, String> {
    @Override
    public PayOrderInfoBean convertToEntityProperty(String databaseValue) {
        if (TextUtils.isEmpty(databaseValue)) {
            return null;
        }
        return GsonManager.getGson().fromJson(databaseValue, PayOrderInfoBean.class);
    }

    @Override
    public String convertToDatabaseValue(PayOrderInfoBean entityProperty) {
        if (entityProperty == null) {
            return "{}";
        } else {
            return GsonManager.getGson().toJson(entityProperty);
        }
    }
}
