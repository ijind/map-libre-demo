package ai.txai.payment.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ai.txai.common.api.ApiConfig;
import ai.txai.common.base.BaseFragment;
import ai.txai.common.databinding.CommonActivityContainerBinding;
import ai.txai.common.dialog.ConfirmDialog;
import ai.txai.common.dialog.DialogCreator;
import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseMvvmActivity;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.widget.VerificationCodeView;
import ai.txai.database.utils.CommonData;
import ai.txai.payment.R;
import ai.txai.payment.fragment.PaymentWebFragment;
import ai.txai.payment.provider.PaymentProviderImpl;
import ai.txai.payment.request.BankCardTokenBean;
import ai.txai.payment.request.PayRequestBean;
import ai.txai.payment.response.PayMethodInfo;
import ai.txai.payment.response.PayOrder;
import ai.txai.payment.utils.BankUtils;
import ai.txai.payment.utils.PaymentParameterField;
import ai.txai.payment.view.CvvView;
import ai.txai.payment.viewmodel.PaymentViewModel;

/**
 * Time: 13/06/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_ACTIVITY_PAYMENT)
public class PaymentActivity extends BaseMvvmActivity<CommonActivityContainerBinding, PaymentViewModel> {
    public static final String METHOD_INPUT_CVV = "method.input.cvv";
    public static final String METHOD_DELETE_CARD = "method.delete.card";
    private CvvView cvvView;

    private ConfirmDialog deleteConfirmDialog;
    String cardToken;

    @Override
    protected CommonActivityContainerBinding initViewBinding() {
        return CommonActivityContainerBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        viewModel.getFragment().observe(this, new Observer<Pair<String, Bundle>>() {
            @Override
            public void onChanged(Pair<String, Bundle> pair) {
                if (TextUtils.equals(pair.first, PaymentActivity.METHOD_INPUT_CVV)) {
                    showInputCvvView(cvvCode -> {
                        LOG.i("CvvView", "inputDown %s", cvvCode);
                        Bundle arguments = pair.second;
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
                                                .cvv(cvvCode.trim())
                                                .cardToken(cardToken)
                                                .build()

                                )
                                .build();

                        viewModel.confirmPay(payBean, cardLast4, brand);
                    });
                    return;
                } else if (TextUtils.equals(pair.first, PaymentActivity.METHOD_DELETE_CARD)) {
                    cardToken = getIntent().getExtras().getString(PaymentParameterField.EXTRA_CARD_TOKEN);
                    if (deleteConfirmDialog == null) {
                        deleteConfirmDialog = DialogCreator.showConfirmDialog(PaymentActivity.this, getString(R.string.payment_card_token_expired), v -> {
                            PayMethodInfo info = new PayMethodInfo();
                            info.cardToken = cardToken;
                            viewModel.deleteCard(info);
                        });
                    } else {
                        deleteConfirmDialog.showPop();
                    }
                    return;
                }
                switchFragment(pair.first, pair.second);
            }
        });

        viewModel.getRemoveCard().observe(this, methodInfo -> {
            Bundle args = getIntent().getExtras();
            String path = args.getString(PaymentParameterField.EXTRA_FRAGMENT_PATH);
            if (TextUtils.equals(path, PaymentActivity.METHOD_DELETE_CARD)) {
                if (methodInfo == null) {
                    onBackPressed();
                    return;
                }
                viewModel.getFragment().postValue(new Pair(ARouterConstants.PATH_FRAGMENT_PAYMENT_METHOD_LIST, args));
            }
        });

        viewModel.getPayOrderLive().observe(this, new Observer<PayOrder>() {
            @Override
            public void onChanged(PayOrder payOrder) {
                if (payOrder == null) {
                    showToast("unknown error", false);
                    return;
                }
                if (TextUtils.isEmpty(payOrder.interActionUri)) {
                    PaymentProviderImpl.Companion.getMapPaying().put(payOrder.orderId, true);
                    viewModel.finish();
                    return;
                }
                if (BankUtils.isPayby(payOrder.payMethod)) {
                    String encode = "";
                    try {
                        encode = URLEncoder.encode("txai://payment.ai/callback?orderId=" + payOrder.orderId, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    String realUrl = String.format("%s&returnUrl=%s", payOrder.interActionUri, encode);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(realUrl));
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    viewModel.finish();
                } else if (BankUtils.isBankCard(payOrder.payMethod)) {
                    Bundle args = new Bundle();
                    args.putString(PaymentWebFragment.EXTRA_CONTENT, payOrder.interActionUri);
                    viewModel.getFragment().postValue(new Pair(ARouterConstants.PATH_FRAGMENT_WEB, args));
                    hideCvvView();
                }
            }
        });
        Bundle args = getIntent().getExtras();
        String path = args.getString(PaymentParameterField.EXTRA_FRAGMENT_PATH);
        viewModel.getFragment().postValue(new Pair(path, getIntent().getExtras()));
    }

    private void switchFragment(String path, Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        BaseFragment fragment = initFromIntent(path, args);
        if (fragment != null) {
            transaction.setCustomAnimations(R.anim.commonview_page_right_enter, R.anim.commonview_page_left_leave);
            transaction.add(R.id.framelayout_container, fragment, fragment.name());
            transaction.addToBackStack(fragment.name());
            transaction.commit();
        }
    }

    protected BaseFragment initFromIntent(String path, Bundle bundle) {
        Object navigation = ARouter.getInstance().build(path).with(bundle).navigation();
        return (BaseFragment) navigation;
    }

    public void showInputCvvView(VerificationCodeView.InputDownListener listener) {
        if (cvvView == null) {
            cvvView = new CvvView(this);
            cvvView.setDismissListener(backPressed -> {
                if (backPressed && getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    onBackPressed();
                }
            });
        }
        cvvView.showPop();
        cvvView.setInputDownListener(listener);
    }

    public void hideCvvView() {
        if (cvvView != null) {
            cvvView.dismissPop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideCvvView();
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            viewModel.finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
