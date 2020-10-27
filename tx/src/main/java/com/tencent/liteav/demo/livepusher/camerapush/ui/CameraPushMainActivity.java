package com.tencent.liteav.demo.livepusher.camerapush.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;

import com.tencent.liteav.demo.livepusher.R;
import com.tencent.liteav.demo.livepusher.camerapush.CameraPushCustomVideoCaptureImpl;
import com.tencent.liteav.demo.livepusher.camerapush.model.CameraPush;
import com.tencent.liteav.demo.livepusher.camerapush.model.Constants;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePusher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云 {@link TXLivePusher} 推流器使用参考 Demo
 * <p>
 * 有以下功能参考 ：
 * <p>
 * - 基本功能参考： 启动推流 {@link CameraPush#startPush()} 与 结束推流 {@link CameraPush#stopPush()} ()}
 * <p>
 * - 性能数据查看参考： {@link #onNetStatus(Bundle)}
 * <p>
 * - 处理 SDK 回调事件参考： {@link #onPushEvent(int, Bundle)}
 * <p>
 * - 美颜面板：{@link BeautyPanel}
 * <p>
 * - BGM 面板：{@link AudioEffectPanel}
 * <p>
 * - 画质选择：{@link PusherVideoQualityFragment}
 * <p>
 * - 混响、变声、码率自适应、硬件加速等使用参考： {@link PusherSettingFragment} 与 {@link PusherSettingFragment.OnSettingChangeListener}
 */
public class CameraPushMainActivity extends FragmentActivity implements CameraPush.OnLivePusherCallback, CameraPushCustomVideoCaptureImpl.OnDataAvailableListener {

    private static final String TAG = "LivePusherMainActivity";

    private static final int REQUEST_CODE = 100;

    private CameraPush mLivePusher;

    private Button mBtnStartPush;                 // 开启推流的按钮

    private String mPusherURL = "rtmp://172.16.139.32:1935/myapp/txliveandroid";   // 推流地址

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.livepusher_activity_live_pusher_main);
        checkPublishPermission();  // 检查权限
        initData();                // 初始化数据
        initPusher();              // 初始化 SDK 推流器

        // 进入页面，自动开始推流，并且弹出推流对应的拉流地址
        mLivePusher.startPush();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLivePusher.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLivePusher.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLivePusher.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLivePusher.setRotationForActivity(); // Activity 旋转
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.livepusher_btn_start) {
            mLivePusher.togglePush();
        } else if (id == R.id.livepusher_btn_switch_camera) {
            // 表明当前是前摄像头
            if (view.getTag() == null || (Boolean) view.getTag()) {
                view.setTag(false);
                view.setBackgroundResource(R.drawable.livepusher_camera_back_btn);
            } else {
                view.setTag(true);
                view.setBackgroundResource(R.drawable.livepusher_camera_front);
            }
            mLivePusher.switchCamera();
        }
    }

    /**
     * 推流器状态回调
     *
     * @param event 事件id.id类型请参考 {@linkplain TXLiveConstants#PLAY_EVT_CONNECT_SUCC 推流事件列表}.
     * @param param
     */
    @Override
    public void onPushEvent(int event, Bundle param) {
        String msg = param.getString(TXLiveConstants.EVT_DESCRIPTION);
        String pushEventLog = getString(R.string.livepusher_receive_event) + event + ", " + msg;
        Log.d(TAG, pushEventLog);

        // Toast错误内容
        if (event < 0) {
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        }
        if (event == TXLiveConstants.PUSH_WARNING_HW_ACCELERATION_FAIL) {
            // 开启硬件加速失败
            Toast.makeText(getApplicationContext(), param.getString(TXLiveConstants.EVT_DESCRIPTION), Toast.LENGTH_SHORT).show();
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_RESOLUTION) {
            Log.d(TAG, "change resolution to " + param.getInt(TXLiveConstants.EVT_PARAM2) + ", bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_EVT_CHANGE_BITRATE) {
            Log.d(TAG, "change bitrate to" + param.getInt(TXLiveConstants.EVT_PARAM1));
        } else if (event == TXLiveConstants.PUSH_WARNING_NET_BUSY) {
            showNetBusyTips();
        }
    }

    @Override
    public void onNetStatus(Bundle status) {
        Log.d(TAG, "Current status, CPU:" + status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE) +
                ", RES:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT) +
                ", SPD:" + status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps" +
                ", FPS:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS) +
                ", ARA:" + status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps" +
                ", VRA:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps");
    }

    @Override
    public void onActivityRotationObserverChange(boolean selfChange) {

    }

    @Override
    public void onPushStart(int code) {
        Log.d(TAG, "onPusherStart: code -> " + code);
        switch (code) {
            case Constants.PLAY_STATUS_SUCCESS:
                mBtnStartPush.setBackgroundResource(R.drawable.livepusher_pause);
                break;
            case Constants.PLAY_STATUS_INVALID_URL:
                Toast.makeText(getApplicationContext(), "url_illegal", Toast.LENGTH_SHORT).show();
                break;
            case Constants.PLAY_STATUS_LICENSE_ERROR:
                String errInfo = getString(R.string.livepusher_license_check_fail);
                int start = (errInfo + getString(R.string.livepusher_license_click_info)).length();
                int end = (errInfo + getString(R.string.livepusher_license_click_use_info)).length();
                SpannableStringBuilder spannableStrBuidler = new SpannableStringBuilder(errInfo + getString(R.string.livepusher_license_click_use_info));
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://cloud.tencent.com/document/product/454/34750");
                        intent.setData(content_url);
                        startActivity(intent);
                    }
                };
                spannableStrBuidler.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableStrBuidler.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                TextView tv = new TextView(this);
                tv.setMovementMethod(LinkMovementMethod.getInstance());
                tv.setText(spannableStrBuidler);
                tv.setPadding(20, 0, 20, 0);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle("push_fail").setView(tv).setPositiveButton("comfirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLivePusher.stopPush();
                    }
                });
                dialogBuilder.show();
            default:
                break;
        }
    }

    @Override
    public void onPushResume() {
    }

    @Override
    public void onPushPause() {
    }

    @Override
    public void onPushStop() {
        mBtnStartPush.setBackgroundResource(R.drawable.livepusher_start);
    }

    @Override
    public void onSnapshot(File file) {
        if (mLivePusher.isPushing()) {
            if (file != null && file.exists() && file.length() > 0) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);//设置分享行为
                Uri uri = getUri(this, "com.tencent.liteav.demo", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "分享"));
            } else {
                Toast.makeText(CameraPushMainActivity.this, "截屏失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CameraPushMainActivity.this, "screenshot_fail_push", Toast.LENGTH_SHORT).show();
        }
    }

    private void initData() {
        mBtnStartPush = findViewById(R.id.livepusher_btn_start);
    }

    /**
     * 初始化 SDK 推流器
     */
    private void initPusher() {
        mLivePusher = CameraPushImplFactory.INSTANCE.createPushImpl(this, findViewById(R.id.livepusher_tx_cloud_view), this.getWindowManager().getDefaultDisplay().getRotation());
        mLivePusher.setMute(false);
        mLivePusher.setMirror(false);
        mLivePusher.setWatermark(true);
        mLivePusher.setTouchFocus(false);
        mLivePusher.setEnableZoom(false);
        mLivePusher.enablePureAudioPush(false);
        mLivePusher.enableAudioEarMonitoring(true);
        mLivePusher.setQuality(true, TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION);
        mLivePusher.setAudioQuality(2, 48000);
        mLivePusher.setHomeOrientation(false);
        mLivePusher.turnOnFlashLight(false);
        mLivePusher.setHardwareAcceleration(false);
        mLivePusher.setOnLivePusherCallback(this);
        mLivePusher.setURL(mPusherURL);
        if (mLivePusher instanceof CameraPushCustomVideoCaptureImpl) {
            ((CameraPushCustomVideoCaptureImpl) mLivePusher).setOnDataAvailableListener(this);

        }
    }

    /**
     * 显示网络繁忙的提示
     */
    private void showNetBusyTips() {
        mBtnStartPush.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraPushMainActivity.this, "网络繁忙", Toast.LENGTH_SHORT).show();
            }
        }, 5000);
    }

    private boolean checkPublishPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    private Uri getUri(Context context, String authority, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //设置7.0以上共享文件，分享路径定义在xml/file_paths.xml
            uri = FileProvider.getUriForFile(context, authority, file);
        } else {
            // 7.0以下,共享文件
            uri = Uri.fromFile(file);
        }
        return uri;
    }


    /**
     * 判断系统 "自动旋转" 设置功能是否打开
     *
     * @return false---Activity可根据重力感应自动旋转
     */
    private boolean isActivityCanRotation(Context context) {
        int flag = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
        return flag != 0;
    }

    /**
     * 获取当前推流状态
     *
     * @param status
     * @return
     */
    private String getStatus(Bundle status) {
        String str = String.format("%-14s %-14s %-12s\n%-8s %-8s %-8s %-8s\n%-14s %-14s %-12s\n%-14s %-14s",
                "CPU:" + status.getString(TXLiveConstants.NET_STATUS_CPU_USAGE),
                "RES:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_WIDTH) + "*" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_HEIGHT),
                "SPD:" + status.getInt(TXLiveConstants.NET_STATUS_NET_SPEED) + "Kbps",
                "JIT:" + status.getInt(TXLiveConstants.NET_STATUS_NET_JITTER),
                "FPS:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_FPS),
                "GOP:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_GOP) + "s",
                "ARA:" + status.getInt(TXLiveConstants.NET_STATUS_AUDIO_BITRATE) + "Kbps",
                "QUE:" + status.getInt(TXLiveConstants.NET_STATUS_AUDIO_CACHE) + "|" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_CACHE),
                "DRP:" + status.getInt(TXLiveConstants.NET_STATUS_AUDIO_DROP) + "|" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_DROP),
                "VRA:" + status.getInt(TXLiveConstants.NET_STATUS_VIDEO_BITRATE) + "Kbps",
                "SVR:" + status.getString(TXLiveConstants.NET_STATUS_SERVER_IP),
                "AUDIO:" + status.getString(TXLiveConstants.NET_STATUS_AUDIO_INFO));
        return str;
    }

    @Override
    public int onDataUpdate(boolean isFront, int cameraDataRotation, int width, int height, int formta, int textureInput, byte[] data) {
        return 0;
    }
}
