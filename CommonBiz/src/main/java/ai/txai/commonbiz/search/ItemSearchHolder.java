package ai.txai.commonbiz.search;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ai.txai.commonbiz.databinding.ItemSearchLayoutBinding;

/**
 * Time: 2/25/22
 * Author Hay
 */
public class ItemSearchHolder extends RecyclerView.ViewHolder {

    public ItemSearchLayoutBinding binding;

    public ItemSearchHolder(@NonNull ItemSearchLayoutBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }
}
