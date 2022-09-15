package ai.txai.database.vehicle;

import ai.txai.database.location.Point;

/**
 * Time: 14/03/2022
 * Author Hay
 * 车辆状态，比如位置，行驶方向等
 */
public class VehicleIndicator {
    public String id;
    public int status;
    public Point point;
    public Point prePoint;
    public int eta;
    public double emt;
    public double totalFare;
    public double totalDistance;
    public double discountFare;
    public double startPrice;
    public double startDistance;
    public double perMileagePrice;

    public VehicleIndicator(String id, double longitude, double latitude) {
        this.id = id;
        point = new Point(longitude, latitude);
    }

    public VehicleIndicator(String id) {
        this.id = id;
        point = new Point();
    }

    public void setPoint(Point point) {
        this.prePoint = this.point;
        this.point = point;
    }


    public void setEta(int eta) {
        this.eta = eta;
    }

    public void setEmt(double emt) {
        this.emt = emt;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void setDiscountFare(double discountFare) {
        this.discountFare = discountFare;
    }

    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }

    public void setStartDistance(double startDistance) {
        this.startDistance = startDistance;
    }

    public void setPerMileagePrice(double perMileagePrice) {
        this.perMileagePrice = perMileagePrice;
    }

    public VehicleIndicator deepCopy() {
        VehicleIndicator vehicleIndicator = new VehicleIndicator(id, point.getLongitude(), point.getLatitude());
        if (prePoint != null) {
            vehicleIndicator.prePoint = new Point(prePoint.getLongitude(), prePoint.getLatitude());
        }
        vehicleIndicator.setEta(eta);
        vehicleIndicator.setEmt(emt);
        vehicleIndicator.setTotalFare(totalFare);
        vehicleIndicator.setTotalDistance(totalDistance);
        vehicleIndicator.setDiscountFare(discountFare);
        vehicleIndicator.setStartDistance(startPrice);
        vehicleIndicator.setStartDistance(startDistance);
        vehicleIndicator.setPerMileagePrice(perMileagePrice);
        return vehicleIndicator;
    }
}
