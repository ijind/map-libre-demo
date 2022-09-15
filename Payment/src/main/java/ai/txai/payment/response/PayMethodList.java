package ai.txai.payment.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Time: 06/06/2022
 * Author Hay
 */
public class PayMethodList {

    @SerializedName("list")
    public List<ListBean> list;

    public static class ListBean {
        @SerializedName("pay_method")
        public String payMethod;
        @SerializedName("pay_method_info_list")
        public List<PayMethodInfo> payMethodInfoList;
        @SerializedName("default")
        public boolean defaultX;
        @SerializedName("seq_no")
        public int seqNo;
    }
}
