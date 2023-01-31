package com.appinfoplugin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    @SerialName("PackageName") val packageName: String,
    @SerialName("VersionName") val versionName: String,
    @SerialName("VersionCode") val versionCode: Int,
    @SerialName("Environment") val environment: String,
) : java.io.Serializable