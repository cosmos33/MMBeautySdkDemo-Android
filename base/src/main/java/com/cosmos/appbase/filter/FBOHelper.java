package com.cosmos.appbase.filter;

import android.opengl.GLES20;
import android.util.Size;

import com.cosmos.beautyutils.YUV2TexureHelper;

import project.android.imageprocessing.input.NV21PreviewInput;

public class FBOHelper {
    protected int[] frameBuffer;
    protected int[] texture_out;
    protected int[] depthRenderBuffer;
    private int mWidth;
    private int mHeight;
    private YUV2TexureHelper yuv2TexureHelper;
    private int cameraDataRotation = 0;

    public FBOHelper(int width, int height) {
        mWidth = width;
        mHeight = height;

    }

    private void createFBO() {

        this.frameBuffer = new int[1];
        this.texture_out = new int[1];
        this.depthRenderBuffer = new int[1];
        if (cameraDataRotation == 90 || cameraDataRotation == 270) {
            int tmp = mWidth;
            mWidth = mHeight;
            mHeight = tmp;
        }

        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glGenRenderbuffers(1, depthRenderBuffer, 0);
        GLES20.glGenTextures(1, texture_out, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture_out[0]);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture_out[0], 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mWidth, mHeight);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRenderBuffer[0]);
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("glCheckFramebufferStatus");
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public int update(byte[] data, int orientation, Size previewSize) {

        cameraDataRotation = orientation;
        if (frameBuffer == null) {
            createFBO();
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glClearColor(1.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        yuv2TexureHelper.updateYUVBuffer(data, previewSize.getWidth(), previewSize.getHeight(), orientation);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return texture_out[0];
    }

    public void setNeedFlip(boolean isFlip, int rotateValue) {
        if (yuv2TexureHelper == null) {
            yuv2TexureHelper = new YUV2TexureHelper();
            yuv2TexureHelper.initWithGLContext();
        }
        if (isFlip) {
            yuv2TexureHelper.changeCurRotation(360 - rotateValue);
            yuv2TexureHelper.flipPosition(NV21PreviewInput.FLIP_BOTH);
        } else {
            yuv2TexureHelper.changeCurRotation(rotateValue);
            yuv2TexureHelper.flipPosition(NV21PreviewInput.FLIP_HORIZONTAL);
        }
    }
}
