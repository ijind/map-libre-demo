package ai.txai.payment.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.base.helper.ScrollBindingHelper;
import ai.txai.common.log.LOG;
import ai.txai.common.router.ARouterConstants;
import ai.txai.commonview.TextWatcherAdapter;
import ai.txai.database.utils.CommonData;
import ai.txai.payment.R;
import ai.txai.payment.databinding.PaymentAddCardFragmentBinding;
import ai.txai.payment.request.BankCardBean;
import ai.txai.payment.request.PayRequestBean;
import ai.txai.payment.utils.BankUtils;
import ai.txai.payment.utils.PaymentParameterField;
import ai.txai.payment.viewmodel.PaymentViewModel;

/**
 * Time: 07/06/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_PAYMENT_ADD_CARD)
public class AddCardFragment extends BaseScrollFragment<PaymentAddCardFragmentBinding, PaymentViewModel> {

    @Override
    protected int getCustomTitle() {
        return R.string.payment_add_card_title;
    }

    @Override
    protected PaymentAddCardFragmentBinding initItemBinding(ViewGroup parent) {
        return PaymentAddCardFragmentBinding.inflate(getLayoutInflater(), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        bottomShadowVisible(false);
        updateConfirmState();
        positiveClickListener(R.string.payment_confirm_payment, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = getArguments();
                if (arguments == null) {
                    return;
                }
                double amount = arguments.getDouble(PaymentParameterField.EXTRA_AMOUNT);
                String orderId = arguments.getString(PaymentParameterField.EXTRA_ORDER_ID);
                String expiry = itemBinding.etExpiryDate.getText().toString();
                String[] split = expiry.split("/");
                boolean saveCard = itemBinding.saveBankCard.isChecked();
                LOG.i("Payment", "saveCard- %s", saveCard);
                String realCardNumber = BankUtils.getRealCardNumber(itemBinding.etCardInput.getText().toString());
                String brand = BankUtils.isVisa(realCardNumber) ? BankUtils.VISA : BankUtils.MASTERCARD;
                viewModel.confirmPay(PayRequestBean.builder()
                        .bizType("0")
                        .userId(CommonData.getInstance().currentUser().getUid())
                        .bizSubject("Txai")
                        .orderId(orderId)
                        .totalAmount(amount)
                        .payMethod(BankUtils.BANK_CARD)
                        .payMethodInfo(BankCardBean.builder()
                                .payType(0)
                                .cardNo(realCardNumber)
                                .holderName(itemBinding.etCardHolderInput.getText().toString())
                                .cvv(itemBinding.etCvv.getText().toString())
                                .exp_year(split[1])
                                .exp_month(split[0])
                                .email(itemBinding.etEmailInput.getText().toString())
                                .saveCard(saveCard)
                                .build())
                        .build(), realCardNumber.substring(realCardNumber.length() - 4), brand);
            }
        });

        itemBinding.etCardInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                String cardNo = editable.toString();
                String formatCardNo = cardNo;

                boolean mayVisa = BankUtils.mayVisa(cardNo);
                boolean mayMaster = BankUtils.mayMaster(cardNo);
                formatCardNo = BankUtils.formatCardNumber(BankUtils.VISA, cardNo);
                if (mayVisa) {
                    itemBinding.ivCurrentCardBranch.setImageResource(R.mipmap.payment_ic_visa_logo);
                    itemBinding.ivCurrentCardBranch.setVisibility(View.VISIBLE);
                } else if (mayMaster) {
                    itemBinding.ivCurrentCardBranch.setImageResource(R.mipmap.payment_ic_master_logo);
                    itemBinding.ivCurrentCardBranch.setVisibility(View.VISIBLE);
                } else {
                    itemBinding.ivCurrentCardBranch.setVisibility(View.GONE);
                }
                if (!TextUtils.equals(cardNo, formatCardNo)) {
                    itemBinding.etCardInput.removeTextChangedListener(this);
                    itemBinding.etCardInput.setText(formatCardNo);
                    itemBinding.etCardInput.setSelection(itemBinding.etCardInput.length());
                    itemBinding.etCardInput.addTextChangedListener(this);
                }

                String realCardNumber = BankUtils.getRealCardNumber(cardNo);
                if (realCardNumber.length() >= 16) {
                    if (BankUtils.isVisa(realCardNumber) || BankUtils.isMaster(BankUtils.getRealCardNumber(cardNo))) {
                        itemBinding.tvCardUnavailable.setVisibility(View.GONE);
                    } else {
                        itemBinding.tvCardUnavailable.setVisibility(View.VISIBLE);
                        itemBinding.ivCurrentCardBranch.setVisibility(View.GONE);
                    }
                } else {
                    itemBinding.tvCardUnavailable.setVisibility(View.GONE);
                }
                updateConfirmState();
            }
        });

        itemBinding.etExpiryDate.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                String expireDate = editable.toString();
                String formatDate = BankUtils.formatExpiry(expireDate);
                if (!TextUtils.equals(expireDate, formatDate)) {
                    itemBinding.etExpiryDate.removeTextChangedListener(this);
                    itemBinding.etExpiryDate.setText(formatDate);
                    itemBinding.etExpiryDate.setSelection(itemBinding.etExpiryDate.length());
                    itemBinding.etExpiryDate.addTextChangedListener(this);
                }
                updateConfirmState();
            }
        });

        itemBinding.etCardHolderInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                updateConfirmState();
            }
        });

        itemBinding.etCvv.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                updateConfirmState();
            }
        });


    }

    private void updateConfirmState() {
        positiveEnableClick(canConfirm());
    }

    public boolean canConfirm() {
        boolean saveCard = itemBinding.saveBankCard.isChecked();
        LOG.i("Payment", "saveCard- %s", saveCard);
        String expireDate = itemBinding.etExpiryDate.getText().toString();
        if (expireDate.length() == 5 && expireDate.contains("/")) {
            String cvv = itemBinding.etCvv.getText().toString();
            if (cvv.length() == 3) {
                String holderName = itemBinding.etCardHolderInput.getText().toString();
                if (!TextUtils.isEmpty(holderName)) {
                    String cardNumber = itemBinding.etCardInput.getText().toString();
                    String realCardNumber = BankUtils.getRealCardNumber(cardNumber);
                    return BankUtils.isVisa(realCardNumber) || BankUtils.isMaster(realCardNumber);
                }
            }
        }
        return false;
    }
}
