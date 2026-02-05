package com.mhxy.app.data.remote

import retrofit2.http.*

// ============================================================
//                      API 服务接口
//  对接：game_assistant/src/app_server.py (FastAPI)
// ============================================================

interface ApiService {

    // ==================== 健康检查 ====================

    @GET("/")
    suspend fun healthCheck(): ApiResponse<HealthData>

    // ==================== 应用控制 ====================

    @GET("/app_list")
    suspend fun getAppList(): ApiResponse<AppListData>

    @POST("/app_create")
    suspend fun createApp(@Body config: AppConfigRequest): ApiResponse<AppCreateData>

    @POST("/app_destroy")
    suspend fun destroyApp(@Body request: AppDestroyRequest): ApiResponse<AppDestroyData>

    @POST("/app_msg")
    suspend fun sendMessage(@Body request: AppMsgRequest): ApiResponse<AppMsgData>

    @POST("/app_action")
    suspend fun executeAction(@Body request: AppActionRequest): ApiResponse<AppActionData>

    @GET("/action_status")
    suspend fun getActionStatus(@Query("virtual_name") virtualName: String): ApiResponse<ActionStatusData>

    // ==================== 配置管理 ====================

    @GET("/app_configs")
    suspend fun listAppConfigs(): ApiResponse<ConfigListData>

    @GET("/task_configs")
    suspend fun listTaskConfigs(@Query("task_type") taskType: String? = null): ApiResponse<ConfigListData>
}
