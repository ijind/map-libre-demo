package ai.txai.common.countrycode

import ai.txai.common.R
import ai.txai.common.log.LOG
import ai.txai.common.widget.txaisearchview.TxaiSearchView
import android.annotation.SuppressLint
import android.app.Activity
import android.text.Editable
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ImmersionBar
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.enums.PopupAnimation

@SuppressLint("ViewConstructor")
class CountryCodeView(
    activity: Activity, view: View,
    val regionItemClickListener: RegionItemClickListener?):
    BasePopupView(activity) {

    companion object {
        private const val TAG = "CountryCodeView"
    }
    private lateinit var itemSearch: TxaiSearchView
    private lateinit var groupView: ListView
    private lateinit var infoView: RecyclerView

    private var popupView: BasePopupView = XPopup.Builder(activity)
        .autoFocusEditText(false)
        .hasShadowBg(false)
        .isTouchThrough(true)
        .customAnimator(TranslateAnimator(view, PopupAnimation.TranslateAlphaFromBottom))
        .dismissOnTouchOutside(false)
        .moveUpToKeyboard(false)
        .asCustom(this)

    private val noPopCountryData: MutableList<Country> = ArrayList()
    private val regionInfoList: MutableList<Country> = ArrayList()
    private val keyInfoList: MutableList<Country> = ArrayList()
    private val noPopCountryHolderList: MutableList<Country> = ArrayList()
    private val regionCodeAdapter by lazy { RegionCodeSearchAdapter(context, noPopCountryData) }
    private val regionGroupAdapter by lazy { CountryCodeAdapter(context, regionInfoList) }

    override fun onCreate() {
        ImmersionBar.with(context as Activity, dialog)
            .transparentStatusBar()
            .statusBarColor(android.R.color.transparent)
            .statusBarAlpha(0.0f)
            .statusBarDarkFont(true)
            .init()
        initView(popupContentView)
    }

    private fun initView(view: View) {
        val backImg = view.findViewById<ImageView>(R.id.back_img)
        backImg.setOnClickListener { dismiss() }
        itemSearch = view.findViewById(R.id.region_item_search)

        groupView = view.findViewById(R.id.region_group_view)
        groupView.adapter = regionGroupAdapter

        regionGroupAdapter.setItemClickListener(object: CountryCodeAdapter.ItemClickListener {
            override fun onInnerItemClick(country: Country) {
                regionItemClickListener?.onItemClick(-1, country)
                dismiss()
            }
        })

        infoView = view.findViewById(R.id.region_info_view)
        infoView.adapter = regionCodeAdapter
        infoView.addOnItemTouchListener(
            RecyclerItemClickListener(context,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val country = noPopCountryData[position]
                        regionItemClickListener?.onItemClick(position, country)
                        dismiss()
                    }
                })
        )

        val sideBar = view.findViewById<SideBar>(R.id.side_bar)
        sideBar.setOnTouchingLetterChangedListener (object: SideBar.OnTouchingLetterChangedListener {
            override fun onTouchingLetterChanged(sectionIndex: Int) {
                val letter = SideBar.sAlphabet[sectionIndex]
                var index = 0
                if (letter != "#") {
                    index = regionGroupAdapter.getPositionForSection(sectionIndex - 1)
                }
                groupView.setSelection(index)
                LOG.d(TAG,"touching letter $letter index $index")
            }
        })
    }

    override fun doAfterDismiss() {
        super.doAfterDismiss()
        itemSearch.setSearchText("")
    }

    override fun doAfterShow() {
        super.doAfterShow()
        itemSearch.setTxaiSearchViewListener(object: TxaiSearchView.TxaiSearchViewListener {
            override fun onInputAfter(editable: Editable) {
                infoView.visibility = if (editable.isEmpty()) View.GONE else View.VISIBLE
                groupView.visibility = if (editable.isEmpty()) View.VISIBLE else View.GONE
                val key = itemSearch.getSearchText()
                if (key.trim().isNotEmpty()) {
                    noPopCountryData.clear()
                    noPopCountryData.addAll(noPopCountryHolderList)
                    for (region in noPopCountryData) {
                        if (region.getPhoneCode().contains(key) || region.getName().contains(key, true)) {
                            keyInfoList.add(region)
                        }
                    }
                    noPopCountryData.clear()
                    noPopCountryData.addAll(keyInfoList)
                    keyInfoList.clear()
                    regionCodeAdapter.notifyDataSetChanged()
                } else {
                    noPopCountryData.clear()
                    noPopCountryData.addAll(noPopCountryHolderList)
                    regionCodeAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun getInnerLayoutId(): Int {
        return R.layout.common_region_layout
    }

    fun showPop() {
        popupView.show()
    }

    fun setRegionData(regionData: MutableList<Country>, sourceData: ArrayList<Country>) {
        this.noPopCountryData.clear()
        this.noPopCountryData.addAll(sourceData)

        regionInfoList.clear()
        regionInfoList.addAll(regionData)

        noPopCountryHolderList.clear()
        noPopCountryHolderList.addAll(sourceData)

        regionCodeAdapter.notifyDataSetChanged()
        regionGroupAdapter.notifyDataSetChanged()
    }

    interface RegionItemClickListener {
        fun onItemClick(position: Int, region: Country)
    }
}