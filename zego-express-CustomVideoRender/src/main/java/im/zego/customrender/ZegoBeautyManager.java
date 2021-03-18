package im.zego.customrender;

import android.content.Context;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTextureFilter;
import com.cosmos.appbase.TransYUVTextureFilter;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.Empty2Filter;
import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter;
import com.cosmos.beautyutils.RotateFilter;
import com.mm.mmutil.app.AppContext;

import im.zego.customrender.orientation.BeautySdkOrientationSwitchListener;
import im.zego.customrender.orientation.ScreenOrientationManager;

public class ZegoBeautyManager extends BeautyManager {
    private TransOesTextureFilter transOesTextureFilter;
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;
    private BeautySdkOrientationSwitchListener orientationListener;
    private TransYUVTextureFilter transYUVTextureFilter;

    public ZegoBeautyManager(Context context, String appId) {
        super(context, appId);
        orientationListener = new BeautySdkOrientationSwitchListener();
        ScreenOrientationManager screenOrientationManager =
                ScreenOrientationManager.getInstance(AppContext.getContext());
        screenOrientationManager.setAngleChangedListener(orientationListener);
        if (!screenOrientationManager.isListening()) {
            screenOrientationManager.start();
        }
    }

    public int renderWithYUVTexture(int[] texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (transYUVTextureFilter == null) {
            transYUVTextureFilter = new TransYUVTextureFilter();
        }
        int yuvTex = transYUVTextureFilter.newTextureReady(texture, texWidth, texHeight);
        return renderWithTexture(yuvTex, texWidth, texHeight, mFrontCamera);
    }

    @Override
    public int renderWithOESTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotation) {
        if (transOesTextureFilter == null) {
            transOesTextureFilter = new TransOesTextureFilter();
        }
        int tempWidth = texWidth;
        int tempHeight = texHeight;
        return renderWithTexture(transOesTextureFilter.newTextureReady(texture, texWidth, texHeight), tempWidth, tempHeight, mFrontCamera);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (faceInfoCreatorPBOFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_VERTICAL);
                faceInfoCreatorPBOFilter = new FaceInfoCreatorPBOFilter(texWidth, texHeight);
                emptyFilter = new Empty2Filter();
                emptyFilter.setWidth(texWidth);
                emptyFilter.setHeight(texHeight);
            }
            float currentAngle = orientationListener.getCurrentAngle();
            int rotateTexture = texture;
            if (currentAngle == 0) {
                rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
            }
            faceInfoCreatorPBOFilter.newTextureReady(rotateTexture, emptyFilter, true);

            if (faceInfoCreatorPBOFilter.byteBuffer != null) {
                byte[] frameData = new byte[faceInfoCreatorPBOFilter.byteBuffer.remaining()];
                faceInfoCreatorPBOFilter.byteBuffer.get(frameData);
                //美颜sdk处理
                CommonDataMode dataMode = new CommonDataMode();
                dataMode.setNeedFlip(false);
                int beautyTexture = renderModuleManager.renderFrame(rotateTexture, new MMRenderFrameParams(
                        dataMode,
                        frameData,
                        texWidth,
                        texHeight,
                        texWidth,
                        texHeight,
                        ImageFrame.MMFormat.FMT_RGBA
                ));
                if (currentAngle != 0) {
                    return beautyTexture;
                }
                return revertRotateFilter.rotateTexture(beautyTexture, texWidth, texHeight);
            }
        }
        return texture;
    }

    public void stopOrientationCallback() {
        ScreenOrientationManager screenOrientationManager =
                ScreenOrientationManager.getInstance(AppContext.getContext());
        if (screenOrientationManager.isListening()) {
            screenOrientationManager.stop();
        }
        ScreenOrientationManager.release();
    }

    @Override
    public void textureDestoryed() {
        super.textureDestoryed();
        if (rotateFilter != null) {
            rotateFilter.destory();
            rotateFilter = null;
        }
        if (revertRotateFilter != null) {
            revertRotateFilter.destory();
            revertRotateFilter = null;
        }
        if (transYUVTextureFilter != null) {
            transYUVTextureFilter.destroy();
            transYUVTextureFilter = null;
        }

    }
}
