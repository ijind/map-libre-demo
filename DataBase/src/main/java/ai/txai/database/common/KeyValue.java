package ai.txai.database.common;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Time: 15/03/2022
 * Author Hay
 */

@Entity
public class KeyValue {
    @Id
    private String key;
    private String value;
    @Generated(hash = 16554064)
    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
    @Generated(hash = 92014137)
    public KeyValue() {
    }
    public String getKey() {
        return this.key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}
