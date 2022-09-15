package com.gomap.robotaxi.push;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ThreadUtils;
import com.gomap.robotaxi.login.GlobalLoginStatusListener;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ai.txai.common.observer.CommonObserver;
import ai.txai.commonbiz.onetrip.TripModel;
import ai.txai.commonbiz.repository.BizApiRepository;
import ai.txai.database.GreenDaoHelper;
import ai.txai.database.user.User;
import ai.txai.database.utils.CommonData;
import ai.txai.push.BlueGoPushClient;
import ai.txai.push.entity.UserInfo;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class PushManager {
    private static final String DEFAULT_IP = "188.116.29.138";
    private static final int DEFAULT_PORT = 9111;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void tryStartPush() {
        executorService.submit(() -> {
            final BlueGoPushClient client = BlueGoPushClient.currentClient();
            if (client != null) {
                return;
            }

            GreenDaoHelper.getInstance().waitUserDb();
            User user = CommonData.getInstance().currentUser();
            BizApiRepository.INSTANCE.pushList()
                    .subscribe(new CommonObserver<>() {
                        @Override
                        public void onSuccess(@Nullable List<String> pushes) {
                            if (pushes == null || pushes.isEmpty()) {
                                return;
                            }

                            String s = pushes.get(0);
                            if (!TextUtils.isEmpty(s) && s.contains(":")) {
                                String[] hosts = s.split(":");
                                startPushToClient(user, hosts[0], Integer.parseInt(hosts[1]));
                                return;
                            }

                            startPushToClient(user, DEFAULT_IP, DEFAULT_PORT);
                        }

                        @Override
                        public void onFailed(@Nullable String msg) {
                            ThreadUtils.getMainHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tryStartPush();
                                }
                            }, 30000);
                        }
                    });
        });
    }

    private static void startPushToClient(User user, String ip, int port) {
        executorService.submit(() -> {
            BlueGoPushClient client = BlueGoPushClient.currentClient();
            if (client != null) {
                return;
            }

            String pushToken = user.getPushToken();
            if (pushToken == null || pushToken.length() != 64) {
                return;
            }

            BlueGoPushClient.startPush(UserInfo.builder().userId(user.getUid())
                    .endpoint("Android")
                    .authKey(pushToken)
                    .build(), ip, port);
            client = BlueGoPushClient.currentClient();
            client.registerPushNotifyListener(TripModel.getInstance());
            client.registerPushStateListener(TripModel.getInstance());
            client.registerPushNotifyListener(GlobalLoginStatusListener.INSTANCE);
        });
    }

    public static void stopPush() {
        executorService.submit(() -> {
            User user = CommonData.getInstance().currentUser();
            final BlueGoPushClient client = BlueGoPushClient.currentClient();
            client.unregisterPushNotifyListener(TripModel.getInstance());
            client.unregisterPushStateListener(TripModel.getInstance());
            client.unregisterPushNotifyListener(GlobalLoginStatusListener.INSTANCE);
            BlueGoPushClient.stopPush(user);
        });
    }
}
