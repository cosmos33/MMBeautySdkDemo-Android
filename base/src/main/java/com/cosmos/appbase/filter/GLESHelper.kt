package com.cosmos.appbase.filter

import android.opengl.GLES20
import android.opengl.GLES30
import android.text.TextUtils
import com.cosmos.appbase.utils.LogUtil

object GLESHelper {
    private const val TAG = "GLESHelper"

    fun createProgram(vertexSource: String?, fragmentSource: String?): Int {
        if (TextUtils.isEmpty(vertexSource) || TextUtils.isEmpty(fragmentSource)) {
            LogUtil.v(TAG, "vertex/fragment shader source is empty")
            return -1
        }
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, vertexSource)
        GLES20.glCompileShader(vertexShader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            LogUtil.v(TAG, "vertex complile error:${GLES20.glGetShaderInfoLog(vertexShader)}")
            GLES20.glDeleteShader(vertexShader)
            return -1
        }
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, fragmentSource)
        GLES20.glCompileShader(fragmentShader)
        GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            LogUtil.v(TAG, "fragment complie error:${GLES20.glGetShaderInfoLog(fragmentShader)}")
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
            return -1
        }
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            LogUtil.v(TAG, "program link error:${GLES20.glGetProgramInfoLog(program)}")
            GLES20.glDeleteShader(vertexShader)
            GLES20.glDeleteShader(fragmentShader)
            GLES20.glDeleteProgram(program)
            return -1
        }
        GLES20.glDeleteShader(vertexShader)
        GLES20.glDeleteShader(fragmentShader)
        return program
    }

    fun checkError() {
        val error = GLES20.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            when (error) {
                GLES20.GL_INVALID_ENUM -> LogUtil.v(TAG, "checkError:${error} GL_INVALID_ENUM")
                GLES20.GL_INVALID_VALUE -> LogUtil.v(TAG, "checkError:${error} GL_INVALID_VALUE")
                GLES20.GL_INVALID_OPERATION -> LogUtil.v(
                    TAG,
                    "checkError:${error} GL_INVALID_OPERATION"
                )
                GLES20.GL_OUT_OF_MEMORY -> LogUtil.v(TAG, "checkError:${error} GL_OUT_OF_MEMORY")
                else -> LogUtil.v(TAG, "checkError:${error} :fs")
            }

        }
    }
}