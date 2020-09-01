package com.cosmos.beautydemo.filter;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class YUV2TexureHelper {
    public static final int FLIP_VERTICAL = 0;
    public static final int FLIP_HORIZONTAL = 1;
    public static final int FLIP_BOTH = 2;
    public static final int FLIP_NONE = 3;
    public static final String ATTRIBUTE_POSITION = "position";
    public static final String ATTRIBUTE_TEXCOORD = "inputTextureCoordinate";
    public static final String VARYING_TEXCOORD = "textureCoordinate";
    protected static final String UNIFORM_TEXTUREBASE = "inputImageTexture";
    public static final String UNIFORM_TEXTURE0 = UNIFORM_TEXTUREBASE + 0;

    private static final String UNIFORM_CAM_MATRIX = "u_Matrix";
    private static final String vertexShaderCode =
            "attribute vec4 position;"
                    + "attribute mediump vec4 inputTextureCoordinate;"
                    + "varying mediump vec2 coordinate;"
                    + ""
                    + "void main()"
                    + "{"
                    + "    gl_Position = position;"
                    + "    coordinate = inputTextureCoordinate.xy;"
                    + "}";
    /**
     * The fragment shader code used by OpenGL ES 2.
     */

    private static final String fragmentShaderCode =
            "precision mediump float;"
                    + "uniform sampler2D SamplerY;"
                    + "uniform sampler2D SamplerUV;"
                    + "varying mediump vec2 coordinate;"
                    + ""
                    + "void main()"
                    + "{" +
                    "   float r, g, b, y, u, v;\n" +
                    //We had put the Y values of each pixel to the R,G,B components by
                    //GL_LUMINANCE, that's why we're pulling it from the R component,
                    //we could also use G or B
                    "   y = texture2D(SamplerY, coordinate).r;\n" +
                    //We had put the U and V values of each pixel to the A and R,G,B
                    //components of the texture respectively using GL_LUMINANCE_ALPHA.
                    //Since U,V bytes are interspread in the texture, this is probably
                    //the fastest way to use them in the shader
                    "   u = texture2D(SamplerUV, coordinate).a - 0.5;\n" +
                    "   v = texture2D(SamplerUV, coordinate).r - 0.5;\n" +
                    "   r = y + 1.13983*v;\n" +
                    "   g = y - 0.39465*u - 0.58060*v;\n" +
                    "   b = y + 2.03211*u;\n" +
                    "   gl_FragColor = vec4(r, g, b, 1.0);\n" +
                    "}";
    public ByteBuffer mYByteBuffer = null;
    public ByteBuffer mUVByteBufer = null;
    private int[] textures; // y, u a
    protected int programHandle;
    protected int textureHandle;
    //    protected int positionHandle;
//    protected int texCoordHandle;
    private int vertexShaderHandle;
    private int fragmentShaderHandle;
    public FloatBuffer[] textureVertices;
    protected FloatBuffer renderVertices;

    public void initWithGLContext() {
        init();
        initWithGLContextInner();
        textures = new int[2];
        GLES20.glGenTextures(2, textures, 0);
        for (int i = 0; i < 2; i++) {
            int texture = textures[i];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, GLES20.GL_TRUE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        GLES20.glUseProgram(programHandle);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programHandle, "SamplerY"), 0);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programHandle, "SamplerUV"), 1);
    }

    private void init() {
        setRenderVertices(new float[]{
                -1f, -1f,
                1f, -1f,
                -1f, 1f,
                1f, 1f
        });
        textureVertices = new FloatBuffer[4];
        float[] texData0 = new float[]{
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
        };

        textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[0].put(texData0).position(0);

        float[] texData1 = new float[]{
                0.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                1.0f, 0.0f,
        };
        textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[1].put(texData1).position(0);

        float[] texData2 = new float[]{
                1.0f, 1.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f,
        };
        textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[2].put(texData2).position(0);

        float[] texData3 = new float[]{
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                0.0f, 1.0f,
        };
        textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[3].put(texData3).position(0);
    }

    public void rotateClockwise90Degrees(int numOfTimes) {
        curRotation += numOfTimes;
        curRotation = curRotation % 4;

    }

    public void changeCurRotation(int rotation) {
        curRotation = 0;
        rotateClockwise90Degrees(rotation / 90);
    }


    public void flipPosition(int flipDirection) {
        if (flipDirection == FLIP_NONE) {
            setRenderVertices(new float[]{
                    -1f, -1f,
                    1f, -1f,
                    -1f, 1f,
                    1f, 1f
            });
            textureVertices = new FloatBuffer[4];
            float[] texData0 = new float[]{
                    0.0f, 0.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
            };

            textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[0].put(texData0).position(0);

            float[] texData1 = new float[]{
                    0.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,
            };
            textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[1].put(texData1).position(0);

            float[] texData2 = new float[]{
                    1.0f, 1.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 0.0f,
            };
            textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[2].put(texData2).position(0);

            float[] texData3 = new float[]{
                    1.0f, 0.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    0.0f, 1.0f,
            };
            textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            textureVertices[3].put(texData3).position(0);
            return;
        }
        float minX = 0f;
        float maxX = 1f;
        float minY = 0f;
        float maxY = 1f;
        switch (flipDirection) {
            case FLIP_VERTICAL:
                minX = 1f;
                maxX = 0f;
                break;
            case FLIP_HORIZONTAL:
                minY = 1f;
                maxY = 0f;
                break;
            case FLIP_BOTH:
                minX = 1f;
                maxX = 0f;
                minY = 1f;
                maxY = 0f;
                break;
        }

        float[] texData0 = new float[]{
                minX, minY,
                maxX, minY,
                minX, maxY,
                maxX, maxY,
        };
        textureVertices[0] = ByteBuffer.allocateDirect(texData0.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[0].put(texData0).position(0);

        float[] texData1 = new float[]{
                minY, maxX,
                minY, minX,
                maxY, maxX,
                maxY, minX,
        };
        textureVertices[1] = ByteBuffer.allocateDirect(texData1.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[1].put(texData1).position(0);

        float[] texData2 = new float[]{
                maxX, maxY,
                minX, maxY,
                maxX, minY,
                minX, minY,
        };
        textureVertices[2] = ByteBuffer.allocateDirect(texData2.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[2].put(texData2).position(0);

        float[] texData3 = new float[]{
                maxY, minX,
                maxY, maxX,
                minY, minX,
                minY, maxX,
        };
        textureVertices[3] = ByteBuffer.allocateDirect(texData3.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureVertices[3].put(texData3).position(0);

    }


    private void initWithGLContextInner() {
        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();
        vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        String errorInfo = "none";
        if (vertexShaderHandle != 0) {
            GLES20.glShaderSource(vertexShaderHandle, vertexShader);
            GLES20.glCompileShader(vertexShaderHandle);
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                errorInfo = GLES20.glGetShaderInfoLog(vertexShaderHandle);
                GLES20.glDeleteShader(vertexShaderHandle);
                vertexShaderHandle = 0;
            }
        }

        if (vertexShaderHandle == 0) {
            throw new RuntimeException(this + ": Could not create vertex shader. Reason: " + errorInfo);
        }

        fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (fragmentShaderHandle != 0) {
            GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);
            GLES20.glCompileShader(fragmentShaderHandle);
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            if (compileStatus[0] == 0) {
                errorInfo = GLES20.glGetShaderInfoLog(fragmentShaderHandle);
                GLES20.glDeleteShader(fragmentShaderHandle);
                fragmentShaderHandle = 0;
            }
        }
        if (fragmentShaderHandle == 0) {
            throw new RuntimeException(this + ": Could not create fragment shader. Reason: " + errorInfo);
        }

        programHandle = GLES20.glCreateProgram();
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            bindShaderAttributes();

            GLES20.glLinkProgram(programHandle);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }
        if (programHandle == 0) {
            throw new RuntimeException("Could not create program.");
        }

        initShaderHandles();
    }

    private String getFragmentShader() {
        return fragmentShaderCode;
    }

    private String getVertexShader() {
        return vertexShaderCode;
    }

    private void bindShaderAttributes() {
        GLES20.glBindAttribLocation(programHandle, 0, ATTRIBUTE_POSITION);
        GLES20.glBindAttribLocation(programHandle, 1, ATTRIBUTE_TEXCOORD);
    }

    private void initShaderHandles() {
        textureHandle = GLES20.glGetUniformLocation(programHandle, UNIFORM_TEXTURE0);
//        positionHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_POSITION);
//        texCoordHandle = GLES20.glGetAttribLocation(programHandle, ATTRIBUTE_TEXCOORD);
    }

    public void updateYUVBuffer(byte[] data, int width, int height, int orientation) {
        if (data == null) {
            return;
        }

        int planerSize = width * height;
        try {
            if (this.mYByteBuffer == null || this.mYByteBuffer.capacity() != planerSize) {
                this.mYByteBuffer = ByteBuffer.allocateDirect(planerSize);
            }
            if (this.mUVByteBufer == null || this.mUVByteBufer.capacity() != planerSize / 2) {
                this.mUVByteBufer = ByteBuffer.allocateDirect(planerSize / 2);
            }

            this.mYByteBuffer.clear();
            this.mUVByteBufer.clear();
            this.mYByteBuffer.position(0);
            this.mUVByteBufer.position(0);
            this.mYByteBuffer.put(data, 0, planerSize);
            this.mUVByteBufer.put(data, planerSize, planerSize / 2);

            this.mYByteBuffer.position(0);
            this.mUVByteBufer.position(0);
        } catch (Exception e) {
            return;
        }
        passShaderValues(width, height, orientation);
    }

    private void setRenderVertices(float[] vertices) {
        renderVertices = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        renderVertices.put(vertices).position(0);
    }

    int curRotation = 0;
    private void passShaderValues(int width, int height, int orientation) {
//
//        if (orientation == 270) {
//            curRotation = 1;
//        } else if (orientation == 90) {
//            curRotation = 3;
//        }

        changeCurRotation(orientation);
        renderVertices.position(0);
        //caculate uv width height just relate to input width  height  not output width height;

        GLES20.glUseProgram(programHandle);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programHandle, "SamplerY"), 0);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(programHandle, "SamplerUV"), 1);
        //        GLES20.glEnableVertexAttribArray(programHandle);

        GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 0, renderVertices);
        GLES20.glEnableVertexAttribArray(0);
        textureVertices[curRotation].position(0);
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, 0, textureVertices[curRotation]);
        GLES20.glEnableVertexAttribArray(1);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE, width, height, 0, GL10.GL_LUMINANCE, GL10.GL_UNSIGNED_BYTE, mYByteBuffer);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[1]);
        GLES20.glTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_LUMINANCE_ALPHA, width / 2, height / 2, 0, GL10.GL_LUMINANCE_ALPHA, GL10.GL_UNSIGNED_BYTE, mUVByteBufer);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        disableDrawArray();
    }

    protected void disableDrawArray() {
        GLES20.glDisableVertexAttribArray(0);
        GLES20.glDisableVertexAttribArray(1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }
}
