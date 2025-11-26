package com.trigon.launcher.vo
import android.graphics.drawable.Drawable

data class APKItem(
    val packageName: String? = null,
    val appName: String? = null,
    val iconRes: Int? = null,  // 资源ID
    val iconDrawable: Drawable? = null,  // 或使用 Drawable 对象
    val isInstalled: Boolean = false,
    val versionName: String? = null,  // 可选：版本信息
    val versionCode: Long? = null
)