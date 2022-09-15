package ai.txai.common.widget.txaiedittext

import ai.txai.common.R
import ai.txai.common.countrycode.Country
import ai.txai.common.countrycode.CountryCodeView
import ai.txai.common.log.LOG
import ai.txai.common.manager.LifeCycleManager
import ai.txai.common.utils.PhoneNumberUtils
import ai.txai.common.utils.setDebounceClickListener
import ai.txai.commonview.databinding.CommonviewAutoSerarationLayoutBinding
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

class AutoSeparationInputView: ConstraintLayout {
    companion object {
        private const val TAG = "AutoSeparationInputView"
        private const val DEFAULT_STYLE = 1
        private const val OUTSIDE_STYLE = 2
    }
    private lateinit var binding: CommonviewAutoSerarationLayoutBinding
    private var countryCodeView: CountryCodeView? = null
    private var statusListener: AutoSeparationStatusListener? = null

    private var isNeedFlags = false
    //Look attrs.xml for this, 1: default 2:outside
    private var showStyle = DEFAULT_STYLE

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) :
            super(context, attributeSet, defStyle) {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet) {
        val view = inflate(context, R.layout.commonview_auto_seraration_layout, this)
        binding = CommonviewAutoSerarationLayoutBinding.bind(view)

        val typeArray = context.obtainStyledAttributes(attributeSet, R.styleable.AutoSeparationInputView)
        isNeedFlags = typeArray.getBoolean(R.styleable.AutoSeparationInputView_isNeedFlags, false)
        showStyle = typeArray.getInteger(R.styleable.AutoSeparationInputView_isoShowStyle, DEFAULT_STYLE)
        initView(typeArray)
        typeArray.recycle()

        initListener()
    }

    private fun initView(typedArray: TypedArray) {
        val defaultIsoCode = typedArray.getString(R.styleable.AutoSeparationInputView_defaultIsoCode)
        if (defaultIsoCode.isNullOrEmpty()) {
            var defaultString = "+971"
            if (isNeedFlags) {
                defaultString = "\uD83C\uDDE6\uD83C\uDDEA  +971"
            }
            binding.isoCodeTv.text = defaultString
        } else {
            binding.isoCodeTv.text = defaultIsoCode
        }

        val isoLeftMargin = typedArray.getDimensionPixelOffset(R.styleable.AutoSeparationInputView_isoMarginLeft, 0)
        val isoLayoutParams = binding.isoCodeTv.layoutParams as MarginLayoutParams
        isoLayoutParams.leftMargin = isoLeftMargin
        binding.isoCodeTv.layoutParams = isoLayoutParams

        val removeRightMargin = typedArray.getDimensionPixelOffset(R.styleable.AutoSeparationInputView_removeMarginRight, 0)
        val removeLayoutParams = binding.inputRemoveImg.layoutParams as MarginLayoutParams
        removeLayoutParams.rightMargin = removeRightMargin
        binding.inputRemoveImg.layoutParams = removeLayoutParams

        initBottomLine(typedArray)
    }

    private fun initBottomLine(typedArray: TypedArray) {
        val showStyle = typedArray.getInteger(R.styleable.AutoSeparationInputView_isoShowStyle, DEFAULT_STYLE)
        if (showStyle == DEFAULT_STYLE) {
            binding.loginEditLine.visibility = View.VISIBLE
        }

        val showLine = typedArray.getBoolean(R.styleable.AutoSeparationInputView_showBottomLine, false)
        if (showLine) {
            binding.loginEditLine.visibility = View.VISIBLE
        }
    }

    private fun initListener() {
        binding.inputPhoneNumber.addTextChangedListener(editListener)
        binding.inputPhoneNumber.setOnFocusChangeListener { _, focus ->
            binding.inputRemoveImg.visibility = View.GONE
            if (focus && binding.inputPhoneNumber.text.toString().trim().isNotEmpty()) {
                binding.inputRemoveImg.visibility = View.VISIBLE
            }
        }
        countryCodeView = LifeCycleManager.getCurActivity()?.let {
            CountryCodeView(it, binding.attachPopView,
                object : CountryCodeView.RegionItemClickListener {
                    @SuppressLint("SetTextI18n")
                    override fun onItemClick(position: Int, region: Country) {
                        var regionTv = "+${region.getPhoneCode()}"
                        if (isNeedFlags) {
                            regionTv = "${region.getFlag()}  +${region.getPhoneCode()}"
                        }
                        binding.isoCodeTv.text = regionTv
                        statusListener?.onIsoCodeChange(regionTv, region.getIsoCode())
                        updatePhoneLengthByCountry(region.getPhoneCode())
                    }
                })
        }

        binding.isoCodeTv.setDebounceClickListener {
            if (showStyle == DEFAULT_STYLE) {
                countryCodeView?.showPop()
            } else if (showStyle == OUTSIDE_STYLE) {
                statusListener?.requestCountries()
            }
        }

        binding.inputRemoveImg.setOnClickListener {
            binding.inputPhoneNumber.setText("")
        }
    }

    private fun updatePhoneLengthByCountry(regionCode: String) {
        val maxLength = PhoneNumberUtils.maxLengthPhoneNumber(regionCode)
        binding.inputPhoneNumber.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        val text = binding.inputPhoneNumber.text
        if (!text.isNullOrEmpty()) {
            val finalString = text.toString().replace(" ", "")
            binding.inputPhoneNumber.setText(finalString)
        }
    }

    private val editListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
             if (s.toString().trim().isEmpty()) {
                 binding.inputPhoneNumber.removeTextChangedListener(this)
                 binding.inputPhoneNumber.setText("")
                 binding.inputPhoneNumber.addTextChangedListener(this)
                 binding.inputRemoveImg.visibility = View.GONE
            } else {
                 binding.inputRemoveImg.visibility = View.VISIBLE
            }
            statusListener?.onInputAfter(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s == null) return

            val phone = s.toString().replace(" ", "")
            val regionTv = binding.isoCodeTv.text
            val isoCode = regionTv.toString().substring(
                regionTv.indexOf("+") + 1,
                regionTv.length
            )
            val formatPhoneNumber = PhoneNumberUtils.formatPhoneNumber(isoCode, phone)
            if (TextUtils.equals(phone, formatPhoneNumber)) {
                binding.inputPhoneNumber.setSelection(phone.length)
                return
            }
            binding.inputPhoneNumber.removeTextChangedListener(this)
            binding.inputPhoneNumber.setText(formatPhoneNumber)
            binding.inputPhoneNumber.addTextChangedListener(this)
            binding.inputPhoneNumber.text?.length?.let { binding.inputPhoneNumber.setSelection(it) }
        }
    }

    private fun buildMobileNumber(): String {
        val regionInfo = binding.isoCodeTv.text.toString()
        if (regionInfo.isEmpty()) return ""
        val regionCode = regionInfo.substring(regionInfo.indexOf('+') + 1, regionInfo.length)
        val phoneNumber = binding.inputPhoneNumber.text.toString().replace(" ", "")
        LOG.d(TAG, "region code $regionCode")
        return "$regionCode $phoneNumber"
    }

    fun removeEditTextWatcher() {
        binding.inputPhoneNumber.removeTextChangedListener(editListener)
    }

    fun setAutoSeparationStatusListener(listener: AutoSeparationStatusListener) {
        statusListener = listener
    }

    /**
     * regionData: include pop countries list (change first letter to #)
     * sourceData: all countries no changes
     */
    fun setRegionData(regionData: MutableList<Country>, sourceData: ArrayList<Country>) {
        countryCodeView?.setRegionData(regionData, sourceData)
    }

    fun setIsoCode(isoString: String) {
        binding.isoCodeTv.text = isoString
        val isoCode = isoString.substring(
            isoString.indexOf("+") + 1,
            isoString.length)
        updatePhoneLengthByCountry(isoCode)
    }

    fun getInputPhoneNumber(): String {
        return binding.inputPhoneNumber.text?.toString() ?: ""
    }

    fun getIsoCode(): String {
        return binding.isoCodeTv.text?.toString() ?: ""
    }

    fun getPhoneNumberWithIsoCode() = buildMobileNumber()

    fun showPop() {
        countryCodeView?.showPop()
    }

    interface AutoSeparationStatusListener {
        fun onInputAfter(text: Editable?)

        fun onIsoCodeChange(areaCode: String, isoCode: String)

        fun requestCountries()
    }
}