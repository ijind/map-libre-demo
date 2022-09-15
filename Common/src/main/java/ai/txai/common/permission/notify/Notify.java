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
package ai.txai.common.permission.notify;

import android.os.Build;

import ai.txai.common.permission.notify.listener.J1RequestFactory;
import ai.txai.common.permission.notify.listener.J2RequestFactory;
import ai.txai.common.permission.notify.listener.ListenerRequest;
import ai.txai.common.permission.notify.option.NotifyOption;
import ai.txai.common.permission.source.Source;

/**
 * Created by zhaoyuntao on 2/22/19.
 */
public class Notify implements NotifyOption {

    private static final PermissionRequestFactory PERMISSION_REQUEST_FACTORY;
    private static final ListenerRequestFactory LISTENER_REQUEST_FACTORY;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PERMISSION_REQUEST_FACTORY = new ORequestFactory();
        } else {
            PERMISSION_REQUEST_FACTORY = new NRequestFactory();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            LISTENER_REQUEST_FACTORY = new J2RequestFactory();
        } else {
            LISTENER_REQUEST_FACTORY = new J1RequestFactory();
        }
    }

    public interface PermissionRequestFactory {

        /**
         * Create notify request.
         */
        PermissionRequest create(Source source);
    }

    public interface ListenerRequestFactory {

        /**
         * Create notification listener request.
         */
        ListenerRequest create(Source source);
    }

    private Source mSource;

    public Notify(Source source) {
        this.mSource = source;
    }

    public PermissionRequest permission() {
        return PERMISSION_REQUEST_FACTORY.create(mSource);
    }

    public ListenerRequest listener() {
        return LISTENER_REQUEST_FACTORY.create(mSource);
    }
}