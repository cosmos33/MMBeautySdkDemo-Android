package com.cosmos.appbase.gl

import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.nio.IntBuffer
import javax.microedition.khronos.opengles.GL10

object GLUtils {
    fun generateTexure(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            texture[0]
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE
        )
        return texture[0]
    }

    fun snapPicture(degress: Int, width: Int, height: Int): Bitmap? {
        return if (height > 0 && width > 0) {
            val Imc2Buf = IntBuffer.allocate(width * height)
            GLES20.glReadPixels(
                0,
                0,
                width,
                height,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                Imc2Buf
            )
            var curBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            curBmp.copyPixelsFromBuffer(Imc2Buf)
            val matrix = Matrix()
            matrix.postScale(1f, -1f) //镜像垂直翻转
            matrix.postRotate(degress.toFloat())
            curBmp = Bitmap.createBitmap(curBmp, 0, 0, width, height, matrix, true)
            curBmp
        } else {
            null
        }
    }
}