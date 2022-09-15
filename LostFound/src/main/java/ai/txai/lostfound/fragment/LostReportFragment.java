package ai.txai.lostfound.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.Observer;

import com.alibaba.android.arouter.facade.annotation.Route;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.activity.DispatcherActivity;
import ai.txai.common.base.BaseScrollFragment;
import ai.txai.common.countrycode.Country;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ProviderManager;
import ai.txai.common.router.provider.BizProvider;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.ClickDebounceUtilsKt;
import ai.txai.common.utils.PhoneNumberUtils;
import ai.txai.common.widget.txaiedittext.AutoSeparationInputView;
import ai.txai.database.order.Order;
import ai.txai.lostfound.R;
import ai.txai.lostfound.activity.LostFoundMainActivity;
import ai.txai.lostfound.bean.ItemTypeEntry;
import ai.txai.lostfound.databinding.LfReportFragmentBinding;
import ai.txai.lostfound.dialog.ItemTypeListView;
import ai.txai.lostfound.viewmodel.LostReportViewModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Time: 28/04/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_LOST_REPORT)
public class LostReportFragment extends BaseScrollFragment<LfReportFragmentBinding, LostReportViewModel> {

    private ItemTypeListView listView;
    private ItemTypeEntry selectedEntry;

    private String orderId;

    @Override
    protected int getCustomTitle() {
        return R.string.lf_property_report;
    }

    @Override
    protected LfReportFragmentBinding initItemBinding(ViewGroup parent) {
        return LfReportFragmentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        Bundle args = getArguments();
        if (args != null) {
            orderId = args.getString(LostFoundMainActivity.EXTRA_ORDER_ID);
        }
        positiveClickListener(R.string.lf_lost_submit, view -> {
            final String description = itemBinding.tvDescription.getText().toString().trim();
            if (description.length() < 8) {
                showToast("Item description's length must between 8-1000", false);
                return;
            }

            if (canSubmit()) {
                viewModel.lostAdd(itemBinding.asivContactNumber.getIsoCode(), selectedEntry,
                        itemBinding.tvEmailInput.getText().toString().trim(), description,
                        PhoneNumberUtils.getRealNumber(itemBinding.asivContactNumber.getInputPhoneNumber().trim()),
                        orderId);
            }
        });

        viewModel.getItemTypes().observe(this, itemTypeEntries -> {
            selectItemType(itemTypeEntries);
        });
        viewModel.getCountries().observe(this, countries -> {
            List<Country> popCountryList = new ArrayList<>();
            for (Country country : countries) {
                if (country.isPop()) {
                    Country tmp = new Country();
                    tmp.copyAllValues(country);
                    tmp.setFirstLetter("#");
                    popCountryList.add(tmp);
                }
            }
            ArrayList<Country> finalList = new ArrayList<>();
            finalList.addAll(popCountryList);
            finalList.addAll(countries);
            itemBinding.asivContactNumber.setRegionData(finalList, new ArrayList<>(countries));
            itemBinding.asivContactNumber.showPop();
        });


        viewModel.getOrder().observe(this, new Observer<Order>() {
            @Override
            public void onChanged(Order order) {
                itemBinding.layoutOrderInfo.tvTime.setText(AndroidUtils.INSTANCE.buildDate(order.getCreateTime()));
                if (order.payOrderInfo != null) {
                    itemBinding.layoutOrderInfo.tvAmount.setText(getString(R.string.lf_aed_with_amount,
                            AndroidUtils.INSTANCE.buildAmount(order.payOrderInfo.paidFare)));
                }

                BizProvider bizProvider = ProviderManager.provide(ARouterConstants.PATH_SERVICE_BIZ);
                if (bizProvider != null) {
                    bizProvider.updateVehicleModel(itemBinding.layoutOrderInfo.tvTaxiName, order.getVehicleModelId());
                    bizProvider.updateSiteName(itemBinding.layoutOrderInfo.tvPickUpName, order.getPickUpId());
                    bizProvider.updateSiteName(itemBinding.layoutOrderInfo.tvDropOffName, order.getDropOffId());
                }
            }
        });

        itemBinding.clSelectItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.requestItemTypes();
            }
        });

        itemBinding.asivContactNumber.setAutoSeparationStatusListener(new AutoSeparationInputView.AutoSeparationStatusListener() {

            @Override
            public void requestCountries() {
                viewModel.requestCounties();
            }

            @Override
            public void onIsoCodeChange(@NonNull String areaCode, @NonNull String isoCode) {

            }

            @Override
            public void onInputAfter(@Nullable Editable text) {

            }
        });

        itemBinding.btnChangeTrip.setNegativeClickListener(v ->
                DispatcherActivity.startActivity(ARouterConstants.PATH_ACTIVITY_TRIP_LIST,
                        new Bundle(), getActivity(), new DispatcherActivity.ICallback() {
            @Override
            public void onSuccess(String content) {
                orderId = content;
                viewModel.requestOrderInfo(content);
            }

            @Override
            public void onFailed() {

            }
        }));

        itemBinding.tvDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                itemBinding.tvDescription.removeTextChangedListener(this);
                if (editable.toString().trim().equals("") && length != 0) {
                    itemBinding.tvDescription.setText("");
                } else {
                    itemBinding.tvCurrentLength.setText(String.valueOf(length));
                    updateSubmitStatus();
                }
                updateDescriptionCountStatus(length);
                itemBinding.tvDescription.addTextChangedListener(this);

            }
        });

        itemBinding.tvEmailInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateSubmitStatus();
            }
        });

        viewModel.requestOrderInfo(orderId);

        updateSubmitStatus();
    }

    private boolean canSubmit() {
        if (selectedEntry == null) {
            //showToast("Please select Item Type", false);
            return false;
        } else if (TextUtils.isEmpty(itemBinding.asivContactNumber.getInputPhoneNumber().trim())) {
            //showToast("Please Input contact number", false);
            return false;
        } else if (TextUtils.isEmpty(itemBinding.tvEmailInput.getText().toString())) {
            //showToast("Please Input email", false);
            return false;
        } else if (TextUtils.isEmpty(itemBinding.tvDescription.getText().toString())) {
            return false;
        }

        return true;
    }

    private void updateDescriptionCountStatus(int count) {
        int color = ResourcesCompat.getColor(getResources(), R.color.commonview_grey_c3, null);
        if (count > 0) {
            color = ResourcesCompat.getColor(getResources(), R.color.commonview_grey_99, null);
        }
        itemBinding.tvCurrentLength.setTextColor(color);
    }

    private void selectItemType(List<ItemTypeEntry> value) {
        if (listView == null) {
            listView = new ItemTypeListView(getActivity(), item -> {
                selectedEntry = item;
                showItemType(selectedEntry);
                updateSubmitStatus();
            });

        }
        listView.showPop();
        if (value != null) {
            listView.post(() -> {
                listView.updateItemTypes(value);
            });
        }
    }

    private void showItemType(ItemTypeEntry selectedEntry) {
        if (selectedEntry != null) {
            itemBinding.tvItemType.setText(selectedEntry.name);
        }
    }

    private void updateSubmitStatus() {
        positiveEnableClick(canSubmit());
    }

}
