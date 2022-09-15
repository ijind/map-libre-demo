package ai.txai.database.vehicle;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Time: 18/03/2022
 * Author Hay
 */
@Entity
public class VehicleModel {
    @Id
    private String id;

    private Integer maxPassengerNum;
    private String name;
    private String pictureUrl;
    private String description;
    private String color;
    @Generated(hash = 286589610)
    public VehicleModel(String id, Integer maxPassengerNum, String name,
            String pictureUrl, String description, String color) {
        this.id = id;
        this.maxPassengerNum = maxPassengerNum;
        this.name = name;
        this.pictureUrl = pictureUrl;
        this.description = description;
        this.color = color;
    }
    @Generated(hash = 1524054240)
    public VehicleModel() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Integer getMaxPassengerNum() {
        return this.maxPassengerNum;
    }
    public void setMaxPassengerNum(Integer maxPassengerNum) {
        this.maxPassengerNum = maxPassengerNum;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPictureUrl() {
        return this.pictureUrl;
    }
    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getColor() {
        return this.color;
    }
    public void setColor(String color) {
        this.color = color;
    }

}
