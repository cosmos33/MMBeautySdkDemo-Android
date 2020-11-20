package com.cosmos.appbase;

import project.android.imageprocessing.GLRenderer;
import project.android.imageprocessing.filter.BasicFilter;

public class TransOesTexture extends BasicFilter {
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
}
