package com.cosmos.appbase.filter;

import android.graphics.Rect;
import android.opengl.GLES20;

import com.core.glcore.cv.FaceDetectInterface;
import com.core.glcore.cv.MMCVInfo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import project.android.imageprocessing.GLRenderer;
import project.android.imageprocessing.filter.BasicFilter;

public abstract class FaceInfoCreatorFilter extends BasicFilter implements FaceDetectInterface {
    final static String TAG = "FaceInfoCreatorFilter";
    protected int[] frameBuffer;
    protected int[] texture_out;
    protected int[] depthRenderBuffer;


    protected int mFaceImageObjectWidth = 150;
    protected int mFaceImageObjectHeight = 200;
    protected long mLastTimestamp = 0;
    protected MMCVInfo mMmcvInfo = null;
    protected Rect mFaceRect = null;
    protected float mWidthScale = 1.0f;
    protected float mHeightScale = 1.0f;
    protected int mReadX = 0;
    protected int mReadY = 0;
    protected long mReadPixelsCostTime = 0;
    //bitmap生成后的监听接口

    public FaceInfoCreatorFilter(int width, int height) {
        mFaceImageObjectWidth = width;
        mFaceImageObjectHeight = height;

        textureVertices = new FloatBuffer[4];

        float[] texData0 = new float[]{
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f,
        };
        textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[0].put(texData0).position(0);

        float[] texData1 = new float[]{
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                0.0f, 0.0f,
        };
        textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[1].put(texData1).position(0);

        float[] texData2 = new float[]{
                1.0f, 0.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
        };
        textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[2].put(texData2).position(0);

        float[] texData3 = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
        };
        textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[3].put(texData3).position(0);
    }

    /* (non-Javadoc)
     * @see project.android.imageprocessing.GLRenderer#destroy()
     */
    @Override
    public void destroy() {
        super.destroy();
        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
            frameBuffer = null;
        }
        if (texture_out != null) {
            GLES20.glDeleteTextures(1, texture_out, 0);
            texture_out = null;
        }
        if (depthRenderBuffer != null) {
            GLES20.glDeleteRenderbuffers(1, depthRenderBuffer, 0);
            depthRenderBuffer = null;
        }
    }

    @Override
    public void setMMCVInfo(MMCVInfo mmcvInfo) {
        mMmcvInfo = mmcvInfo;
        if (mmcvInfo != null) {
            float[][] rect = mmcvInfo.getFaceRects();
            if (rect != null && rect.length > 0 && rect[0] != null) {
                mFaceRect = new Rect((int) rect[0][0], (int) rect[0][1], (int) rect[0][2], (int) rect[0][3]);
            }
        }
    }

    protected boolean drawImageScale() {
        if (frameBuffer == null) {
            if (getWidth() != 0 && getHeight() != 0) {
                initFBO();
            } else {
                return false;
            }
        }
/*
        //未检测到人脸等异常情况
        if (mFaceRect == null) {
            return false;
        }
*/


        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);

       /* mWidthScale = (float) mFaceImageObjectWidth / mFaceRect.width();
        mHeightScale = (float) mFaceImageObjectHeight / mFaceRect.height();
        mReadX = (int) (mFaceRect.left * mWidthScale);
        mReadY = (int) (mFaceRect.top * mHeightScale);
        int viewWidth = (int) (getWidth() * mWidthScale);
        int viewHeight = (int) (getHeight() * mHeightScale);
*/
        drawFrameByScale(getWidth(), getHeight());

        return true;
    }


    protected void drawFrameByScale(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(getBackgroundRed(), getBackgroundGreen(), getBackgroundBlue(), getBackgroundAlpha());
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(programHandle);
        passShaderValues();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        disableDrawArray();
    }

    @Override
    protected void handleSizeChange() {
        initFBO();
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
    }

    @Override
    protected void initFBO() {
        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
            frameBuffer = null;
        }
        if (texture_out != null) {
            GLES20.glDeleteTextures(1, texture_out, 0);
            texture_out = null;
        }
        if (depthRenderBuffer != null) {
            GLES20.glDeleteRenderbuffers(1, depthRenderBuffer, 0);
            depthRenderBuffer = null;
        }
        frameBuffer = new int[1];
        texture_out = new int[1];
        depthRenderBuffer = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glGenRenderbuffers(1, depthRenderBuffer, 0);
        GLES20.glGenTextures(1, texture_out, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_out[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, getWidth(), getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture_out[0], 0);

        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, getWidth(), getHeight());
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);

        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException(this + ": Failed to set up render buffer with status " + status + " and error " + GLES20.glGetError());
        }
    }

    @Override
    protected String getFragmentShader() {
        return
                "precision mediump float;\n"
                        + "uniform sampler2D " + GLRenderer.UNIFORM_TEXTURE0 + ";\n"
                        + "varying vec2 " + GLRenderer.VARYING_TEXCOORD + ";\n"

                        + "void main(){\n"
                        + "   vec4 originalColor = texture2D(" + GLRenderer.UNIFORM_TEXTURE0 + "," + GLRenderer.VARYING_TEXCOORD + ");\n"
                        + "   gl_FragColor = vec4(originalColor.b, originalColor.g, originalColor.r, originalColor.a);\n"
                        + "}\n";
    }

    @Override
    protected String getVertexShader() {
        return
                "attribute vec4 " + GLRenderer.ATTRIBUTE_POSITION + ";\n"
                        + "attribute vec2 " + GLRenderer.ATTRIBUTE_TEXCOORD + ";\n"
                        + "varying vec2 " + GLRenderer.VARYING_TEXCOORD + ";\n"

                        + "void main() {\n"
                        + "  " + GLRenderer.VARYING_TEXCOORD + " = " + GLRenderer.ATTRIBUTE_TEXCOORD + ";\n"
                        + "   gl_Position = " + GLRenderer.ATTRIBUTE_POSITION + ";\n"
                        + "}\n";
    }

}
