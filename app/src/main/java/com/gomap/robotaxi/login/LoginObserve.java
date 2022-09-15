package com.gomap.robotaxi.login;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gomap.robotaxi.push.PushManager;

import ai.txai.common.external.LoginListener;
import ai.txai.common.network.NetworkObserver;
import ai.txai.common.thread.TScheduler;
import ai.txai.common.utils.DeviceUtils;
import ai.txai.commonbiz.onetrip.SelfDrivingTaxi;
import ai.txai.commonbiz.onetrip.TripModel;
import ai.txai.database.GreenDaoHelper;
import ai.txai.database.enums.TripState;
import ai.txai.database.user.User;
import ai.txai.database.utils.CommonData;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class LoginObserve implements LoginListener, NetworkObserver {
    @Override
    public void whenLogin(User user, boolean dbUserInit) {
        if (dbUserInit) {
            PushManager.tryStartPush();
            SelfDrivingTaxi.getInstance().requestAreas();
        } else {
            TScheduler.INSTANCE.single().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    CommonData.getInstance().whenLogin(user);
                    GreenDaoHelper.getInstance().initUserDb(user.getUid(), DeviceUtils.INSTANCE.getDeviceId());
                }
            });
        }
    }

    @Override
    public void whenLogout() {
        User user = CommonData.getInstance().currentUser();
        if (user == null) {
            return;
        }
        TripModel.getInstance().getTripStateMachine().resetMachine();
        TripModel.getInstance().transferTo(TripState.Idle);
        SelfDrivingTaxi.getInstance().clearData();
        PushManager.stopPush();
        TScheduler.INSTANCE.single().scheduleDirect(new Runnable() {
            @Override
            public void run() {
                GreenDaoHelper.getInstance().closeUserDb(user.getUid());
                CommonData.getInstance().whenLogout();
            }
        });
    }

    @Override
    public void onRefresh() {

    }


    @Override
    public void onConnected(@Nullable NetworkUtils.NetworkType networkType) {
        User user = CommonData.getInstance().currentUser();
        if (user != null) {
            TScheduler.INSTANCE.single().scheduleDirect(new Runnable() {
                @Override
                public void run() {
                    PushManager.tryStartPush();
                    SelfDrivingTaxi.getInstance().requestAreas();
                }
            });
        }
    }

    @Override
    public void onDisconnected() {
    }
}
