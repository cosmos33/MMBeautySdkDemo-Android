package com.qiniu.pili.droid.streaming.demo.gles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import com.qiniu.pili.droid.streaming.demo.R;
import com.qiniu.pili.droid.streaming.demo.filter.CameraFilterBeauty;
import com.qiniu.pili.droid.streaming.demo.filter.NormalFilter;
import com.qiniu.pili.droid.streaming.demo.utils.Config;

import java.nio.ByteBuffer;

public class FBO {
    private static final String TAG = "FBO";
    private boolean mEnable = Config.FILTER_ENABLED;

    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private static int[] mCurveArrays = new int[]{
            R.raw.cross_1, R.raw.cross_2, R.raw.cross_3, R.raw.cross_4, R.raw.cross_5,
            R.raw.cross_6, R.raw.cross_7, R.raw.cross_8, R.raw.cross_9, R.raw.cross_10,
            R.raw.cross_11,
    };
    private int mCurveIndex = 10;

    // Used for off-screen rendering.
    private int mOffscreenTexture;
    private int mFramebuffer;
    private FullFrameRect mFullScreen;
    private FullFrameRect mNormal;

    public void updateSurfaceSize(int width, int height) {
        if (!mEnable) {
            return;
        }

        mSurfaceWidth = width;
        mSurfaceHeight = height;
    }

    public void initialize(Context context) {
        if (!mEnable) {
            return;
        }

        if (mFullScreen != null) {
            mFullScreen.release(false);
        }
        if (mNormal != null) {
            mNormal.release(false);
        }

        /**
         * Create a new full frame renderer with beauty filter.
         * There are two another filter, you can have a try.
         */
//        mFullScreen = new FullFrameRect(new CameraFilterToneCurve(context,
//                context.getResources().openRawResource(mCurveArrays[mCurveIndex])));
//        mFullScreen = new FullFrameRect(new CameraFilterMosaic(context));
        mFullScreen = new FullFrameRect(new CameraFilterBeauty(context));
        mNormal = new FullFrameRect(new NormalFilter(context));

        mOffscreenTexture = 0;
    }

    public void release() {
        if (!mEnable) {
            return;
        }
        mFullScreen.release(true);
        mNormal.release(true);
    }

    /**
     * Prepares the off-screen framebuffer.
     */
    private void prepareFramebuffer(int width, int height) {
        GlUtil.checkGlError("start");
        int[] values = new int[1];

        // Create a texture object and bind it.  This will be the color buffer.
        GLES20.glGenTextures(1, values, 0);
        GlUtil.checkGlError("glGenTextures");
        mOffscreenTexture = values[0];   // expected > 0
        Log.i(TAG, "prepareFramebuffer mOffscreenTexture:" + mOffscreenTexture);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOffscreenTexture);
        GlUtil.checkGlError("glBindTexture");

        // Create texture storage.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GlUtil.checkGlError("glTexParameter");

        // Create framebuffer object and bind it.
        GLES20.glGenFramebuffers(1, values, 0);
        GlUtil.checkGlError("glGenFramebuffers");
        mFramebuffer = values[0];    // expected > 0

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        GlUtil.checkGlError("glBindFramebuffer " + mFramebuffer);

        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mOffscreenTexture, 0);

        // See if GLES is happy with all this.
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete, status=" + status);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        // Switch back to the default framebuffer.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GlUtil.checkGlError("glBindFramebuffer");
    }

    private ByteBuffer byteBuffer;
    private int lastTexWidth = -1;
    private int lastTexHeight = -1;

    /**
     * 说明，方法内部使用了多次旋转渲染操作，主要原因为传入的texId的 oes texture， 且数据内容具体270度的旋转处理
     * 在进行美颜处理时，需要优先将图像转正，故需要先旋转270度，将图像转正，然后进行美颜处理
     * 在接入时，需要按接入情况，具体确定是否需要旋转及旋转角度
     * 外部在使用时，需要将oes texture对应的matrix重置为单位矩阵
     *
     * @param texId
     * @param texWidth
     * @param texHeight
     * @param onDrawFrameListener
     * @return
     */
    public int drawFrame(int texId, int texWidth, int texHeight, OnDrawFrameListener onDrawFrameListener) {

        if (!mEnable) {
            return texId;
        }
        //camera回调输出图像会有90的旋转
        int renderWidth = texHeight;
        int renderHeight = texWidth;

        GLES20.glViewport(0, 0, renderWidth, renderHeight);
        if (mOffscreenTexture == 0) {
            prepareFramebuffer(renderWidth, renderHeight);
        }
        //第一步，将图像转正
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
        mNormal.getFilter().setTextureSize(renderWidth, renderHeight);
        mNormal.drawFrame(texId, 270);

        int resultTextureId = mOffscreenTexture;

        //使用美颜SDK时进行如下操作
        boolean needDataBuffer = false;
        if (onDrawFrameListener != null) {
            //美颜处理
            resultTextureId = onDrawFrameListener.onDrawFrame(mOffscreenTexture, texWidth, texHeight, renderWidth, renderHeight);
            //转回输入时状态
            if (needDataBuffer) {
                //申请buffer
                if (lastTexWidth != renderWidth || lastTexHeight != renderHeight) {
                    byteBuffer = ByteBuffer.allocate(renderWidth * renderHeight * 4);
                    lastTexWidth = renderWidth;
                    lastTexHeight = renderHeight;
                }
                byteBuffer.position(0);

                //确定渲染设备
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebuffer);
                GLES20.glViewport(0, 0, renderWidth, renderHeight);
                mNormal.getFilter().setTextureSize(renderWidth, renderHeight);
                if (resultTextureId == mOffscreenTexture) {
                    //鉴权失败，直接从buffer中读取数据
                    GLES30.glReadPixels(0, 0, renderWidth, renderHeight, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
                } else {
                    //美颜处理成功，旋转至输入时状态
                    //将美颜后的数据，渲染至mOffscreenTexture中，以获取buffer
                    mNormal.drawFrame(resultTextureId, 0);
                    GLES30.glReadPixels(0, 0, renderWidth, renderHeight, GLES30.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
                }
                resultTextureId = mOffscreenTexture;
            }
        }
        //重置当前渲染环境状态
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        return resultTextureId;
    }

    public interface OnDrawFrameListener {
        int onDrawFrame(int texId, int dataWidth, int dataHeight, int textureWidth, int textureHeight);
    }
}
