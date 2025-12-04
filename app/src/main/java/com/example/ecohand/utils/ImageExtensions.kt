package com.example.ecohand.utils

import android.graphics.Bitmap
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 * Extensión para convertir ImageProxy a Bitmap
 */
fun ImageProxy.toBitmap(): Bitmap {
    val buffer: ByteBuffer = planes[0].buffer
    val pixelStride = planes[0].pixelStride
    val rowStride = planes[0].rowStride
    val rowPadding = rowStride - pixelStride * width

    return Bitmap.createBitmap(
        width + rowPadding / pixelStride,
        height,
        Bitmap.Config.ARGB_8888
    ).also { bitmap ->
        bitmap.copyPixelsFromBuffer(buffer)
    }.let { bitmap ->
        // Recortar la imagen al tamaño correcto si hay padding
        if (bitmap.width != width) {
            val croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            bitmap.recycle()
            croppedBitmap
        } else {
            bitmap
        }
    }
}
