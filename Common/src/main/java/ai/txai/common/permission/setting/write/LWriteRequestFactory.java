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
package ai.txai.common.permission.setting.write;


import ai.txai.common.permission.setting.Setting;
import ai.txai.common.permission.source.Source;

public class LWriteRequestFactory implements Setting.SettingRequestFactory {

    @Override
    public WriteRequest create(Source source) {
        return new LWriteRequest(source);
    }
}