package com.appinfoplugin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
	@SerialName("PackageName") val packageName: String,
	@SerialName("VersionKey") val versionKey: String,
	@SerialName("VersionName") val versionName: String,
	@SerialName("VersionCode") val versionCode: Int,
	@SerialName("Flavors") val flavors: List<FlavorInfo>,
) : java.io.Serializable

@Serializable
data class FlavorInfo(
	@SerialName("FlavorId") val flavorId: String,
	@SerialName("FlavorName") val flavorName: String,
	@SerialName("DimensionId") val dimensionId: String,
	@SerialName("DimensionName") val dimensionName: String,
) : java.io.Serializable