package ai.txai.payment.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.SizeUtils;

import java.util.HashMap;
import java.util.Map;

import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.base.helper.ScrollBindingHelper;
import ai.txai.common.dialog.DialogCreator;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.utils.ClickDebounceUtilsKt;
import ai.txai.common.widget.VerificationCodeView;
import ai.txai.commonview.leftslidelib.LeftSlideView;
import ai.txai.database.utils.CommonData;
import ai.txai.payment.R;
import ai.txai.payment.activity.PaymentActivity;
import ai.txai.payment.databinding.PaymentCardDeleteLayoutBinding;
import ai.txai.payment.databinding.PaymentCardItemLayoutBinding;
import ai.txai.payment.databinding.PaymentMethodListFragmentBinding;
import ai.txai.payment.provider.PaymentProviderImpl;
import ai.txai.payment.request.BankCardTokenBean;
import ai.txai.payment.request.PayByBean;
import ai.txai.payment.request.PayRequestBean;
import ai.txai.payment.response.PayMethodEntry;
import ai.txai.payment.response.PayMethodInfo;
import ai.txai.payment.response.PayMethodList;
import ai.txai.payment.utils.BankUtils;
import ai.txai.payment.utils.PaymentParameterField;
import ai.txai.payment.utils.ViewHelper;
import ai.txai.payment.view.CvvView;
import ai.txai.payment.viewmodel.PaymentViewModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Time: 07/06/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_PAYMENT_METHOD_LIST)
public class MethodListFragment extends BaseScrollFragment<PaymentMethodListFragmentBinding, PaymentViewModel> {
    public static final String EXTRA_DEFAULT_METHOD_KEY = "extra.default.method";

    private Map<String, PaymentCardItemLayoutBinding> cacheBinds = new HashMap<>();
    private Map<String, LeftSlideView> cacheBankCards = new HashMap<>();

    private String defaultKey;

    private PayMethodInfo selectedCard;
    private String selectedPayMethod;

    @Override
    protected int getCustomTitle() {
        return R.string.payment_method_title;
    }

    @Override
    protected PaymentMethodListFragmentBinding initItemBinding(ViewGroup parent) {
        return PaymentMethodListFragmentBinding.inflate(getLayoutInflater(), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        backgroundColor(R.color.white);
        bottomShadowVisible(false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            defaultKey = arguments.getString(EXTRA_DEFAULT_METHOD_KEY);
        }
        ViewHelper.updateCardItemName(itemBinding.pmDebitCard, R.mipmap.payment_ic_card_set, getString(R.string.payment_credit_card_title));
        ViewHelper.updateCardItemLogoSupport(itemBinding.pmDebitCard, true, R.mipmap.payment_ic_supported_card);
        ViewHelper.updateCardItemName(itemBinding.pmPayby, R.mipmap.payment_ic_payby_logo, getString(R.string.payment_payby_title));

        ViewHelper.updateCardItemName(itemBinding.addNewCard, R.mipmap.payment_ic_add, getString(R.string.payment_add_new_card_title));
        ViewHelper.updateCardItemSelected(itemBinding.addNewCard, false, false);

        positiveClickListener(R.string.payment_title, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle arguments = getArguments();
                if (arguments == null) {
                    return;
                }

                if (selectedCard != null) {
                    if (selectedCard.expired) {
                        DialogCreator.showConfirmDialog(getActivity(), getString(R.string.payment_card_token_expired), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewModel.deleteCard(selectedCard);
                            }
                        });
                        return;
                    }
                }

                double amount = arguments.getDouble(PaymentParameterField.EXTRA_AMOUNT);
                String orderId = arguments.getString(PaymentParameterField.EXTRA_ORDER_ID);


                if (BankUtils.isPayby(selectedPayMethod)) {
                    viewModel.confirmPay(PayRequestBean.builder()
                            .bizType("0")
                            .userId(CommonData.getInstance().currentUser().getUid())
                            .bizSubject("Txai")
                            .orderId(orderId)
                            .totalAmount(amount)
                            .payMethod(BankUtils.PAY_BY)
                            .payMethodInfo(PayByBean.builder()
                                    .ewalletCode("payby").build())
                            .build(), null, null);
                } else if (selectedCard != null) {
                    if (getActivity() instanceof PaymentActivity) {
                        PaymentActivity activity = (PaymentActivity) getActivity();
                        activity.showInputCvvView(cvvCode -> {
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
                                                    .cardToken(selectedCard.cardToken)
                                                    .build()

                                    )
                                    .build();
                            viewModel.confirmPay(payBean, selectedCard.last4, selectedCard.brand);
                        });
                    }
                } else {
                    viewModel.getFragment().postValue(new Pair(ARouterConstants.PATH_FRAGMENT_PAYMENT_ADD_CARD, getArguments()));
                }
            }
        });

        viewModel.getRemoveCard().observe(this, removed -> {
            removeUI(removed);
        });


        viewModel.getCardMethods().observe(this, new Observer<PayMethodList>() {
            @Override
            public void onChanged(PayMethodList methodList) {
                if (methodList == null || methodList.list == null || methodList.list.isEmpty()) {
                    showToast("No available payment method", false);
                    return;
                }
                for (int i = 0; i < methodList.list.size(); i++) {
                    PayMethodList.ListBean listBean = methodList.list.get(i);
                    if (BankUtils.isBankCard(listBean.payMethod)) {
                        itemBinding.pmDebitCard.getRoot().setVisibility(View.VISIBLE);
                        addToCache(listBean.payMethod, itemBinding.pmDebitCard);
                        if (listBean.payMethodInfoList == null || listBean.payMethodInfoList.isEmpty()) {
                            itemBinding.pmDebitCard.ivSelectedBox.setVisibility(View.VISIBLE);
                            if (listBean.defaultX || TextUtils.isEmpty(defaultKey)) {
                                clickSelectPayMethod(listBean.payMethod, null);
                            }
                            ClickDebounceUtilsKt.setDebounceClickListener(itemBinding.pmDebitCard.getRoot(),
                                    v -> clickSelectPayMethod(BankUtils.BANK_CARD, null));
                        } else {
                            itemBinding.pmDebitCard.ivSelectedBox.setVisibility(View.GONE);
                            itemBinding.llDebitCardSet.setVisibility(View.VISIBLE);
                            for (int j = 0; j < listBean.payMethodInfoList.size(); j++) {
                                PayMethodInfo payMethodInfo = listBean.payMethodInfoList.get(j);
                                LeftSlideView slideView = newLeftSideView(payMethodInfo);
                                itemBinding.llDebitCardSet.addView(slideView, j);
                                String tmpKey = formatKey(payMethodInfo);
                                cacheBankCards.put(tmpKey, slideView);
                                if (TextUtils.equals(tmpKey, defaultKey)) {
                                    clickSelectPayMethod(BankUtils.BANK_CARD, payMethodInfo);
                                }
                            }
                        }
                    }
                    if (BankUtils.isPayby(listBean.payMethod)) {
                        addToCache(listBean.payMethod, itemBinding.pmPayby);
                        itemBinding.pmPayby.getRoot().setVisibility(View.VISIBLE);
                        ClickDebounceUtilsKt.setDebounceClickListener(itemBinding.pmPayby.getRoot(), v -> clickSelectPayMethod(listBean.payMethod, null));
                        if (listBean.defaultX) {
                            clickSelectPayMethod(listBean.payMethod, null);
                        }
                    }
                }
            }
        });

        ClickDebounceUtilsKt.setDebounceClickListener(itemBinding.addNewCard.getRoot(), v -> viewModel.getFragment().postValue(new Pair(ARouterConstants.PATH_FRAGMENT_PAYMENT_ADD_CARD, getArguments())));

        viewModel.methodList();
    }

    private void removeUI(PayMethodInfo methodInfo) {
        if (methodInfo == null) {
            return;
        }
        String key = formatKey(methodInfo);
        LeftSlideView slideView = cacheBankCards.remove(key);
        if (slideView == null) {
            return;
        }
        itemBinding.llDebitCardSet.removeView(slideView);
        if (cacheBankCards.isEmpty()) {
            itemBinding.llDebitCardSet.setVisibility(View.GONE);
            itemBinding.pmDebitCard.ivSelectedBox.setVisibility(View.VISIBLE);
            ClickDebounceUtilsKt.setDebounceClickListener(itemBinding.pmDebitCard.getRoot(), v -> clickSelectPayMethod(BankUtils.BANK_CARD, null));
            if (TextUtils.equals(formatKey(selectedCard), key)) {
                clickSelectPayMethod(BankUtils.BANK_CARD, null);
            }
        }
    }

    private void addToCache(String key, PaymentCardItemLayoutBinding binding) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        binding.getRoot().setTag(key);
        cacheBinds.put(key, binding);
    }

    private void selectMethod(String defaultMethod) {
        for (Map.Entry<String, PaymentCardItemLayoutBinding> entry : cacheBinds.entrySet()) {
            entry.getValue().getRoot().setVisibility(View.VISIBLE);
            if (TextUtils.equals(entry.getKey(), defaultMethod)) {
                entry.getValue().ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_selected);
            } else {
                entry.getValue().ivSelectedBox.setImageResource(R.drawable.commonview_ic_box_default);
            }
        }
    }

    public static String formatKey(PayMethodInfo method) {
        if (method == null) {
            return null;
        }
        return method.cardToken;
    }

    public LeftSlideView newLeftSideView(PayMethodInfo methodInfo) {
        final LeftSlideView leftSlideView = new LeftSlideView(getActivity());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT
                , SizeUtils.dp2px(60));
        leftSlideView.setLayoutParams(params);
        PaymentCardItemLayoutBinding cardBinding = PaymentCardItemLayoutBinding.inflate(getLayoutInflater(), itemBinding.llDebitCardSet, false);
        PaymentCardDeleteLayoutBinding deleteCarrBind = PaymentCardDeleteLayoutBinding.inflate(getLayoutInflater(), itemBinding.llDebitCardSet, false);
        cardBinding.getRoot().setPadding(SizeUtils.dp2px(40), 0, SizeUtils.dp2px(20), 0);
        cardBinding.llCardPots.setVisibility(View.VISIBLE);
        ViewHelper.updateCardItemName(cardBinding, BankUtils.bankLogoRes(methodInfo.brand), methodInfo.last4);

        deleteCarrBind.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.deleteCard(methodInfo);
            }
        });

        cardBinding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!leftSlideView.isShowDelete()) {
                    clickSelectPayMethod(null, methodInfo);
                }
            }
        });

        leftSlideView.addContentView(cardBinding.getRoot());
        leftSlideView.addMenuView(deleteCarrBind.getRoot());
        addToCache(formatKey(methodInfo), cardBinding);
        return leftSlideView;
    }

    public void clickSelectPayMethod(String payMethod, PayMethodInfo methodInfo) {
        selectedPayMethod = payMethod;
        selectedCard = methodInfo;
        if (methodInfo == null) {
            selectMethod(selectedPayMethod);
        } else {
            selectMethod(formatKey(selectedCard));
        }
    }
}
