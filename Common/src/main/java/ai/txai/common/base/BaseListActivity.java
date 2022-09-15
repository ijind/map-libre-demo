package ai.txai.common.base;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;

import ai.txai.common.R;
import ai.txai.common.databinding.CommonFragmentContentListBinding;
import ai.txai.common.log.LOG;
import ai.txai.common.mvvm.BaseMvvmActivity;
import ai.txai.common.parallaxrecyclerview.ParallaxRecyclerAdapter;
import ai.txai.commonview.SpacesItemDecoration;
import ai.txai.commonview.observablescrollview.ObservableScrollViewCallbacks;
import ai.txai.commonview.observablescrollview.ScrollState;
import ai.txai.commonview.observablescrollview.ScrollUtils;

/**
 * Time: 15/03/2022
 * Author Hay
 *
 * @param <E>  每一项的Bean
 * @param <F>  分页下一页标志，
 * @param <VB> 每一项的 ViewBinding （对应变量名 itemBinding）
 * @param <VM> ViewModel
 */
public abstract class BaseListActivity<E, F, VB extends ViewBinding, VM extends BaseListViewModel<E,F>> extends BaseMvvmActivity<CommonFragmentContentListBinding, VM> implements ObservableScrollViewCallbacks {
    protected boolean hasNext;
    protected MyAdapter adapter;

    @Override
    protected CommonFragmentContentListBinding initViewBinding() {
        return CommonFragmentContentListBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initViewObservable() {
        binding.settingBackImg.setOnClickListener(view -> onBackPressed());

        binding.contentRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.contentRecycler.addItemDecoration(new SpacesItemDecoration(SizeUtils.dp2px(12)));
        adapter = createAdapter(binding.contentRecycler);
        binding.contentRecycler.setAdapter(adapter);
        binding.contentRefreshLayout.setEnableRefresh(false);

        binding.contentRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                if (hasNext) {
                    viewModel.loadData(viewModel.flag, true);
                } else {
                    binding.contentRefreshLayout.finishLoadMore();
                }
            }
        });

        binding.contentRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                viewModel.loadData(null, false);
            }
        });

        viewModel.getRefresh().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean b) {
                if (Boolean.TRUE.equals(b)) {
//                    addList(viewModel.getList(), false);
                    setList(viewModel.getList());
                }
            }
        });

        viewModel.loadMore.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                addList(viewModel.getList(), false);
            }
        });

        viewModel.getHasMore().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                hasNext = aBoolean;
                if (!hasNext) {
                    binding.contentRefreshLayout.setEnableLoadMore(false);
                }
            }
        });

        viewModel.loadData(null, false);

        int title = getCustomTitle();
        if (title > 0) {
            binding.toolbarTitleTv.setText(title);
            binding.titleTv.setText(title);
        }
        binding.contentRecycler.addScrollViewCallbacks(this);

        ScrollUtils.addOnGlobalLayoutListener(binding.toolbarTitleTv, new Runnable() {
            @Override
            public void run() {
                updateFlexibleSpaceText(binding.contentRecycler.getCurrentScrollY());
            }
        });
    }

    protected abstract int getCustomTitle();

    protected abstract void updateItem(VB itemBinding, E t, int pos);

    protected abstract VB initItemBinding(ViewGroup parent);

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    protected class MyAdapter extends ParallaxRecyclerAdapter<E> {
        public MyAdapter() {
        }

        @Override
        public void onBindViewHolderImpl(RecyclerView.ViewHolder holder, ParallaxRecyclerAdapter<E> adapter, int position) {
            E item = adapter.getData().get(position);
            if (holder instanceof MyHolder) {
                ((MyHolder) holder).bindData(item, position);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolderImpl(ViewGroup parent, ParallaxRecyclerAdapter<E> adapter, int position) {
            VB bind = initItemBinding(parent);
            return new MyHolder<E, VB>(bind) {

                @Override
                protected void updateItem() {
                    BaseListActivity.this.updateItem(getItemBinding(), getT(), getPos());
                }
            };
        }

        @Override
        public int getItemCountImpl(ParallaxRecyclerAdapter<E> adapter) {
            return adapter.getData().size();
        }
    }


    protected static abstract class MyHolder<E, VB extends ViewBinding> extends RecyclerView.ViewHolder {

        private E t;
        private VB itemBinding;
        private int pos;

        public MyHolder(@NonNull VB binding) {
            super(binding.getRoot());
            this.itemBinding = binding;
        }

        public void bindData(E item, int pos) {
            t = item;
            this.pos = pos;
            updateItem();
        }

        public E getT() {
            return t;
        }

        public VB getItemBinding() {
            return itemBinding;
        }

        public int getPos() {
            return pos;
        }

        protected abstract void updateItem();
    }

    public void addList(List<E> list, boolean front) {
        binding.contentRefreshLayout.finishLoadMore();
        if (list == null || list.isEmpty()) {
            return;
        }
        goneEmptyView();
        adapter.addData(list, front);
    }

    private void goneEmptyView() {
        binding.emptyView.getRoot().setVisibility(View.GONE);
        backgroundColor(R.color.commonview_grey_f6);
    }

    public void finishRefresh() {

        binding.contentRefreshLayout.finishRefresh();
    }

    public void finishLoadMore() {
        binding.contentRefreshLayout.finishLoadMore();
    }

    public void setList(List<E> list) {
        binding.contentRefreshLayout.finishRefresh();
        if (list == null || list.isEmpty()) {
            emptyLoad();
            return;
        }
        goneEmptyView();
        adapter.setData(list);
    }

    public void empty(String text) {
        binding.emptyView.getRoot().setVisibility(View.VISIBLE);
    }

    public void emptyLoad() {
        backgroundColor(R.color.white);
        binding.emptyView.getRoot().setVisibility(View.VISIBLE);
    }


    private MyAdapter createAdapter(RecyclerView recyclerView) {
        MyAdapter adapter = new MyAdapter();
        adapter.setOnParallaxScroll(new ParallaxRecyclerAdapter.OnParallaxScroll() {
            @Override
            public void onParallaxScroll(float percentage, float offset, View parallax) {
                if (percentage >= 0.7) {
                    int color = ColorUtils.setAlphaComponent(Color.parseColor("#040818"), percentage);
                    binding.toolbarTitleTv.setTextColor(color);
                } else {
                    binding.toolbarTitleTv.setTextColor(Color.parseColor("#00040818"));
                }
            }
        });
        View header = getLayoutInflater().inflate(R.layout.common_recycler_header, recyclerView, false);
        TextView titleTv = header.findViewById(R.id.title_tv);
        titleTv.setText(getCustomTitle());
        adapter.setParallaxHeader(header, recyclerView);
        return adapter;
    }

    private void updateFlexibleSpaceText(final int scrollY) {
        float scale = scrollY * 1f / SizeUtils.dp2px(33.5F);
        LOG.i("Scroll", "updateFlexibleSpaceText %s-%s-%s", scale, scrollY, SizeUtils.dp2px(33.5F));
        if (scale >= 1) {
            binding.toolbarTitleTv.setTextColor(Color.parseColor("#040818"));
        } else if (scale <= 0) {
            binding.toolbarTitleTv.setTextColor(Color.parseColor("#00040818"));
        } else {
            int color = ColorUtils.setAlphaComponent(Color.parseColor("#040818"), scale);
            binding.toolbarTitleTv.setTextColor(color);
        }
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateFlexibleSpaceText(scrollY);
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    protected void backgroundColor(@ColorRes int colorRes) {
        binding.getRoot().setBackgroundResource(colorRes);
    }

    protected void emptyInfo(int imgRes, int textRes) {
        binding.emptyView.noTripIc.setImageResource(imgRes);
        binding.emptyView.noTripTv.setText(textRes);
    }
}
