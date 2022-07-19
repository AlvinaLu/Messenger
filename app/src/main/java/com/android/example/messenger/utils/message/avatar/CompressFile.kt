package com.android.example.messenger.utils.message.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

class CompressFile {


    fun compressImage(filePath: String, targetMb: Double = 1.0) {

        var image: Bitmap = BitmapFactory.decodeFile(filePath)
        var exif = ExifInterface(filePath)
        var exifOrientation: Int = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )
        val exitDegree: Int = exifOrientationToDegrees(exifOrientation)
        image = rotateImage(image, exitDegree.toFloat())

        try {
            val fileSizeInMB = getFileSizeInMB(filePath)

            var quality = 100
            if (fileSizeInMB > targetMb) {
                quality = ((targetMb / fileSizeInMB) * 100).toInt()
            }

            val fileOutputStream = FileOutputStream(filePath)
            image.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
            fileOutputStream.close()



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileSizeInMB(filePath: String): Double {
        val file = File(filePath)
        val length = file.length()

        val fileSizeInKB = (length / 1024).toString().toDouble()
        return (fileSizeInKB / 1024).toString().toDouble()
    }

    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
        return when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                90
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                180
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                270
            }
            else -> 0
        }
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }
}