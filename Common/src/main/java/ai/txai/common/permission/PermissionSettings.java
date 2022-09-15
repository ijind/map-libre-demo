package ai.txai.common.permission;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.StringRes;

/**
 * created by zhaoyuntao
 * on 2019-11-08
 * description:
 * Go to application permission setting page
 */
public abstract class PermissionSettings {

    @StringRes
    private int descriptionNormal;
    @StringRes
    private int descriptionAlwaysDeny;
    private String[] permission;

    private boolean isAlwaysDeny;

    private ClickEvent clickEvent;

    public abstract void whenDeny();

    public abstract void whenContinue();

    public void goToAppSettings(Context context) {
        if (context != null) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri packageURI = Uri.fromParts("package", context.getPackageName(), null);
            intent.setData(packageURI);
            context.startActivity(intent);
        }
    }

    public String[] getPermission() {
        return permission;
    }

    public void setPermission(String[] permission) {
        this.permission = permission;
    }

    public void setAlwaysDeny(boolean alwaysDeny) {
        isAlwaysDeny = alwaysDeny;
    }

    public boolean isAlwaysDeny() {
        return isAlwaysDeny;
    }

    public void setClickEvent(ClickEvent clickEvent) {
        this.clickEvent = clickEvent;
    }

    public ClickEvent getClickEvent() {
        return clickEvent;
    }

    public void performClickNegative() {
        if (clickEvent != null) {
            clickEvent.whenClickNegative();
        }
    }

    public void performClickPositive(boolean isAlwaysDeny) {
        if (clickEvent != null) {
            clickEvent.whenClickPositive(isAlwaysDeny);
        }
    }

    public void setDialogDescription(@StringRes int descriptionNormalResId, @StringRes int descriptionAlwaysDenyResId) {
        this.descriptionNormal = descriptionNormalResId;
        this.descriptionAlwaysDeny = descriptionAlwaysDenyResId;
    }

    public int getDescriptionNormal() {
        return descriptionNormal;
    }

    public int getDescriptionAlwaysDeny() {
        return descriptionAlwaysDeny;
    }

    public interface ClickEvent {
        void whenClickNegative();

        void whenClickPositive(boolean isAlwaysDeny);
    }
}
