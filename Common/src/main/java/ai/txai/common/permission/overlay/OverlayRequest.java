/*
 * Copyright 2018 zhaoyuntao
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
package ai.txai.common.permission.overlay;

import ai.txai.common.permission.Action;
import ai.txai.common.permission.Rationale;

/**
 * Created by Zhaoyuntao on 2018/5/29.
 */
public interface OverlayRequest {

    /**
     * Set request rationale.
     */
    OverlayRequest rationale(Rationale<Void> rationale);

    /**
     * Action to be taken when all permissions are granted.
     */
    OverlayRequest onGranted(Action<Void> granted);

    /**
     * Action to be taken when all permissions are denied.
     */
    OverlayRequest onDenied(Action<Void> denied);

    /**
     * Start request.
     */
    void start();
}