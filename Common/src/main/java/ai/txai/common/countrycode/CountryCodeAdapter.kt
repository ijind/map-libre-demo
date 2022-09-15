package ai.txai.common.countrycode

import ai.txai.common.R
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SectionIndexer
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.LogUtils

class CountryCodeAdapter(private val context: Context,
                         private val dataList: MutableList<Country>):
    BaseAdapter(), SectionIndexer {

    companion object {
        private const val TAG = "CountryCode"
    }
    private var isSpecial = false
    private var clickListener: ItemClickListener? = null

    override fun getCount() = dataList.size

    override fun getItem(position: Int) = dataList[position]

    override fun getItemId(position: Int): Long = 0L

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder? = null
        var codeView: View? = null
        if (convertView == null) {
            codeView = LayoutInflater.from(context).
            inflate(R.layout.common_coutry_code_layout, null)
            holder = ViewHolder()
            holder.letterTitle = codeView.findViewById(R.id.letter_title_tv)
            holder.countryNameTv = codeView.findViewById(R.id.country_name_tv)
            holder.countryCodeTv = codeView.findViewById(R.id.country_code_tv)
            holder.countryInfoLayout = codeView.findViewById(R.id.country_content_layout)
            codeView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            codeView = convertView
        }

        val selectPosition = getSectionForPosition(position)
        val firstItemPosition = getPositionForSection(selectPosition)
        if (position == firstItemPosition) {
            holder.letterTitle?.let {
                it.text = dataList[position].getFirstLetter()
                it.visibility = View.VISIBLE
            }
        } else {
            holder.letterTitle?.visibility = View.GONE
        }

        if (firstItemPosition == -1) {
            holder.letterTitle?.let {
                it.text = "Popular countries"
                it.visibility = if (isSpecial) View.GONE else View.VISIBLE
            }
            isSpecial = true
        } else {
            isSpecial = false
        }

        val country = dataList[position]
        holder.countryInfoLayout?.setOnClickListener { clickListener?.onInnerItemClick(country) }
        holder.countryNameTv?.text = "${country.getFlag()}  ${country.getName()}"
        holder.countryCodeTv?.text = "+${country.getPhoneCode()}"
        return codeView!!
    }

    override fun getSections(): Array<Any> {
        return arrayOf()
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        val char = (sectionIndex + 65).toChar()
        for (i in 0 until dataList.size) {
            val curChar = dataList[i].getFirstLetter().uppercase()[0]
            LogUtils.d(TAG, "get position $curChar char $char")
            if (curChar == char) {
                return i
            }
        }
        return -1
    }

    override fun getSectionForPosition(position: Int): Int {
        val c: Char = dataList[position].getFirstLetter()[0]
        return if (c in 'A'..'Z') {
            c - 'A'
        } else 26
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.clickListener = listener
    }

    interface ItemClickListener {
        fun onInnerItemClick(country: Country)
    }

    class ViewHolder {
        var letterTitle: TextView? = null
        var countryNameTv: TextView? = null
        var countryCodeTv: TextView? = null
        var countryInfoLayout: ConstraintLayout? = null
    }
}