package ai.txai.database.router;

import java.util.List;

import ai.txai.database.location.Point;

/**
 * Time: 2/23/22
 * Author Hay
 */
public class Router {
    private String id;

    private List<Point> path;

    public Router() {
    }

    public Router(List<Point> path) {
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Point> getPath() {
        return path;
    }

    public void setPath(List<Point> path) {
        this.path = path;
    }

    public void addPoint(Point point) {
        path.add(point);
    }
}
