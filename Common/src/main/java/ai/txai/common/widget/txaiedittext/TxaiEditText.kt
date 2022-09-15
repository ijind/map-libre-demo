package ai.txai.common.widget.txaiedittext

import ai.txai.common.R
import ai.txai.common.widget.txaiedittext.TxaiEditHelper.textCountBuilder
import ai.txai.commonview.databinding.CommonviewMultiLineLayoutBinding
import ai.txai.commonview.databinding.CommonviewSingleLineLayoutBinding
import ai.txai.commonview.databinding.CommonviewSingleListLayoutBinding
import android.content.Context
import android.content.res.TypedArray
import android.text.*
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewbinding.ViewBinding

class TxaiEditText: ConstraintLayout {
    companion object {
        private const val SINGLE_LINE = 1
        private const val SINGLE_LIST = 2
        private const val MULTI_LINE = 3
        private const val MULTI_WITH_CAMERA = 4
    }
    private lateinit var binding: ViewBinding

    private var editStyle: Int = SINGLE_LINE
    private var txaiEditListener: TxaiEditTextListener? = null

    private val removeImg by lazy { getInputRemoveImg() }
    private val editText by lazy { getInputEditText() }
    private val textCountTv by lazy { getCountTv() }
    private val titleTv by lazy { getInputTitleTv() }

    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet) {
        init(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) :
            super(context, attributeSet, defStyle) {
        init(context, attributeSet)
    }

    private fun init(context: Context, attributeSet: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.TxaiEditText)
        initBinding(typedArray)
        initView(typedArray)
        initListener()
        typedArray.recycle()
    }

    private fun initBinding(typedArray: TypedArray) {
        editStyle = typedArray.getInteger(R.styleable.TxaiEditText_editStyle, SINGLE_LINE)
        when(editStyle) {
            SINGLE_LINE -> {
                val view = inflate(context, R.layout.commonview_single_line_layout, this)
                binding = CommonviewSingleLineLayoutBinding.bind(view)
            }
            SINGLE_LIST -> {
                val view = inflate(context, R.layout.commonview_single_list_layout, this)
                binding = CommonviewSingleListLayoutBinding.bind(view)
            }
            MULTI_LINE, MULTI_WITH_CAMERA -> {
                val view = inflate(context, R.layout.commonview_multi_line_layout, this)
                binding = CommonviewMultiLineLayoutBinding.bind(view)
            }
        }
    }

    private fun initView(typedArray: TypedArray) {
        val hint = typedArray.getResourceId(R.styleable.TxaiEditText_inputHint, R.string.commonview_hint)
        val maxLength = typedArray.getInteger(R.styleable.TxaiEditText_android_maxLength, 1000)
        val inputType = typedArray.getInt(R.styleable.TxaiEditText_android_inputType, -1)
        editText?.let {
            it.setHint(hint)
            it.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
            if (inputType != -1 && editStyle != MULTI_LINE && editStyle != MULTI_WITH_CAMERA) {
                it.inputType = inputType
            }
        }

        val inputTitle = typedArray.getString(R.styleable.TxaiEditText_singleInputTitle)
        titleTv?.text = inputTitle ?: ""
    }

    private fun initListener() {
        removeImg?.setOnClickListener { editText?.setText("") }
        editText?.let {
            it.addTextChangedListener(editListener)
            it.setOnFocusChangeListener { _, focus ->
                getEditFocusOperation(focus)?.invoke()
                txaiEditListener?.onFocusChange(focus)
            }
        }
    }

    private val editListener = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            getInputAfterOperation(s)?.invoke()
            txaiEditListener?.onInputAfter(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            getTextChangeOperation(s)?.invoke()
        }
    }

    private fun getInputRemoveImg(): ImageView? = operation(null,
        translateBinding<CommonviewSingleListLayoutBinding>()?.singleListRemoveImg,
        translateBinding<CommonviewSingleLineLayoutBinding>()?.inputRemoveImg)

    private fun getInputEditText(): EditText? = operation(
        translateBinding<CommonviewMultiLineLayoutBinding>()?.inputEt,
        translateBinding<CommonviewSingleListLayoutBinding>()?.singleListEt,
        translateBinding<CommonviewSingleLineLayoutBinding>()?.inputPhoneNumber)

    private fun getCountTv(): TextView? = operation(
        translateBinding<CommonviewMultiLineLayoutBinding>()?.countTv, null,null)

    private fun getInputTitleTv(): TextView? = operation(null,
        translateBinding<CommonviewSingleListLayoutBinding>()?.titleTv, null)

    private fun getInputAfterOperation(s: Editable): (() -> Unit)? = operation(
        { textCountTv?.textCountBuilder(s.trim().length) },
        { removeImgStatusChange(s) },
        { removeImgStatusChange(s) })

    private fun getTextChangeOperation(s: CharSequence?): (() -> Unit)? = operation({
        val text = s?.toString()
        text?.let {
            if (it.startsWith(" ")) {
                editText?.let { et ->
                    et.removeTextChangedListener(editListener)
                    et.setText(it.trim())
                    et.addTextChangedListener(editListener)
                    et.setSelection(it.trim().length)
                }
            }
        }
    }, null, null)

    private fun getEditFocusOperation(focus: Boolean): (() -> Unit)? = operation(
        { removeTrim { textCountTv?.textCountBuilder(it) } },
        { removeImgStatusChangeByFocus(focus) },
        { removeImgStatusChangeByFocus(focus) })

    private fun removeTrim(extend: ((count: Int) -> Unit)? = null) {
        val text = editText?.text
        if (!text.isNullOrEmpty() && text.endsWith(" ")) {
            editText?.let {
                it.removeTextChangedListener(editListener)
                it.setText(text.trim())
                it.addTextChangedListener(editListener)
                it.setSelection(text.trim().length)
                extend?.invoke(text.trim().length)
            }
        }
    }

    private fun removeImgStatusChange(s: Editable) {
        if (s.toString().trim().isEmpty()) {
            removeImg?.visibility = View.GONE
        } else {
            removeImg?.visibility = View.VISIBLE
        }
    }

    private fun removeImgStatusChangeByFocus(focus: Boolean) {
        if (!focus) {
            removeImg?.visibility = View.GONE
            return
        }

        if (editText?.text.toString().trim().isNotEmpty()) {
            removeImg?.visibility = View.VISIBLE
        }
        removeTrim()
    }

    private fun <V> operation(multi: V?, singleList: V?, singleLine: V?): V? = when (editStyle) {
        MULTI_LINE, MULTI_WITH_CAMERA -> { multi }
        SINGLE_LIST -> { singleList }
        else -> { singleLine }
    }

    private inline fun <reified T> translateBinding(): T? {
        if (binding !is T) {
            return null
        }

        return binding as T
    }

    fun getInputText(): String = editText?.text?.trim().toString()

    fun setInputText(text: String) {
        editText?.setText(text)
        if (editText?.hasFocus() == false) removeImgStatusChangeByFocus(false)
    }

    fun setInputHint(resId: Int) {
        editText?.hint = resources.getString(resId)
    }

    fun clear() {
        editText?.removeTextChangedListener(editListener)
    }

    fun setTxaiEditTextListener(listener: TxaiEditTextListener) {
        txaiEditListener = listener
    }

    interface TxaiEditTextListener {
        fun onInputAfter(s: Editable)

        fun onFocusChange(focus: Boolean)
    }
}