package ai.txai.payment.fragment;

import android.os.Bundle;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.router.ARouterConstants;
import ai.txai.database.utils.CommonData;
import ai.txai.payment.R;
import ai.txai.payment.databinding.PaymentPaybyFragmentBinding;
import ai.txai.payment.request.PayByBean;
import ai.txai.payment.request.PayRequestBean;
import ai.txai.payment.utils.BankUtils;
import ai.txai.payment.utils.PaymentParameterField;
import ai.txai.payment.viewmodel.PaymentViewModel;

/**
 * Time: 07/06/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_PAYBY)
public class PaybyFragment extends BaseScrollFragment<PaymentPaybyFragmentBinding, PaymentViewModel> {

    @Override
    protected int getCustomTitle() {
        return 0;
    }

    @Override
    protected PaymentPaybyFragmentBinding initItemBinding(ViewGroup parent) {
        return PaymentPaybyFragmentBinding.inflate(getLayoutInflater(), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        backgroundColor(R.color.transparent);
        Bundle arguments = getArguments();
        if (arguments == null) {
            viewModel.finish();
            return;
        }

        double amount = arguments.getDouble(PaymentParameterField.EXTRA_AMOUNT);
        String orderId = arguments.getString(PaymentParameterField.EXTRA_ORDER_ID);
        PayRequestBean payBean = PayRequestBean.builder()
                .bizType("0")
                .userId(CommonData.getInstance().currentUser().getUid())
                .bizSubject("Txai")
                .orderId(orderId)
                .totalAmount(amount)
                .payMethod(BankUtils.PAY_BY)
                .payMethodInfo(
                        PayByBean.builder()
                                .ewalletCode("payby").build()
                )
                .build();
        viewModel.confirmPay(payBean, null, null);
    }
}
