package com.cosmos.appbase.filter;

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.Build;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;


public class FaceInfoCreatorPBOFilter extends FaceInfoCreatorFilter {
    final static String TAG = "FaceInfoCreatorPBOFilter";
    private IntBuffer mPboIds;
    private int mPboSize;
    private int mPboIndex;
    private int mPboNewIndex;
    private boolean mInitRecord;
    public ByteBuffer byteBuffer;


    public FaceInfoCreatorPBOFilter(int width, int height) {
        super(width, height);
    }

    private ByteBuffer readPixels() {
        long startRead = System.currentTimeMillis();
        ByteBuffer byteBuffer = readPixelByPBO(0, 0, getWidth(), getHeight());
        if (byteBuffer == null) {
            return null;
        }
        long afterRead = System.currentTimeMillis();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        mReadPixelsCostTime = afterRead - startRead;


        return byteBuffer;
    }

    @Override
    public void drawFrame() {
        drawImageScale();

        byteBuffer = readPixels();

//        sendFrame(byteBuffer);
    }

    @Override
    protected void handleSizeChange() {
        super.handleSizeChange();
        if (frameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
            frameBuffer = null;
        }
        destroyPixelBuffers();
    }

    @Override
    protected void initFBO() {
        super.initFBO();
        initPixelBuffer(getWidth(), getHeight());
    }

    //初始化2个pbo，交替使用
    private void initPixelBuffer(int width, int height) {
        if (mPboIds != null) {
            return;
        }

        mPboIndex = 0;
        mPboNewIndex = 1;
        mInitRecord = true;

        mPboSize = 4 * width * height;
        mPboIds = IntBuffer.allocate(2);
        GLES30.glGenBuffers(2, mPboIds);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(0));
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES30.GL_STATIC_READ);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(1));
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, mPboSize, null, GLES30.GL_STATIC_READ);

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);
    }

    private void destroyPixelBuffers() {
        if (mPboIds != null) {
            GLES30.glDeleteBuffers(2, mPboIds);
            mPboIds = null;
        }
    }

    private ByteBuffer readPixelByPBO(int x, int y, int width, int height) {
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(mPboIndex));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            GLES30.glReadPixels(x, y, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0);
        } else {
//            ReadPixelsJni.nativeReadPixels(x, y, width, height, GLES20.GL_RGBA, GLES30.GL_UNSIGNED_BYTE);
        }

        if (mInitRecord) {//第一帧没有数据跳出
            unbindPixelBuffer();
            mInitRecord = false;
            return null;
        }

        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, mPboIds.get(mPboNewIndex));

        //glMapBufferRange会等待DMA传输完成，所以需要交替使用pbo
        ByteBuffer byteBuffer = (ByteBuffer) GLES30.glMapBufferRange(GLES30.GL_PIXEL_PACK_BUFFER, 0, mPboSize, GLES30.GL_MAP_READ_BIT);
        if (byteBuffer == null) {
            GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER);
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);
            return null;
        }
        byteBuffer.position(0);

        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER);
        unbindPixelBuffer();

        return byteBuffer;
    }

    //解绑pbo
    private void unbindPixelBuffer() {
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);

        mPboIndex = (mPboIndex + 1) % 2;
        mPboNewIndex = (mPboNewIndex + 1) % 2;
    }
}
