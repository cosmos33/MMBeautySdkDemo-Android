package im.zego.common.widgets.log;

import android.app.Application;

import im.zego.common.application.ZegoApplication;


public class EnContext {

    private static final Application INSTANCE = (Application) ZegoApplication.zegoApplication;


    public static Application get() {
        return INSTANCE;
    }
}
