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
package ai.txai.common.permission.notify.listener;

import ai.txai.common.permission.RequestExecutor;
import ai.txai.common.permission.bridge.BridgeRequest;
import ai.txai.common.permission.bridge.RequestManager;
import ai.txai.common.permission.source.Source;

/**
 * Created by zhaoyuntao on 2018/5/29.
 */
class J2Request extends BaseRequest implements RequestExecutor, BridgeRequest.Callback {

    private Source mSource;

    J2Request(Source source) {
        super(source);
        this.mSource = source;
    }

    @Override
    public void start() {
        if (mSource.canListenerNotification()) {
            callbackSucceed();
        } else {
            showRationale(this);
        }
    }

    @Override
    public void execute() {
        BridgeRequest request = new BridgeRequest(mSource);
        request.setType(BridgeRequest.TYPE_NOTIFY_LISTENER);
        request.setCallback(this);
        RequestManager.get().add(request);
    }

    @Override
    public void cancel() {
        callbackFailed();
    }

    @Override
    public void onCallback() {
        if (mSource.canListenerNotification()) {
            callbackSucceed();
        } else {
            callbackFailed();
        }
    }
}