package ai.txai.commonbiz.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.activity.DispatcherActivity;
import ai.txai.common.json.GsonManager;
import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseMvvmActivity;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.FormatUtils;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.bean.AreaResponse;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.data.BizHistory;
import ai.txai.commonbiz.data.BizSite;
import ai.txai.commonbiz.databinding.ActivityLocationSearchBinding;
import ai.txai.commonbiz.databinding.ItemSearchLayoutBinding;
import ai.txai.commonbiz.main.TripDetailsActivity;
import ai.txai.database.location.Point;
import ai.txai.database.site.Site;
import ai.txai.database.utils.CommonData;

/**
 * Time: 2/21/22
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_ACTIVITY_SEARCH)
public class SearchActivity extends BaseMvvmActivity<ActivityLocationSearchBinding, SearchViewModel> {
    private static final String TAG = "SearchActivity";
    public static final int REQUEST_CODE = 1001;
    public static final String EXTRA_CONTENT = "extra.content";
    public static final String EXTRA_FIRST_PAGE = "extra.first.page";//是否是选择站点的时候，跳的第一个界面
    public static final String EXTRA_CURRENT_POINT = "extra.current.point";
    public static final String EXTRA_IS_PICK_UP = "extra.is.pick.up";
    public static final String EXTRA_PICK_UP_SITE = "extra.pickup.site";
    public static final String EXTRA_DROP_OFF_SITE = "extra.drop.off.site";

    private boolean firstPage = true;
    private boolean isPickUp = false;
    private Point currentPoint;
    private String pickupId;
    private String dropOffId;

    boolean isSearchMode = false;
    private ItemSearchAdapter adapter;

    private OptionSearch.SearchRunnable searchRunnable = new OptionSearch.SearchRunnable() {

        @Override
        public void search(String keyword) {
            viewModel.search(keyword);
        }
    };

    private OptionSearch optionSearch;

    @Override
    protected ActivityLocationSearchBinding initViewBinding() {
        return ActivityLocationSearchBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        Bundle args = getIntent().getExtras();

        if (args != null) {
            firstPage = args.getBoolean(SearchActivity.EXTRA_FIRST_PAGE, true);
            isPickUp = args.getBoolean(SearchActivity.EXTRA_IS_PICK_UP);
            String strPoint = args.getString(SearchActivity.EXTRA_CURRENT_POINT);
            currentPoint = GsonManager.fromJsonObject(strPoint, Point.class);
            pickupId = args.getString(SearchActivity.EXTRA_PICK_UP_SITE);
            dropOffId = args.getString(SearchActivity.EXTRA_DROP_OFF_SITE);
        }
        optionSearch = new OptionSearch(searchRunnable);
        initSearchView();
        initSearchHistoryView();
        initRecycleView();
        viewModel.search("");

        viewModel.siteList.observe(this, pair -> {
            adapter.setKeyword(pair.first);
            final List<BizSite> sites = viewModel.sortByDistance(pair.second, getCurPoint());
            adapter.setSiteList(sites, isSearchMode);
        });

        binding.toMapSelect.setVisibleButtonClickListener(v -> {
            if (firstPage) {
                Bundle bundle;
                if (args != null) {
                    bundle = new Bundle(args);
                } else {
                    bundle = new Bundle();
                }
                bundle.putBoolean(EXTRA_FIRST_PAGE, false);
                bundle.putBoolean(TripDetailsActivity.EXTRA_BACK_ACTION, true);
                ARouterUtils.navigationWithBundleForResult(SearchActivity.this, ARouterConstants.PATH_ACTIVITY_SEARCH_IN_MAP, bundle, SearchInMapActivity.REQUEST_CODE);
            } else {
                finish();
            }
        });

        binding.searchBackImg.setOnClickListener(v -> finish());
        addSoftKeyObserver();
    }

    @Override
    public void onDisconnected() {
        binding.statusView.setVisibility(View.VISIBLE);
        binding.statusView.loadError();
    }

    @Override
    public void onConnected(@Nullable NetworkUtils.NetworkType networkType) {
        binding.statusView.setVisibility(View.GONE);
        binding.statusView.loadSuccess();
    }

    private void initSearchView() {
        ThreadUtils.runOnUiThreadDelayed(() -> {
            binding.searchView.requestSearchFocus();
            KeyboardUtils.showSoftInput(this);
        }, 100);

        int resId = R.string.biz_search_drop_off;
        if (isPickUp) resId = R.string.biz_search_pick_up;
        binding.searchView.setSearchHint(resId);

        binding.searchView.setTxaiSearchViewListener(editable -> {
            optionSearch.optionSearch(editable.toString());
            if (!editable.toString().isEmpty()) {
                isSearchMode = true;
                binding.searchSiteView.setVisibility(View.VISIBLE);
                binding.allSiteContainer.setVisibility(View.GONE);
            } else {
                isSearchMode = false;
                ThreadUtils.runOnUiThreadDelayed(() -> {
                    binding.searchSiteView.setVisibility(View.GONE);
                    binding.allSiteContainer.setVisibility(View.VISIBLE);
                }, 10);
            }
        });
    }

    private void initRecycleView() {
        adapter = new ItemSearchAdapter();
        final Point point = getCurPoint();
        if (point != null) {
            adapter.setCurrentPoint(point);
        }
        adapter.setOnItemClickListener((view, site, position) ->
                afterSelectedItem(site, isPickUp));

        binding.searchSiteView.setLayoutManager(new LinearLayoutManager(this));
        binding.searchSiteView.setAdapter(adapter);

        binding.allSiteView.setAdapter(adapter);
        binding.allSiteView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initSearchHistoryView() {
        String key = SearchViewModel.SEARCH_HISTORY_UP_KEY;
        if (!isPickUp) key = SearchViewModel.SEARCH_HISTORY_OFF_KEY;
        final BizHistory history = CommonData.getInstance().get(key, BizHistory.class);
        if (history == null) {
            return;
        }

        initHistoryBg(history.getList());
        final List<BizSite> tempList = new ArrayList<>();
        // max size is 2
        for (int i = history.getList().size() - 1; i >= 0; i--) {
            BizSite history1 = history.getList().get(i);
            final BizSite curHistoryDis = viewModel.buildHistoryBizSite(history1.getSite(), getCurPoint());
            if (curHistoryDis.getDistance() != history1.getDistance()) {
                history1 = curHistoryDis;
                tempList.add(history1);
            }
            ItemSearchLayoutBinding itemBinding = binding.searchHistory2;
            if (i == 1) {
                itemBinding = binding.searchHistory1;
            }
            binding.searchHistoryContainer.setVisibility(View.VISIBLE);
            updateSearchHistoryItem(itemBinding, history1);
        }
        updateSearchHistoryCache(tempList);
    }

    private void initHistoryBg(@NonNull List<BizSite> siteList) {
        binding.searchHistory1.itemSearchLayout.setVisibility(View.GONE);
        binding.searchHistory2.itemSearchLayout.setVisibility(View.GONE);
        int history1BgId = R.drawable.biz_all_corner_10_click_bg;
        int history2BgId = R.drawable.biz_all_corner_10_click_bg;
        if (siteList.size() > 1) {
            history1BgId = R.drawable.biz_item_top_click_bg;
            history2BgId = R.drawable.biz_item_bottom_click_bg;
        }
        binding.searchHistory1.itemSearchLayout.setBackgroundResource(history1BgId);
        binding.searchHistory2.itemSearchLayout.setBackgroundResource(history2BgId);
        binding.searchHistory2.viewLine.setBackgroundColor(Color.TRANSPARENT);
    }

    private void updateSearchHistoryItem(ItemSearchLayoutBinding binding, BizSite site) {
        binding.itemSearchLayout.setVisibility(View.VISIBLE);
        binding.itemSearchLayout.setOnClickListener(view ->
                afterSelectedItem(site.getSite(), isPickUp));
        binding.icon.setImageResource(R.drawable.common_ic_recent_search);
        final String dis = FormatUtils.INSTANCE.buildDistance(site.getDistance());
        binding.distance.setText(dis);
        binding.title.setText(site.getSite().getName());
        binding.description.setText(site.getSite().getDescription());
    }

    private void updateSearchHistoryCache(@NonNull List<BizSite> siteList) {
        if (siteList.isEmpty()) {
            return;
        }

        for (int i = siteList.size() - 1; i >= 0; i--) {
            final BizSite curHistorySite = siteList.get(i);
            viewModel.saveHistorySite(curHistorySite.getSite(), isPickUp, getCurPoint());
        }
    }

    private Point getCurPoint() {
        return currentPoint;
    }

    private void addSoftKeyObserver() {
        KeyboardUtils.registerSoftInputChangedListener(this, height -> {
            LOG.d(TAG, "height %s", height);
            final int bottom = binding.toMapSelect.getBottom();
            final int decorViewHeight = this.getWindow().getDecorView().getHeight();
            if (height > 0) {
                binding.toMapSelect.setY(decorViewHeight - height -
                        binding.toMapSelect.getMeasuredHeight() -
                        AndroidUtils.INSTANCE.dip2px(this, 10));
            } else {
                binding.toMapSelect.setY(bottom - binding.toMapSelect.getMeasuredHeight());
            }
        });
    }

    private void afterSelectedItem(Site site, boolean isPickUp) {
        if ((isPickUp && TextUtils.equals(site.getId(), dropOffId))
                || (!isPickUp && TextUtils.equals(site.getId(), pickupId))) {
            showToast(getString(R.string.biz_pickup_and_dropoff_cannot_same), false);
            return;
        }
        AreaResponse area = BizData.getInstance().getArea(site.getAreaId());
        String s = AndroidUtils.INSTANCE.buildDateHour(System.currentTimeMillis());
        if (FormatUtils.INSTANCE.compareHour(s, area.opStartTime) > 0
                && FormatUtils.INSTANCE.compareHour(s, area.opEndTime) < 0) {
            viewModel.saveHistorySite(site, isPickUp, getCurPoint());
            finishForResult(site.getId());
        } else {
            showToast(getString(R.string.biz_station_out_of_hours), false);
        }
    }

    private void finishForResult(String content) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONTENT, content);
        intent.putExtra(EXTRA_IS_PICK_UP, isPickUp);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode) {
            if (requestCode == SearchInMapActivity.REQUEST_CODE) {
                String content = data.getStringExtra(EXTRA_CONTENT);
                finishForResult(content);
            }
        }
    }
}
