package ai.txai.payment.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.base.helper.ScrollBindingHelper;
import ai.txai.common.router.ARouterConstants;
import ai.txai.commonview.TextWatcherAdapter;
import ai.txai.database.utils.CommonData;
import ai.txai.payment.R;
import ai.txai.payment.activity.PaymentActivity;
import ai.txai.payment.databinding.PaymentInputCvvFragmentBinding;
import ai.txai.payment.request.BankCardTokenBean;
import ai.txai.payment.request.PayRequestBean;
import ai.txai.payment.utils.BankUtils;
import ai.txai.payment.utils.PaymentParameterField;
import ai.txai.payment.viewmodel.PaymentViewModel;

/**
 * Time: 07/06/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_CVV_INPUT)
public class CvvInputFragment extends BaseScrollFragment<PaymentInputCvvFragmentBinding, PaymentViewModel> {
    @Override
    protected int getCustomTitle() {
        return R.string.payment_info_title;
    }

    @Override
    protected PaymentInputCvvFragmentBinding initItemBinding(ViewGroup parent) {
        return PaymentInputCvvFragmentBinding.inflate(getLayoutInflater(), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        bottomShadowVisible(false);
        ScrollBindingHelper.backImgClickListener(binding, () -> getActivity().onBackPressed());
        positiveClickListener(R.string.payment_confirm_payment, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = getArguments();
                if (arguments == null) {
                    return;
                }
                double amount = arguments.getDouble(PaymentParameterField.EXTRA_AMOUNT);
                String orderId = arguments.getString(PaymentParameterField.EXTRA_ORDER_ID);
                String cardToken = arguments.getString(PaymentParameterField.EXTRA_CARD_TOKEN);
                String cardLast4 = arguments.getString(PaymentParameterField.EXTRA_CARD_LAST_4);
                String brand = arguments.getString(PaymentParameterField.EXTRA_CARD_BRAND);
                var payBean = PayRequestBean.builder()
                        .bizType("0")
                        .userId(CommonData.getInstance().currentUser().getUid())
                        .bizSubject("Txai")
                        .orderId(orderId)
                        .totalAmount(amount)
                        .payMethod(BankUtils.BANK_CARD)
                        .payMethodInfo(
                                BankCardTokenBean.builder()
                                        .payType(1)
                                        .cvv(itemBinding.etCvvInput.getText().toString().trim())
                                        .cardToken(cardToken)
                                        .build()

                        )
                        .build();

                viewModel.confirmPay(payBean, cardLast4, brand);
            }
        });
        itemBinding.etCvvInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(@NonNull Editable s) {
                super.afterTextChanged(s);
                updateConfirmState();
            }
        });

        viewModel.getRemoveCard().observe(this, removed -> {
            FragmentActivity activity = getActivity();
            if (activity instanceof PaymentActivity) {
                activity.onBackPressed();
            }
        });
        updateConfirmState();
    }

    private void updateConfirmState() {
        positiveEnableClick(canConfirm());
    }

    public boolean canConfirm() {
        String cvv = itemBinding.etCvvInput.getText().toString();
        return cvv.length() == 3;
    }
}
