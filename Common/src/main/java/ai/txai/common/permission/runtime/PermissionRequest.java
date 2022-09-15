/*
 * Copyright © zhaoyuntao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.txai.common.permission.runtime;



import androidx.annotation.NonNull;

import ai.txai.common.permission.Action;
import ai.txai.common.permission.Rationale;

import java.util.List;

/**
 * <p>Permission request.</p>
 *
 */
public interface PermissionRequest {

    /**
     * One or more permissions.
     */
    PermissionRequest permission(@NonNull @PermissionDef String... permissions);

    /**
     * Set request rationale.
     */
    PermissionRequest rationale(@NonNull Rationale<List<String>> rationale);

    /**
     * Action to be taken when all permissions are granted.
     */
    PermissionRequest onGranted(@NonNull Action<List<String>> granted);

    /**
     * Action to be taken when all permissions are denied.
     */
    PermissionRequest onDenied(@NonNull Action<List<String>> denied);

    /**
     * Request permission.
     */
    void start();
}