package com.mhxy.app.data.remote

import com.google.gson.annotations.SerializedName

// ============================================================
//                      API 数据模型
//  统一响应格式 + 请求/响应 DTO
// ============================================================

// ==================== 统一响应 ====================

data class ApiResponse<T>(
    val status: String,
    val code: Int,
    val message: String,
    val data: T?
)

// ==================== 健康检查 ====================

data class HealthData(
    val service: String,
    val version: String
)

// ==================== 应用列表 ====================

data class AppListData(
    val count: Int,
    val instances: List<AppInstanceDto>
)

data class AppInstanceDto(
    @SerializedName("virtual_name") val virtualName: String,
    @SerializedName("cache_prefix") val cachePrefix: String,
    @SerializedName("main_role") val mainRole: String,
    @SerializedName("is_running") val isRunning: Boolean
)

// ==================== 创建应用 ====================

data class AppConfigRequest(
    @SerializedName("virtual_name") val virtualName: String,
    @SerializedName("cache_prefix") val cachePrefix: String = "default",
    @SerializedName("warehouse_password") val warehousePassword: String = "",
    @SerializedName("main_role") val mainRole: String = "剑侠客",
    @SerializedName("dm_debug") val dmDebug: Boolean = false,
    @SerializedName("auto_init_actions") val autoInitActions: Boolean = false
)

data class AppCreateData(
    @SerializedName("virtual_name") val virtualName: String,
    @SerializedName("is_starting") val isStarting: Boolean
)

// ==================== 销毁应用 ====================

data class AppDestroyRequest(
    @SerializedName("virtual_name") val virtualName: String
)

data class AppDestroyData(
    @SerializedName("virtual_name") val virtualName: String,
    @SerializedName("is_destroyed") val isDestroyed: Boolean
)

// ==================== 发送消息 ====================

data class AppMsgRequest(
    @SerializedName("virtual_name") val virtualName: String,
    val msg: String
)

data class AppMsgData(
    @SerializedName("virtual_name") val virtualName: String,
    val msg: String
)

// ==================== 执行动作 ====================

data class AppActionRequest(
    @SerializedName("virtual_name") val virtualName: String,
    val action: String,
    val params: Map<String, Any> = emptyMap()
)

data class AppActionData(
    @SerializedName("virtual_name") val virtualName: String,
    val action: String
)

// ==================== 动作状态 ====================

data class ActionStatusData(
    @SerializedName("virtual_name") val virtualName: String,
    val actions: Map<String, Any>?
)

// ==================== 配置列表 ====================

data class ConfigListData(
    val configs: List<Map<String, Any>>,
    val count: Int
)
