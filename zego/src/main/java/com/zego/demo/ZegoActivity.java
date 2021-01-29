package com.zego.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.skiin.zego.R;
import com.zego.zegoliveroom.ZegoLiveRoom;
import com.zego.zegoliveroom.callback.IZegoLivePublisherCallback;
import com.zego.zegoliveroom.callback.IZegoLoginCompletionCallback;
import com.zego.zegoliveroom.entity.ZegoPublishStreamQuality;
import com.zego.zegoliveroom.entity.ZegoStreamInfo;

import java.util.HashMap;

public class ZegoActivity extends AppCompatActivity {
    public static final String ROOM_ID = "skin_left_room";

    public EditText editText ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_zego);
        editText = findViewById(R.id.userId);
    }

    public void onInitBtn(View view){
        ZegoApplication.initZego();
    }

    public void onLogin(View view){
        String userId = editText.getText().toString().trim();
        if(TextUtils.isEmpty(userId)){
            Toast.makeText(this,"登录失败",Toast.LENGTH_SHORT).show();
            return;
        }

        // 设置用户
        ZegoLiveRoom.setUser(userId, userId+"skin");
        // 登录房间
        ZegoApplication.zegoLiveRoom.loginRoom(ROOM_ID, 0, new IZegoLoginCompletionCallback() {

            @Override
            public void onLoginCompletion(int stateCode, ZegoStreamInfo[] zegoStreamInfos) {
                // zegoStreamInfos，内部封装了 userID、userName、streamID 和 extraInfo。
                // 登录房间成功后，开发者可通过 zegoStreamInfos 获取到当前房间推流信息，便于后续的拉流操作。
                // 当 listStream 为 null 时说明当前房间没有人推流
                if (stateCode == 0) {
                    Log.i("zego","登录房间成功 roomId "+ ROOM_ID);
                } else {
                    // 登录房间失败请查看 登录房间错误码，如果错误码是网络问题相关的，App 提示用户稍后再试，或者 App 内部重试登录。
                    Log.i("zego","登录房间失败, stateCode " + (stateCode));
                }
            }
        });
    }

    public void onGotoBeautyAndPublish(View view){
        ZegoApplication.zegoLiveRoom.setZegoLivePublisherCallback(new IZegoLivePublisherCallback(){
            @Override
            public void onPublishStateUpdate(int i, String s, HashMap<String, Object> hashMap) {
                if(i ==0){
                    Log.e("zego","推流成功");
                }else{
                    Log.e("zego","推流失败"+s);
                }
            }

            @Override
            public void onJoinLiveRequest(int i, String s, String s1, String s2) {

            }

            @Override
            public void onPublishQualityUpdate(String s, ZegoPublishStreamQuality zegoPublishStreamQuality) {
                Log.e("zego","推流质量"+s+zegoPublishStreamQuality.toString());
            }

            @Override
            public void onCaptureVideoSizeChangedTo(int i, int i1) {

            }

            @Override
            public void onCaptureVideoFirstFrame() {

            }

            @Override
            public void onCaptureAudioFirstFrame() {

            }
            // 处理推流相关的回调
        });

        // 跳转到直播页面开始推流
    }
}
