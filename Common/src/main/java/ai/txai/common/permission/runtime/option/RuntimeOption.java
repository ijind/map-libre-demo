package ai.txai.common.permission.runtime.option;

import androidx.annotation.NonNull;

import ai.txai.common.permission.runtime.PermissionDef;
import ai.txai.common.permission.runtime.PermissionRequest;
import ai.txai.common.permission.runtime.setting.SettingRequest;

/**
 */
public interface RuntimeOption {

    /**
     * One or more permissions.
     */
    PermissionRequest permission(@NonNull @PermissionDef String... permissions);

    /**
     * One or more permission groups.
     *
     * @param groups use constants in {@link Permission.Group}.
     */
    PermissionRequest permission(@NonNull String[]... groups);

    /**
     * Permission settings.
     */
    SettingRequest setting();
}