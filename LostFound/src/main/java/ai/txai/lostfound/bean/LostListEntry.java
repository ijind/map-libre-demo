package ai.txai.lostfound.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Time: 10/05/2022
 * Author Hay
 */
public class LostListEntry {

    @SerializedName("data")
    public List<LostItem> data;
    @SerializedName("page")
    public int page;
    @SerializedName("page_size")
    public int pageSize;
    @SerializedName("size")
    public int size;
    @SerializedName("total")
    public int total;
    @SerializedName("total_page")
    public int totalPage;

}
