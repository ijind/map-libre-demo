package ai.txai.database.site;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

import ai.txai.database.location.Point;
import ai.txai.database.location.PointConverter;

/**
 * Time: 2/23/22
 * Author Hay
 */
@Entity
public class Site {
    @Id
    private String id;

    @Convert(converter = PointConverter.class, columnType = String.class)
    private Point point;
    private String icon;
    private String name;
    private String areaId;
    private String description;
    private boolean recentlyUsed;
    @Generated(hash = 1765211021)
    public Site(String id, Point point, String icon, String name, String areaId,
            String description, boolean recentlyUsed) {
        this.id = id;
        this.point = point;
        this.icon = icon;
        this.name = name;
        this.areaId = areaId;
        this.description = description;
        this.recentlyUsed = recentlyUsed;
    }
    @Generated(hash = 1136322986)
    public Site() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Point getPoint() {
        return this.point;
    }
    public void setPoint(Point point) {
        this.point = point;
    }
    public String getIcon() {
        return this.icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAreaId() {
        return this.areaId;
    }
    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean getRecentlyUsed() {
        return this.recentlyUsed;
    }
    public void setRecentlyUsed(boolean recentlyUsed) {
        this.recentlyUsed = recentlyUsed;
    }

    
}
