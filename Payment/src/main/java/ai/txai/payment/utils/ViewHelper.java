package ai.txai.payment.utils;

import android.view.View;

import ai.txai.payment.databinding.PaymentCardItemLayoutBinding;

/**
 * Time: 07/06/2022
 * Author Hay
 */
public class ViewHelper {

    public static void updateCardItemName(PaymentCardItemLayoutBinding binding, int logoRes, String name) {
        binding.ivCardLogo.setImageResource(logoRes);
        binding.tvCardName.setText(name);
    }

    public static void updateCardItemLogoSupport(PaymentCardItemLayoutBinding binding, boolean hasSupport, int picRes) {
        binding.ivCardLogoSupported.setVisibility(hasSupport ? View.VISIBLE : View.GONE);
        binding.ivCardLogoSupported.setImageResource(picRes);
    }

    public static void updateCardItemSelected(PaymentCardItemLayoutBinding binding, boolean containBox, boolean isSelected) {
        if (containBox) {
            binding.ivSelectedBox.setVisibility(View.VISIBLE);
            binding.ivSelectedBox.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        } else {
            binding.ivSelectedBox.setVisibility(View.GONE);
        }
    }
}
