package com.zego.common.application;

import android.app.Application;
import android.content.Context;

import com.zego.common.ZGBaseHelper;
import com.zego.common.util.DeviceInfoManager;

import java.util.Date;

//import android.support.multidex.MultiDexApplication;

/**
 * Created by zego on 2018/10/16.
 */

public class ZegoApplication  {

    public static Application zegoApplication;
    public static String cosmosID;

    public static void onCreate(Context context,String cosmosId){
        cosmosID = cosmosId;
        zegoApplication = (Application) context;
        String randomSuffix = "-" + new Date().getTime() % (new Date().getTime() / 1000);
//
        String userId = DeviceInfoManager.generateDeviceId(zegoApplication) + randomSuffix;
        String userName = DeviceInfoManager.getProductName() + randomSuffix;

        // 添加悬浮日志视图
//        FloatingView.get().add();

        // 使用Zego sdk前必须先设置SDKContext。
        ZGBaseHelper.sharedInstance().setSDKContextEx(userId, userName, null, null, 10 * 1024 * 1024, zegoApplication);
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        zegoApplication = this;
//
//        String randomSuffix = "-" + new Date().getTime()%(new Date().getTime()/1000);
//
//        String userId = DeviceInfoManager.generateDeviceId(this) + randomSuffix;
//        String userName = DeviceInfoManager.getProductName() + randomSuffix;
//
//        // 添加悬浮日志视图
//        FloatingView.get().add();
//
//        // 使用Zego sdk前必须先设置SDKContext。
//        ZGBaseHelper.sharedInstance().setSDKContextEx(userId, userName, null, null, 10 * 1024 * 1024, this);
//
//        AppLogger.getInstance().i(ZegoApplication.class, "SDK version : %s",  ZegoLiveRoom.version());
//        AppLogger.getInstance().i(ZegoApplication.class, "VE version : %s",  ZegoLiveRoom.version2());
//
//        // bugly初始化用户id
//        CrashReport.initCrashReport(getApplicationContext(), "7ace07528f", false);
//        CrashReport.setUserId(userId);
//    }
}
