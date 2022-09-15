package ai.txai.common.countrycode

import ai.txai.common.R
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * region code search
 */
class RegionCodeSearchAdapter(
    private val context: Context,
    private val dataList: MutableList<Country>
) : RecyclerView.Adapter<RegionCodeSearchAdapter.RegionCodeVH>() {
    private val inflater: LayoutInflater by lazy { LayoutInflater.from(context) }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RegionCodeVH {
        return RegionCodeVH(inflater.inflate(R.layout.common_region_search_item, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RegionCodeVH, position: Int) {
        val country = dataList[position]
        holder.countryNameTv.text = "${country.getFlag()}  ${country.getName()}"
        holder.countryCodeTv.text = "+${country.getPhoneCode()}"
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class RegionCodeVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val countryNameTv: TextView by lazy { itemView.findViewById(R.id.region_country_name_tv) }
        val countryCodeTv: TextView by lazy { itemView.findViewById(R.id.region_country_code_tv) }
    }
}
