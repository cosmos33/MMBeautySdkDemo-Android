package com.cosmos.appbase;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import project.android.imageprocessing.GLRenderer;
import project.android.imageprocessing.filter.BasicFilter;

public class TransOesTextureFilter extends BasicFilter {
    @Override
    protected String getFragmentShader() {
        return "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "varying vec2 " + GLRenderer.VARYING_TEXCOORD + ";\n" +
                "uniform samplerExternalOES " + GLRenderer.UNIFORM_TEXTURE0 + ";\n" +
                "void main() {\n" +
                "    gl_FragColor = texture2D(" + GLRenderer.UNIFORM_TEXTURE0 + ", " + GLRenderer.VARYING_TEXCOORD + ");\n" +
                "}\n";
    }

    public int newTextureReady(int texture, int width, int height) {
        markAsDirty();
        texture_in = texture;
        setWidth(width);
        setHeight(height);
        onDrawFrame();
        return getTextOutID();
    }

    @Override
    protected void passShaderValues() {
        renderVertices.position(0);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, renderVertices);
        GLES20.glEnableVertexAttribArray(positionHandle);
        textureVertices[curRotation].position(0);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureVertices[curRotation]);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture_in);
        GLES20.glUniform1i(textureHandle, 0);
    }
}
