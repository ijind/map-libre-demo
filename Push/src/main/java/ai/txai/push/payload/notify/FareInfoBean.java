package ai.txai.push.payload.notify;

import com.google.gson.annotations.SerializedName;

public class FareInfoBean {
    @SerializedName("baseFare")
    public double baseFare;
    @SerializedName("baseMileage")
    public double baseMileage;
    @SerializedName("perMileageSurcharge")
    public double perMileageSurcharge;
    @SerializedName("mileageSurcharge")
    public double mileageSurcharge;
    @SerializedName("totalFare")
    public double totalFare;
    @SerializedName("discountFare")
    public double discountFare;
    @SerializedName("freeOrder")
    public boolean freeOrder;
    @SerializedName("totalMileage")
    public double totalMileage;


    public ai.txai.database.order.bean.FareInfoBean toDBFareInfo() {
        ai.txai.database.order.bean.FareInfoBean fareInfoBean = new ai.txai.database.order.bean.FareInfoBean();
        fareInfoBean.baseFare = baseFare;
        fareInfoBean.baseMileage = baseMileage;
        fareInfoBean.perMileageSurcharge = perMileageSurcharge;
        fareInfoBean.mileageSurcharge = mileageSurcharge;
        fareInfoBean.totalFare = totalFare;
        fareInfoBean.discountFare = discountFare;
        fareInfoBean.freeOrder = freeOrder;
        fareInfoBean.totalMileage = totalMileage;
        return fareInfoBean;
    }
}