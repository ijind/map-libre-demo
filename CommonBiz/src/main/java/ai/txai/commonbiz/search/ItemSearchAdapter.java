package ai.txai.commonbiz.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ai.txai.common.utils.FormatUtils;
import ai.txai.commonbiz.R;
import ai.txai.commonbiz.data.BizSite;
import ai.txai.commonbiz.databinding.ItemSearchLayoutBinding;
import ai.txai.commonview.NoLineClickSpan;
import ai.txai.commonview.items.NoLineItem;
import ai.txai.database.location.Point;
import ai.txai.database.site.Site;

/**
 * Time: 2/25/22
 * Author Hay
 */
public class ItemSearchAdapter extends RecyclerView.Adapter<ItemSearchHolder> {

    private List<BizSite> siteList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    private String keyword;
    private Point point;

    private boolean isSearchMode = false;

    public void setSiteList(List<BizSite> siteList, boolean isSearchMode) {
        this.isSearchMode = isSearchMode;
        if (siteList == null || siteList.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        this.siteList.clear();
        this.siteList.addAll(siteList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemSearchHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSearchLayoutBinding binding = ItemSearchLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ItemSearchHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemSearchHolder holder, int position) {
        Site site = siteList.get(position).getSite();
        NoLineItem item = new NoLineItem(R.color.primary, keyword, null);
        NoLineClickSpan.setClickableSpan(holder.binding.title, holder.binding.getRoot().getContext(),
                site.getName(), item);
        holder.binding.description.setText(site.getDescription());
        holder.binding.icon.setImageResource(R.drawable.common_ic_location_search);
        if (point != null) {
            double distance = siteList.get(position).getDistance();
            if (distance < 0) {
                holder.binding.distance.setVisibility(View.GONE);
                holder.binding.line.setVisibility(View.GONE);
                return;
            }
            holder.binding.distance.setVisibility(View.VISIBLE);
            holder.binding.line.setVisibility(View.VISIBLE);
            final String dis = FormatUtils.INSTANCE.buildDistance(distance);
            holder.binding.distance.setText(dis);
        } else {
            holder.binding.distance.setVisibility(View.GONE);
            holder.binding.line.setVisibility(View.GONE);
        }
        holder.binding.getRoot().setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(holder.itemView, site, position);
            }
        });
        holder.binding.viewLine.setVisibility(View.VISIBLE);
        if (!isSearchMode && position == siteList.size() - 1) {
            holder.binding.viewLine.setVisibility(View.GONE);
        }

        if (!isSearchMode && position == siteList.size() - 1) {
            holder.binding.itemSearchLayout.setBackgroundResource(R.drawable.biz_item_bottom_click_bg);
        }
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setCurrentPoint(Point point) {
        this.point = point;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, Site site, int position);
    }
}
