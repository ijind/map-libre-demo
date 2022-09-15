package ai.txai.lostfound.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.alibaba.android.arouter.facade.annotation.Route;

import ai.txai.common.base.BaseListFragment;
import ai.txai.common.router.ARouterConstants;
import ai.txai.common.router.ARouterUtils;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.FormatUtils;
import ai.txai.lostfound.R;
import ai.txai.lostfound.bean.LostItem;
import ai.txai.lostfound.constant.Constant;
import ai.txai.lostfound.databinding.LfItemLostBinding;
import ai.txai.lostfound.viewmodel.LostFoundListViewModel;

/**
 * Time: 28/04/2022
 * Author Hay
 */
@Route(path = ARouterConstants.PATH_FRAGMENT_LOST_LIST)
public class LostFoundListFragment extends BaseListFragment<LostItem, Integer, LfItemLostBinding, LostFoundListViewModel> {
    @Override
    protected int getCustomTitle() {
        return R.string.lf_lost_process_query;
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        emptyInfo(R.mipmap.lf_img_no_query, R.string.lf_query_list_empty);
    }

    @Override
    protected void updateItem(LfItemLostBinding itemBinding, LostItem t, int pos) {
        itemBinding.getRoot().setOnClickListener(view -> {
            Bundle args = new Bundle();
            args.putString(LostProgressFragment.EXTRA_LOST_ID, t.id);
            ARouterUtils.navigation(getActivity(), ARouterConstants.PATH_FRAGMENT_LOST_PROGRESS, args);
        });

        itemBinding.tvSummary.setText(t.summary);
        itemBinding.tvTime.setText(AndroidUtils.INSTANCE.buildDate(t.createdAt));
        switch (t.status) {
            case Constant.PENDING:
                itemBinding.tvOrderState.setText(R.string.lf_status_pending);
                itemBinding.tvOrderState.setTextColor(getContext().getResources().getColor(R.color.commonview_color_13D59B));
                break;
            case Constant.PROCESSING:
                itemBinding.tvOrderState.setText(R.string.lf_status_processing);
                itemBinding.tvOrderState.setTextColor(getContext().getResources().getColor(R.color.commonview_color_F0A712));
                break;
            case Constant.COMPLETE:
                itemBinding.tvOrderState.setText(R.string.lf_status_complete);
                itemBinding.tvOrderState.setTextColor(getContext().getResources().getColor(R.color.commonview_grey_99));
                break;
        }

    }

    @Override
    protected LfItemLostBinding initItemBinding(ViewGroup parent) {
        return LfItemLostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }
}
