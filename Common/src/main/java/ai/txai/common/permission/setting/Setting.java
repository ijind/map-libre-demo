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
package ai.txai.common.permission.setting;

import android.os.Build;

import ai.txai.common.permission.setting.write.LWriteRequestFactory;
import ai.txai.common.permission.setting.write.MWriteRequestFactory;
import ai.txai.common.permission.setting.write.WriteRequest;
import ai.txai.common.permission.source.Source;

/**
 * Created by zhaoyuntao on 3/1/19.
 */
public class Setting {

    private static final SettingRequestFactory SETTING_REQUEST_FACTORY;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SETTING_REQUEST_FACTORY = new MWriteRequestFactory();
        } else {
            SETTING_REQUEST_FACTORY = new LWriteRequestFactory();
        }
    }

    public interface SettingRequestFactory {

        WriteRequest create(Source source);
    }

    private Source mSource;

    public Setting(Source source) {
        this.mSource = source;
    }

    /**
     * Handle write system settings.
     */
    public WriteRequest write() {
        return SETTING_REQUEST_FACTORY.create(mSource);
    }
}