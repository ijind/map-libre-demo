package ai.txai.feedback.view;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SizeUtils;

import ai.txai.common.base.BaseListFragment;
import ai.txai.common.utils.AndroidUtils;
import ai.txai.common.utils.ClickDebounceUtilsKt;
import ai.txai.feedback.R;
import ai.txai.feedback.data.IssueInfo;
import ai.txai.feedback.databinding.FeedbackIssueItemLayoutBinding;
import ai.txai.feedback.viewmodel.FeedbackViewModel;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Time: 28/06/2022
 * Author Hay
 */
public class ChooseIssueFragmentV2 extends BaseListFragment<IssueInfo, Integer, FeedbackIssueItemLayoutBinding, FeedbackViewModel> {
    @Override
    protected int getCustomTitle() {
        return R.string.feedback_report;
    }

    @Override
    public void initViewObservable() {
        super.initViewObservable();
        recyclerBackgroundColor(R.color.white);
        setRecyclerHeaderPaddingLeft(SizeUtils.dp2px(20));
        ClickDebounceUtilsKt.setDebounceClickListener(binding.settingBackImg, v -> getActivity().onBackPressed());
    }

    @Override
    protected void goneEmptyView() {
        super.goneEmptyView();
        backgroundColor(R.color.white);
    }

    @Override
    protected void initItemDecorations(RecyclerView recyclerView) {
        recyclerView.addItemDecoration(new RecyclerSectionItemDecoration(SizeUtils.dp2px(20),
                true, getSectionCallback()));
    }

    @Override
    protected void updateItem(FeedbackIssueItemLayoutBinding itemBinding, IssueInfo t, int pos) {
        itemBinding.chooseIssueItem.setItemTitle(t.getTitle());
        ClickDebounceUtilsKt.setDebounceClickListener(itemBinding.chooseIssueItem, v -> {
            viewModel.getFeedbackStatus().postValue(FeedbackViewModel.ISSUE_DETAIL);
            viewModel.getReportIssueType().postValue(t.getValue());
        });
    }

    @Override
    protected FeedbackIssueItemLayoutBinding initItemBinding(ViewGroup parent) {
        return FeedbackIssueItemLayoutBinding.inflate(getLayoutInflater(), parent, false);
    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback() {
        return new RecyclerSectionItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                return position == 1;
            }

            @NonNull
            @Override
            public CharSequence getSectionHeader(int position) {
                return getActivity().getString(R.string.feedback_choose_category);
            }
        };
    }
}
