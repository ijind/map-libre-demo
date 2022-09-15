package ai.txai.commonbiz.view

import ai.txai.common.log.LOG
import ai.txai.common.widget.txaiButton.TxaiButton
import ai.txai.commonbiz.R
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BasePopupView
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.enums.PopupPosition

class PrecautionsView(activity: Context) : BottomPopupView(activity) {
    private val tvNoticeList = ArrayList<TextView>()
    private val tvNumberList = ArrayList<TextView>()
    private var popupView: BasePopupView = XPopup.Builder(context)
        .isCenterHorizontal(true)
        .autoFocusEditText(false)
        .popupPosition(PopupPosition.Bottom)
        .asCustom(this)

    override fun getImplLayoutId(): Int {
        return R.layout.biz_precautions_layout
    }

    override fun doAfterShow() {
        super.doAfterShow()
        for (i in 0 until tvNoticeList.size) {
            val tvNotice = tvNoticeList[i]
            val tvNumber = tvNumberList[i]
            if (tvNotice.lineCount >= 2) {
                val lp = tvNumber.layoutParams as MarginLayoutParams
                lp.topMargin = (tvNotice.measuredHeight - tvNumber.measuredHeight) / 2
                tvNumber.layoutParams = lp
            }
        }
        tvNoticeList.clear()
        tvNumberList.clear()
    }

    override fun onCreate() {
        super.onCreate()
        val okView = popupContentView.findViewById<TxaiButton>(R.id.tv_ok)
        okView.setVisibleButtonClickListener {
            dismissPop()
        }

        updateItemView(1, R.string.biz_precautions_item_1, R.id.ll_notice_1)
        updateItemView(2, R.string.biz_precautions_item_2, R.id.ll_notice_2)
        updateItemView(3, R.string.biz_precautions_item_3, R.id.ll_notice_3)
        updateItemView(4, R.string.biz_precautions_item_4, R.id.ll_notice_4)
        updateItemView(5, R.string.biz_precautions_item_5, R.id.ll_notice_5)
        updateItemView(6, R.string.biz_precautions_item_6, R.id.ll_notice_6)
    }

    private fun updateItemView(number: Int, res: Int, id: Int) {
        val llNotice1 = popupContentView.findViewById<View>(id)
        val tvNumber = llNotice1.findViewById<TextView>(R.id.number)
        tvNumber.text = number.toString()
        val tvNotice = llNotice1.findViewById<TextView>(R.id.notice)
        tvNotice.setText(res)
        tvNumberList.add(tvNumber)
        tvNoticeList.add(tvNotice)
    }

    fun showPop() {
        popupView.show()
    }

    fun dismissPop() {
        popupView.dismiss()
    }
}