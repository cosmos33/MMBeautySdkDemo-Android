package com.zego.videofilter;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoLogHookCallback;
import com.zego.zegoliveroom.callback.IZegoRoomCallback;
import com.zego.zegoliveroom.entity.ZegoRoomInfo;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;

public class ZegoInitHelper {
    public static final long APP_ID = 1082737666L;
    public static final byte[] APP_SIGN = new byte[]{(byte)0x83,(byte)0x27,(byte)0x55,(byte)0x7c,(byte)0xf4,(byte)0x14,(byte)0x27,(byte)0xd3,(byte)0x1b,(byte)0x3b,(byte)0xc9,(byte)0x8e,(byte)0x8e,(byte)0xb0,(byte)0x2c,(byte)0x15,(byte)0xd7,(byte)0xf2,(byte)0x1f,(byte)0xec,(byte)0x98,(byte)0xae,(byte)0x6f,(byte)0x31,(byte)0x46,(byte)0x25,(byte)0x23,(byte)0xe2,(byte)0xab,(byte)0x13,(byte)0x2d,(byte)0xe1};
    public static final String SERVER_URL = "wss://webliveroom-test.zego.im/ws";
    public static final String LOG_URL = "wss://weblogger-test.zego.im/log";

    public static ZegoLiveRoom zegoLiveRoom = ZGFilterHelper.sharedInstance().getZegoLiveRoom();

    public static void init(Context context){
        ZegoLiveRoom.setTestEnv(true);
        ZegoLiveRoom.setSDKContext(new ZegoLiveRoom.SDKContextEx() {

            @Override
            public long getLogFileSize() {
                return 0;  // 单个日志文件的大小，必须在 [5M, 100M] 之间；当返回 0 时，表示关闭写日志功能，不推荐关闭日志。
            }

            @Override
            public String getSubLogFolder() {
                return null;
            }

            @Override
            public IZegoLogHookCallback getLogHookCallback() {
                return null;
            }

            @Override
            public String getSoFullPath() {

                return null; // return null 表示使用默认方式加载 libzegoliveroom.so
                // 此处可以返回 so 的绝对路径，用来指定从这个位置加载 libzegoliveroom.so，确保应用具备存取此路径的权限
            }

            @Override
            public String getLogPath() {
                return null; //  return null 表示日志文件会存储到默认位置，如果返回非空，则将日志文件存储到该路径下，注意应用必须具备存取该目录的权限
            }

            @Override
            public Application getAppContext() {
                return (Application) context;
            }
        });

//        ZegoLiveRoom.setUser("skin","skinname");

//        initZego();
    }

    // 初始化
    public static void initZego(){
        zegoLiveRoom.initSDK(APP_ID, APP_SIGN, i -> Log.e("zego",i == 0?"初始化成功":"初始化失败"));

        // 房间状态callback
        zegoLiveRoom.setZegoRoomCallback(new IZegoRoomCallback() {

            @Override
            public void onKickOut(int i, String s, String s1) {
                Log.e("zego","踢出房间");
            }

            @Override
            public void onDisconnect(int i, String s) {
                Log.e("zego","房间断开");
            }

            @Override
            public void onReconnect(int i, String s) {
                Log.e("zego","房间重连");
            }

            @Override
            public void onTempBroken(int i, String s) {
            }

            @Override
            public void onRoomInfoUpdated(ZegoRoomInfo zegoRoomInfo, String s) {
                Log.e("zego","房间信息更新"+s);
            }

            @Override
            public void onStreamUpdated(int i, ZegoStreamInfo[] zegoStreamInfos, String s) {
                Log.e("zego","房间流更新"+s);
            }

            @Override
            public void onStreamExtraInfoUpdated(ZegoStreamInfo[] zegoStreamInfos, String s) {

            }

            @Override
            public void onRecvCustomCommand(String s, String s1, String s2, String s3) {
                Log.e("zego","房间接收到自定义command");
            }
        });

    }
}
