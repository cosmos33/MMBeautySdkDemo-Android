package com.cosmos.appbase;

import android.opengl.GLES20;

import project.android.imageprocessing.filter.BasicFilter;

public class TransYUVTextureFilter extends BasicFilter {
    private int[] yuvTexture;
    private int yTexLocation;
    private int uTexLocation;
    private int vTexLocation;

    @Override
    protected String getFragmentShader() {
        return "precision mediump float;"
                + "uniform sampler2D SamplerY;"
                + "uniform sampler2D SamplerU;"
                + "uniform sampler2D SamplerV;"
                + "varying mediump vec2 textureCoordinate;"
                + "void main(){" +
                "vec3 rgb;" +
                "vec3 yuv;" +
                "yuv.r = texture2D(SamplerY, textureCoordinate).r - (16.0/255.0);\n" +
                "yuv.g = texture2D(SamplerU, textureCoordinate).r - 0.5;\n" +
                "yuv.b = texture2D(SamplerV, textureCoordinate).r - 0.5;\n" +
                " mat3 colorConvertion = mat3(1.164, 1.164, 1.164,\n" +
                "                             0.0, -0.392, 2.017,\n" +
                "                             1.596, -0.813, 0.0);\n" +
                " rgb = colorConvertion * yuv;" +
                "   gl_FragColor = vec4(rgb, 1.0);\n" +
                "}";
    }

    public int newTextureReady(int[] texture, int width, int height) {
        markAsDirty();
        yuvTexture = texture;
        setWidth(width);
        setHeight(height);
        onDrawFrame();
        return getTextOutID();
    }

    @Override
    protected void initShaderHandles() {
        super.initShaderHandles();
        yTexLocation = GLES20.glGetUniformLocation(this.programHandle, "SamplerY");
        uTexLocation = GLES20.glGetUniformLocation(this.programHandle, "SamplerU");
        vTexLocation = GLES20.glGetUniformLocation(this.programHandle, "SamplerV");
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTexture[0]);
        GLES20.glUniform1i(yTexLocation, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTexture[1]);
        GLES20.glUniform1i(uTexLocation, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTexture[2]);
        GLES20.glUniform1i(vTexLocation, 2);
    }
}
