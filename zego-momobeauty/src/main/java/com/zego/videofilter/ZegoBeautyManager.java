package com.zego.videofilter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.core.glcore.util.ImageFrame;
import com.cosmos.appbase.BeautyManager;
import com.cosmos.appbase.TransOesTexture;
import com.cosmos.beauty.model.MMRenderFrameParams;
import com.cosmos.beauty.model.datamode.CommonDataMode;
import com.cosmos.beautyutils.Empty2Filter;
import com.cosmos.beautyutils.FaceInfoCreatorPBOFilter;
import com.cosmos.beautyutils.RotateFilter;

public class ZegoBeautyManager extends BeautyManager {
    private TransOesTexture transOesTexture;
    private RotateFilter rotateFilter;
    private RotateFilter revertRotateFilter;

    public ZegoBeautyManager(Context context, String appId) {
        super(context, appId);
    }

    @Override
    public int renderWithOESTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera, int cameraRotation) {
        if (transOesTexture == null) {
            transOesTexture = new TransOesTexture();
        }
        int tempWidth = texWidth;
        int tempHeight = texHeight;
        if (cameraRotation == 90 || cameraRotation == 270) {
            tempHeight = texWidth;
            tempWidth = texHeight;
        }
        return renderWithTexture(transOesTexture.newTextureReady(texture, texWidth, texHeight), tempWidth, tempHeight, mFrontCamera);
    }

    @Override
    public int renderWithTexture(int texture, int texWidth, int texHeight, boolean mFrontCamera) {
        if (resourceReady) {
            if (faceInfoCreatorPBOFilter == null) {
                rotateFilter = new RotateFilter(RotateFilter.ROTATE_90);
                revertRotateFilter = new RotateFilter(RotateFilter.ROTATE_270);
                faceInfoCreatorPBOFilter = new FaceInfoCreatorPBOFilter(texWidth, texHeight);
                emptyFilter = new Empty2Filter();
                emptyFilter.setWidth(texWidth);
                emptyFilter.setHeight(texHeight);
            }
            int rotateTexture = rotateFilter.rotateTexture(texture, texWidth, texHeight);
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
                return revertRotateFilter.rotateTexture(beautyTexture, texWidth, texHeight);
            }
        }
        return texture;
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

    }
}
