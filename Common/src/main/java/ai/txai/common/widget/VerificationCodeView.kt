package ai.txai.common.widget

import ai.txai.common.R
import ai.txai.common.utils.LogUtils
import ai.txai.common.widget.txaiedittext.TxaiEditText
import ai.txai.commonview.databinding.CommonviewSmsCodeViewBinding
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout


class VerificationCodeView : ConstraintLayout, View.OnFocusChangeListener, TextWatcher, View.OnKeyListener {
    companion object {
        private const val TAG = "SMSCodeView"
        private const val DELETE_TYPE = 0
        private const val INPUT_TYPE = 1
        private const val DOWN_TYPE = 2
    }
    private lateinit var binding: CommonviewSmsCodeViewBinding
    private var inputListener: InputDownListener? = null
    private var type = INPUT_TYPE
    private val editList = ArrayList<EditText>()

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        initView(context, attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) :
            super(context, attributeSet, defStyleAttr) {
        initView(context, attributeSet)
    }

    private fun initView(context: Context, attributeSet: AttributeSet) {
        binding = CommonviewSmsCodeViewBinding.inflate(LayoutInflater.from(context), this, true)
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.VerificationCodeView)
        var count = typedArray.getInt(R.styleable.VerificationCodeView_codeCount, 4)
        if (count > 4 || count <= 0) {
            count = 4
        }
        for (i in 0 until count) {
            initEditList(i + 1)
        }

        val inputType = typedArray.getInt(R.styleable.VerificationCodeView_android_inputType, -1)
        if (inputType >= 0) {
            editList.forEach {
                it.inputType = inputType
            }
        }
        typedArray.recycle()
        editList.forEach {
            it.addTextChangedListener(this)
            it.onFocusChangeListener = this
            it.setOnKeyListener(this)
        }
        for (i in 0 until editList.size) {
            editList[i].tag = i
        }
        binding.loginSmsFir.isFocusable = true
        binding.loginSmsFir.requestFocus()
    }

    private fun initEditList(count: Int) {
        when(count) {
            4 -> {
                binding.loginSmsFou.visibility = VISIBLE
                editList.add(binding.loginSmsFou)
            }
            3 -> {
                binding.loginSmsThi.visibility = VISIBLE
                editList.add(binding.loginSmsThi)
            }
            2 -> {
                binding.loginSmsSec.visibility = VISIBLE
                editList.add(binding.loginSmsSec)
            }
            1 -> {
                binding.loginSmsFir.visibility = VISIBLE
                editList.add(binding.loginSmsFir)
            }
        }
    }

    override fun onFocusChange(view: View?, focus: Boolean) {
        if (focus) {
            val edit = (view as EditText)
            edit.requestFocus()
            onFocus()
        }
    }

    private fun buildSmsCode(): String {
        var smsCode = ""
        editList.forEach {
            smsCode += "${it.text}"
        }
        return smsCode
    }

    private fun onFocus() {
        if (type == DOWN_TYPE) return
        if (type == INPUT_TYPE) {
            inputType()
        } else {
            deleteType()
        }
    }

    private fun inputType() {
        editList.forEach {
            if (it.text.isEmpty()) {
                it.requestFocus()
                return
            } else {
                it.isCursorVisible = true
            }
        }
        val lastEdit = editList[editList.size - 1]
        if (lastEdit.text.isNotEmpty()) {
            lastEdit.isCursorVisible = false
            lastEdit.clearFocus()
            val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            inputManager.hideSoftInputFromWindow(lastEdit.windowToken, 0)
            inputListener?.onInputDown(buildSmsCode())
            type = DOWN_TYPE
        }
    }

    private fun deleteType() {
        var start = editList.size - 1
        editList.forEach {
            if (it.isFocused) start = it.tag as Int
        }
        for (i in start downTo  0 step 1) {
            val edit = editList[i]
            edit.isCursorVisible = true
            edit.setSelectAllOnFocus(true)
            //LogUtils.d(TAG, "delete $i")
            if (edit.text.isNotEmpty()) {
                edit.requestFocus()
                return
            } else {
                continue
            }
        }
    }

    override fun afterTextChanged(editable: Editable?) {
        editable?.let {
            if (it.isNotEmpty()) {
                type = INPUT_TYPE
            } else {
                type = DELETE_TYPE
                LogUtils.d(TAG, "change text ...$it")
            }
            onFocus()
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }


    override fun onKey(p0: View?, code: Int, ev: KeyEvent?): Boolean {
        if (ev == null) return false
        if (code == KeyEvent.KEYCODE_DEL && ev.action == KeyEvent.ACTION_DOWN) {
            clearText()
        }
        return false
    }

    fun clearText() {
        editList.forEach {
            if (it.isFocused) {
                it.setText("")
            }
        }
    }

    fun smsFirFocusable() {
        binding.loginSmsFir.isFocusable = true
        binding.loginSmsFir.requestFocus()
    }

    fun setInputDownListener(listener: InputDownListener) {
        inputListener = listener
    }

    interface InputDownListener {
        fun onInputDown(smsCode: String)
    }
}