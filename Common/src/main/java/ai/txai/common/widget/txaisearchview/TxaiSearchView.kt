package ai.txai.common.widget.txaisearchview

import ai.txai.common.R
import ai.txai.common.utils.AndroidUtils
import ai.txai.commonview.databinding.CommonviewSearchLayoutBinding
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.blankj.utilcode.util.ThreadUtils

class TxaiSearchView: ConstraintLayout {
    private lateinit var binding: CommonviewSearchLayoutBinding

    private var searchListener: TxaiSearchViewListener? = null

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) :
            super(context, attributeSet, defStyle) {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet) {
        val view = inflate(context, R.layout.commonview_search_layout, this)
        binding = CommonviewSearchLayoutBinding.bind(view)
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TxaiSearchView)
        val showSearchIcon = typedArray.getBoolean(R.styleable.TxaiSearchView_showSearchIcon, true)
        if (showSearchIcon) {
            val searchIcon = ResourcesCompat.getDrawable(resources, R.mipmap.commonview_search_ic, null)
            searchIcon?.let { it.setBounds(0, 0, it.minimumWidth, it.minimumHeight) }
            binding.searchView.setCompoundDrawables(searchIcon, null, null, null)
            binding.searchView.compoundDrawablePadding = AndroidUtils.dip2px(context, 5f)
            binding.searchView.setPadding(AndroidUtils.dip2px(context, 10f), 0, 0, 0)
        }

        val hint = typedArray.getResourceId(R.styleable.TxaiSearchView_android_hint, R.string.commonview_hint)
        setSearchHint(hint)
        typedArray.recycle()

        initListener()
    }

    private fun initListener() {
        binding.removeImg.setOnClickListener {
            val searchText = binding.searchView.text
            if (searchText != null && searchText.toString().isNotEmpty()) {
                binding.searchView.setText("")
            }
        }

        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                binding.removeImg.visibility = GONE
                if (editable.toString().isNotEmpty()) {
                    binding.removeImg.visibility = VISIBLE
                }
                searchListener?.onInputAfter(editable)
            }
        })
    }

    fun getSearchText() = binding.searchView.text.toString()

    fun setSearchText(text: String) {
        binding.searchView.setText(text)
    }

    fun setTxaiSearchViewListener(listener: TxaiSearchViewListener) {
        searchListener = listener
    }

    fun setSearchHint(resId: Int) {
        binding.searchView.setHint(resId)
    }

    fun requestSearchFocus() = binding.searchView.requestFocus()

    interface TxaiSearchViewListener {
        fun onInputAfter(editable: Editable)
    }
}