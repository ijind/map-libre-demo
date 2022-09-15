/*
 * Copyright 2019 zhaoyuntao
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
package ai.txai.common.permission.option;

import ai.txai.common.permission.install.InstallRequest;
import ai.txai.common.permission.notify.option.NotifyOption;
import ai.txai.common.permission.overlay.OverlayRequest;
import ai.txai.common.permission.runtime.option.RuntimeOption;
import ai.txai.common.permission.setting.Setting;

/**
 * Created by zhaoyuntao on 2/22/19.
 */
public interface Option {

    /**
     * Handle runtime permissions.
     */
    RuntimeOption runtime();

    /**
     * Handle request package install permission.
     */
    InstallRequest install();

    /**
     * Handle overlay permission.
     */
    OverlayRequest overlay();

    /**
     * Handle notification permission.
     */
    NotifyOption notification();

    /**
     * Handle system setting.
     */
    Setting setting();
}