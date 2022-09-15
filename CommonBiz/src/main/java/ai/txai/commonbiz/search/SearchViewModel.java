package ai.txai.commonbiz.search;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseViewModel;
import ai.txai.common.thread.TScheduler;
import ai.txai.commonbiz.data.BizData;
import ai.txai.commonbiz.data.BizHistory;
import ai.txai.commonbiz.data.BizSite;
import ai.txai.database.location.Point;
import ai.txai.database.site.Site;
import ai.txai.database.utils.CommonData;
import ai.txai.mapsdk.utils.MapBoxUtils;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

/**
 * Time: 2/21/22
 * Author Hay
 */
public class SearchViewModel extends BaseViewModel {

    public MutableLiveData<Pair<String, List<Site>>> siteList = new MutableLiveData<>();
    public String keyword;

    public static final String SEARCH_HISTORY_UP_KEY = "search_history_pick_up";
    public static final String SEARCH_HISTORY_OFF_KEY = "search_history_drop_off";

    public SearchViewModel(@NonNull Application application) {
        super(application);
    }


    public void search(String key) {
        this.keyword = key;

        BizData.getInstance().requestSites(new BizData.SiteChangeListener() {
            @Override
            public void onLoaded(List<Site> sites) {
                final ArrayList<Site> searchList = new ArrayList<>();
                for (Site site : sites) {
                    if (site.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        searchList.add(site);
                    }
                }
                siteList.postValue(new Pair<>(keyword, searchList));
            }

            @Override
            public void onFailed(String msg) {
                showToast(msg, false);
            }
        });
    }

    public void saveHistorySite(Site site, boolean isPickUp, Point curPoint) {
        if (site == null || curPoint == null) {
            return;
        }

        String key = SEARCH_HISTORY_UP_KEY;
        if (!isPickUp) key = SEARCH_HISTORY_OFF_KEY;
        BizHistory history = CommonData.getInstance().get(key, BizHistory.class);
        if (history == null) {
            final BizSite bizSite = buildHistoryBizSite(site, curPoint);
            LinkedList<BizSite> queue = new LinkedList<>();
            queue.offer(bizSite);
            history = new BizHistory(queue);
            CommonData.getInstance().put(key, history);
            return;
        }

        final LinkedList<BizSite> histories = history.getList();

        // Search history max count is 2
        if (histories.size() == 2) {
            histories.poll();
        }
        final BizSite newBizSite = buildHistoryBizSite(site, curPoint);
        final BizSite oldBizSite = histories.peek();
        if (oldBizSite != null && !oldBizSite.getSite().getId().equals(site.getId())) {
            histories.offer(newBizSite);
            history.setList(histories);
            CommonData.getInstance().put(key, history);
        }
    }

    public BizSite buildHistoryBizSite(Site site, Point curPoint) {
        final double dis = MapBoxUtils.distance(curPoint, site.getPoint());
        final BizSite bizSite = new BizSite(site, dis);
        return bizSite;
    }

    public List<BizSite> sortByDistance(List<Site> source, Point curPoint) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        final ArrayList<BizSite> siteList = new ArrayList<>();
        for (Site site : source) {
            final double dis = curPoint == null ? -1D : MapBoxUtils.distance(curPoint,
                    site.getPoint());
            siteList.add(new BizSite(site, dis));
        }

        return quickSort(siteList, 0, siteList.size() - 1);
    }

    private List<BizSite> quickSort(ArrayList<BizSite> sites, int left, int right) {
        if (left < right) {
            int partitionIndex = partition(sites, left, right);
            quickSort(sites, left, partitionIndex - 1);
            quickSort(sites, partitionIndex + 1, right);
        }
        return sites;
    }

    private int partition(ArrayList<BizSite> sites, int left, int right) {
        int index = left + 1;
        for (int i = index; i <= right; i++) {
            final double curDis = sites.get(i).getDistance();
            final double pivotDis = sites.get(left).getDistance();
            if (curDis < pivotDis) {
                swap(sites, i, index);
                index++;
            }
        }
        swap(sites, left, index - 1);
        return index - 1;
    }

    private void swap(ArrayList<BizSite> sites, int i, int j) {
        BizSite temp = sites.get(i);
        sites.set(i, sites.get(j));
        sites.set(j, temp);
    }
}
