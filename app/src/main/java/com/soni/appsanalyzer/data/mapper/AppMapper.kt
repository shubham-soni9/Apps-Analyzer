package com.soni.appsanalyzer.data.mapper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.soni.appsanalyzer.data.local.AppEntity
import com.soni.appsanalyzer.domain.model.AppInfo
import com.soni.appsanalyzer.domain.model.AppType
import java.io.ByteArrayOutputStream

fun AppEntity.toAppInfo(context: Context): AppInfo {
    return AppInfo(
        name = name,
        packageName = packageName,
        icon = icon?.let { byteArrayToDrawable(context, it) },
        versionName = versionName,
        appType = try { AppType.valueOf(appType) } catch (e: Exception) { AppType.UNKNOWN }
    )
}

fun AppInfo.toAppEntity(): AppEntity {
    return AppEntity(
        packageName = packageName,
        name = name,
        versionName = versionName,
        appType = appType.name,
        icon = icon?.let { drawableToByteArray(it) }
    )
}

fun drawableToByteArray(drawable: Drawable): ByteArray {
    val bitmap = if (drawable is BitmapDrawable) {
        drawable.bitmap
    } else {
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun byteArrayToDrawable(context: Context, byteArray: ByteArray): Drawable {
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    return BitmapDrawable(context.resources, bitmap)
}
