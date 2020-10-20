package com.cosmos.appbase.utils

import android.content.Context
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class SaveByteToFile {
    private var file: File? = null
    private var fileOutputStream: FileOutputStream? = null
    private var i = 0
    public fun saveByte(context: Context, frameData: ByteArray) {
        if (file == null) {
            file = File(context.getFilesDir(), "bytes");
            if (!file!!.exists()) {
                try {
                    file?.createNewFile();
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
            try {
                fileOutputStream = FileOutputStream(file);
            } catch (e: FileNotFoundException) {
                e.printStackTrace();
            }
        }
        if (i <= 10) {
            try {
                fileOutputStream?.write(frameData);
            } catch (e: IOException) {
                e.printStackTrace();
            }
        }
        i++;
    }
}