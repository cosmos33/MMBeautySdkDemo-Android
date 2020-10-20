package com.tencent.liteav.demo.livepusher.camerapush;

import android.content.Context;

import androidx.multidex.BuildConfig;

import com.core.glcore.cv.MMCVInfo;
import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.filter.Empty2Filter;
import com.cosmos.appbase.filter.FaceInfoCreatorPBOFilter;
import com.cosmos.appbase.filter.RotateFilter;
import com.cosmos.appbase.listener.OnFilterResourcePrepareListener;
import com.cosmos.appbase.listener.OnStickerResourcePrepareListener;
import com.cosmos.appbase.utils.FilterUtils;
import com.cosmos.beauty.CosmosBeautySDK;
import com.cosmos.beauty.inter.OnAuthenticationStateListener;
import com.cosmos.beauty.inter.OnBeautyResourcePreparedListener;
import com.cosmos.beauty.model.AuthResult;
import com.cosmos.beauty.model.BeautySDKInitConfig;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beauty.module.IMMRenderModuleManager;
import com.cosmos.beauty.module.beauty.IBeautyModule;
import com.cosmos.beauty.module.beauty.SimpleBeautyType;
import com.cosmos.beauty.module.lookup.ILookupModule;
import com.cosmos.beauty.module.sticker.DetectRect;
import com.cosmos.beauty.module.sticker.IStickerModule;
import com.cosmos.beauty.module.sticker.MaskLoadCallback;
import com.mm.mmutil.toast.Toaster;
import com.momo.mcamera.mask.MaskModel;
import com.tencent.liteav.demo.livepusher.camerapush.model.CameraPushImpl;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class CameraPushTxVideoCaptureImpl extends CameraPushImpl implements TXLivePusher.VideoCustomProcessListener, IMMRenderModuleManager.CVModelStatusListener, IMMRenderModuleManager.IDetectFaceCallback, IMMRenderModuleManager.IDetectGestureCallback {
    private TXCloudVideoView mPusherView;
    private IMMRenderModuleManager renderModuleManager;
    private boolean authSuccess = false;
    private boolean filterResouceSuccess = false;
    private boolean cvModelSuccess = false;
    private boolean stickerSuccess;
    private IBeautyModule iBeautyModule;
    private ILookupModule iLookupModule;
    private IStickerModule iStickerModule;
    private RotateFilter rotateFilter;

    public CameraPushTxVideoCaptureImpl(Context context, TXCloudVideoView pusherView, int screenRotation) {
        super(context, pusherView);
    }

    @Override
    protected void showViewLog(boolean enable) {

    }

    @Override
    protected void startPreview(TXCloudVideoView mPusherView) {
        this.mPusherView = mPusherView;
        mLivePusher.getConfig().setVideoFPS(30);
        mLivePusher.getConfig().enableHighResolutionCaptureMode(false);
        mLivePusher.getConfig().setFrontCamera(true);
//        mLivePusher.getConfig().setVideoResolution(TXLiveConstants.VIDEO_RESOLUTION_TYPE_1280_720);
        initSDK();
        mLivePusher.setVideoProcessListener(this);
        mLivePusher.startCameraPreview(mPusherView);
    }

    @Override
    protected void setVisibility(int visibility) {
        if (mPusherView != null) {
            mPusherView.setVisibility(visibility);
        }
    }

    FaceInfoCreatorPBOFilter faceInfoCreatorPBOFilter;
    Empty2Filter emptyFilter = new Empty2Filter();

    @Override
    public int onTextureCustomProcess(int texture, int texWidth, int texHeight) {
        if (rotateFilter == null) {
            rotateFilter = new RotateFilter(RotateFilter.ROTATE_180);
            faceInfoCreatorPBOFilter = new FaceInfoCreatorPBOFilter(texWidth, texHeight);
            emptyFilter.setWidth(texWidth);
            emptyFilter.setHeight(texHeight);
        }
        int rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
        if (renderModuleManager != null) {
            faceInfoCreatorPBOFilter.newTextureReady(rotateTexture, emptyFilter, true);
            if (faceInfoCreatorPBOFilter.byteBuffer != null) {
                byte[] frameData = new byte[faceInfoCreatorPBOFilter.byteBuffer.remaining()];
                faceInfoCreatorPBOFilter.byteBuffer.get(frameData);
                //美颜sdk处理
                CommonDataMode dataMode = new CommonDataMode();
                dataMode.setNeedFlip(mFrontCamera);
                int beautyTexture = renderModuleManager.renderFrame(rotateTexture, new MMRenderFrameParams(
                        dataMode,
                        frameData,
                        texWidth,
                        texHeight,
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_RGBA
                ));
                return rotateFilter.rotateTexture(beautyTexture, texWidth, texHeight);
            }
        }
        return texture;
    }

    @Override
    public void onDetectFacePoints(float[] floats) {

    }

    @Override
    public void onTextureDestoryed() {
        if (rotateFilter != null) {
            rotateFilter.destory();
        }
        if (renderModuleManager != null) {
            if (iBeautyModule != null) {
                renderModuleManager.unRegisterModule(iBeautyModule);
            }
            if (iLookupModule != null) {
                renderModuleManager.unRegisterModule(iLookupModule);
            }
            if (iStickerModule != null) {
                renderModuleManager.unRegisterModule(iStickerModule);
            }
            renderModuleManager.destroyModuleChain();
            renderModuleManager.release();
        }

    }

    private void initSDK() {
        BeautySDKInitConfig beautySDKInitConfig = new BeautySDKInitConfig.Builder(TxApplication.INSTANCE.getCosmosAppId())
                .setUserVersionCode(BuildConfig.VERSION_CODE)
                .setUserVersionName(BuildConfig.VERSION_NAME)
                .build();
        CosmosBeautySDK.INSTANCE.init(TxApplication.INSTANCE.getContext(), beautySDKInitConfig, new OnAuthenticationStateListener() {
            public void onResult(AuthResult result) {
                if (!result.isSucceed()) {
                    Toaster.show("授权失败");
                } else {
                    mPusherView.post(new Runnable() {
                        @Override
                        public void run() {
                            authSuccess = true;
                            checkResouceReady();
                        }
                    });
                }
            }
        }, new OnBeautyResourcePreparedListener() {
            public void onResult(boolean isSucceed) {
                if (!isSucceed) {
                    Toaster.show("resource false");
                }
            }
        });

        FilterUtils.INSTANCE.prepareFilterResource(TxApplication.INSTANCE.getContext(), new OnFilterResourcePrepareListener() {
            public void onFilterReady() {
                filterResouceSuccess = true;
                checkResouceReady();
            }
        });
        FilterUtils.INSTANCE.prepareStikcerResource(TxApplication.INSTANCE.getContext(), new OnStickerResourcePrepareListener() {
            public void onStickerReady(String rootPath) {
                stickerSuccess = true;
                checkResouceReady();
            }
        });
        renderModuleManager = CosmosBeautySDK.INSTANCE.createRenderModuleManager();
        renderModuleManager.prepare(true, this, this, this);
    }

    private void checkResouceReady() {
        if (cvModelSuccess && filterResouceSuccess && authSuccess && stickerSuccess) {
            Toaster.show("美颜sdk资源准备就绪！！");
            mPusherView.post(new Runnable() {
                @Override
                public void run() {
                    initRender();
                }
            });
        }
    }

    private void initRender() {
        iBeautyModule = CosmosBeautySDK.INSTANCE.createBeautyModule();
        renderModuleManager.registerModule(iBeautyModule);
        iBeautyModule.setValue(SimpleBeautyType.BIG_EYE, 1.0f);
//        iBeautyModule.setValue(SimpleBeautyType.SKIN_SMOOTH, 1.0f);
//        iBeautyModule.setValue(SimpleBeautyType.SKIN_WHITENING, 1.0f);
        iBeautyModule.setValue(SimpleBeautyType.THIN_FACE, 1.0f);

        iLookupModule = CosmosBeautySDK.INSTANCE.createLoopupModule();
        renderModuleManager.registerModule(iLookupModule);
        iLookupModule.setEffect(FilterUtils.INSTANCE.getFilterHomeDir().getAbsolutePath() + "/GrayTone");
        iLookupModule.setIntensity(0.2f);

        iStickerModule = CosmosBeautySDK.INSTANCE.createStickerModule();
        renderModuleManager.registerModule(iStickerModule);
        iStickerModule.addMaskModel(
                new File(mPusherView.getContext().getFilesDir().getAbsolutePath() + "/facemasksource/", "rainbow_engine"),
                new MaskLoadCallback() {

                    @Override
                    public void onMaskLoadSuccess(MaskModel maskModel) {
                        if (maskModel == null) {
                            Toaster.show("贴纸加载失败");
                        }
                    }
                });
    }

    @Override
    public void onCvModelStatus(boolean loadCvModelSuccess) {
        if (loadCvModelSuccess) {
            cvModelSuccess = true;
            checkResouceReady();
        }
    }

    @Override
    public void onDetectHead(@Nullable MMCVInfo info) {

    }

    @Override
    public void onDetectGesture(@NotNull String type, @NotNull DetectRect detect) {

    }

    @Override
    public void onGestureMiss() {

    }
}
