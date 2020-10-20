package com.cosmos.appbase.filter

import android.opengl.GLES30
import com.cosmos.appbase.utils.LogUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RotateFilter {
    companion object {
        const val ROTATE_0 = 0 // 逆时针旋转
        const val ROTATE_90 = 1 // 逆时针旋转
        const val ROTATE_180 = 2
        const val ROTATE_270 = 3
        const val ROTATE_VERTICAL = 4 //垂直旋转
        const val ROTATE_HORIZONTAL = 5 // 水平旋转
    }

    private val fboArray = IntArray(1)
    private val textureArray = IntArray(1)
    private val vaoArray by lazy { IntArray(1) }
    private val vboArray by lazy { IntArray(1) }
    private val eboArray by lazy { IntArray(1) }

    private var lastWidth = -1
    private var lastHeight = -1
    private val program: Int by lazy {
        GLESHelper.createProgram(
            getVertexShader(),
            getFragmentShader()
        )
    }
    private var rotateType: Int = -1
    private val vertexcoord_0 by lazy {
        floatArrayOf(
            -1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f, 0.0f, 1.0f
        )
    }
    private val vertexcoord_Vertical by lazy {
        floatArrayOf(
            -1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 1.0f, 0.0f, 0.0f
        )
    }
    private val vertexcoord_Horizontal by lazy {
        floatArrayOf(
            -1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, 1.0f, 1.0f, 1.0f, 1.0f
        )
    }
    private val vertexcoord_90 by lazy {
        floatArrayOf(
            -1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 1.0f, 1.0f, 1.0f
        )
    }
    private val vertexcoord_180 by lazy {
        floatArrayOf(
            -1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            -1.0f, 1.0f, 1.0f, 1.0f, 0.0f
        )
    }
    private val eboData by lazy {
        intArrayOf(
            0, 1, 2, 0, 2, 3
        )
    }

    private fun getVertexShader(): String {
        return "attribute vec3 vertexCoord;" +
                "attribute vec2 texCoordIn;" +
                "varying vec2 texCoord;" +
                "void main(){" +
                "gl_Position = vec4(vertexCoord,1.0);" +
                "texCoord = texCoordIn;" +
                "}"

    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "varying vec2 texCoord;" +
                "uniform sampler2D texture;" +
                "void main(){" +
                "gl_FragColor = texture2D(texture,texCoord);" +
//                "gl_FragColor = vec4(texCoord.x,texCoord.y,texCoord.x,1.0f);" +
//                "gl_FragColor = vec4(0.5f,0.5f,0.5f,1.0f);" +
                "}"
    }


    constructor(rotateType: Int) {
        this.rotateType = rotateType
        config()
    }


    private fun config() {
        GLES30.glGenVertexArrays(1, vaoArray, 0)
        GLES30.glBindVertexArray(vaoArray[0])
        GLES30.glGenBuffers(1, vboArray, 0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vboArray[0])
        val vertexCoord = getVertexCoord(rotateType)
        val vertexDataSize = vertexCoord.size * 4
        val vboBuffer = ByteBuffer.allocateDirect(vertexDataSize)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(vertexCoord)
            .position(0)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            vertexDataSize,
            vboBuffer,
            GLES30.GL_STATIC_DRAW
        )
        var vertexCoordIndex = GLES30.glGetAttribLocation(program, "vertexCoord")
        GLES30.glVertexAttribPointer(
            vertexCoordIndex,
            3,
            GLES30.GL_FLOAT,
            false,
            5 * 4,
            0
        )
        GLES30.glEnableVertexAttribArray(vertexCoordIndex)
        var texCoordIndex = GLES30.glGetAttribLocation(program, "texCoordIn")
        GLES30.glVertexAttribPointer(
            texCoordIndex,
            2,
            GLES30.GL_FLOAT,
            false,
            5 * 4,
            3 * 4
        )
        GLES30.glEnableVertexAttribArray(texCoordIndex)
        GLES30.glGenBuffers(1, eboArray, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboArray[0])
        val eboSize = eboData.size * 4
        val eboData = ByteBuffer.allocateDirect(eboSize)
            .order(ByteOrder.nativeOrder())
            .asIntBuffer()
            .put(eboData)
            .position(0)
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER, eboSize, eboData, GLES30.GL_STATIC_DRAW)

        GLES30.glBindVertexArray(0)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0)
    }

    private fun getVertexCoord(rotateType: Int): FloatArray {
        return when (rotateType) {
            ROTATE_0 -> vertexcoord_0
            ROTATE_VERTICAL -> vertexcoord_Vertical
            ROTATE_HORIZONTAL -> vertexcoord_Horizontal
            ROTATE_90 -> vertexcoord_90
            ROTATE_180 -> vertexcoord_180
            else -> vertexcoord_0
        }
    }


    fun rotateTexture(texture: Int, width: Int, height: Int): Int {
        createFbo(width, height)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboArray[0])
        GLES30.glClearColor(1.0f, 0.5f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glViewport(0, 0, width, height)

        GLES30.glUseProgram(program)
        GLES30.glBindVertexArray(vaoArray[0])

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glUniform1i(GLES30.glGetUniformLocation(program, "texture"), 0)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES30.GL_UNSIGNED_INT, 0)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glBindVertexArray(0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        return textureArray[0]
    }

    private fun createFbo(width: Int, height: Int) {
        if (fboArray[0] != 0 && lastWidth == width && height == lastHeight) {
            return
        }
        GLES30.glDeleteTextures(1, textureArray, 0)
        GLES30.glDeleteFramebuffers(1, fboArray, 0)
        lastHeight = height
        lastWidth = width
        GLES30.glPixelStorei(GLES30.GL_UNPACK_ALIGNMENT, 1)
        GLES30.glGenFramebuffers(1, fboArray, 0)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboArray[0])
        GLES30.glGenTextures(1, textureArray, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureArray[0])
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA,
            width,
            height,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            null
        )
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            textureArray[0],
            0
        )
        if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            LogUtil.v("RotateFilter", "RotateFilter fbo create failed!!!!!!")
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }

    fun destory() {
        GLES30.glDeleteVertexArrays(vaoArray.size, vaoArray, 0)
        GLES30.glDeleteTextures(textureArray.size, textureArray, 0)
        GLES30.glDeleteFramebuffers(fboArray.size, fboArray, 0)
        GLES30.glDeleteBuffers(vboArray.size, vboArray, 0)
        GLES30.glDeleteBuffers(eboArray.size, eboArray, 0)
        GLES30.glDeleteProgram(program)
    }

    fun getFbo(): Int {
        return fboArray[0]
    }

}