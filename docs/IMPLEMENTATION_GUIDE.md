# Android App 实现指南

## 项目初始化

### 1. 创建项目

```bash
# 使用 Android Studio 创建新项目
# 选择：Empty Compose Activity
# 最低 SDK：API 24 (Android 7.0)
# 语言：Kotlin
# 构建系统：Gradle (Kotlin DSL)
```

### 2. 依赖配置（build.gradle.kts）

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.mhxy.assistant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mhxy.assistant"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

---

## 模块结构

```
app/src/main/java/com/mhxy/assistant/
├── MainActivity.kt                  # 入口 Activity
│
├── ui/                              # UI 层
│   ├── theme/                       # 主题配置
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   ├── components/                  # 通用组件
│   │   ├── AppCard.kt              # 应用卡片
│   │   ├── StatusBadge.kt          # 状态徽章
│   │   └── LoadingScreen.kt        # 加载屏幕
│   │
│   ├── home/                        # 首页
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   │
│   ├── detail/                      # 应用详情
│   │   ├── DetailScreen.kt
│   │   └── DetailViewModel.kt
│   │
│   ├── task/                        # 任务中心
│   │   ├── TaskScreen.kt
│   │   └── TaskViewModel.kt
│   │
│   └── settings/                    # 设置
│       ├── SettingsScreen.kt
│       └── SettingsViewModel.kt
│
├── data/                            # 数据层
│   ├── remote/                      # 网络请求
│   │   ├── ApiService.kt           # API 接口定义
│   │   ├── dto/                    # 数据传输对象
│   │   │   ├── AppDto.kt
│   │   │   ├── TaskDto.kt
│   │   │   └── ResponseDto.kt
│   │   └── NetworkModule.kt        # 网络模块（Hilt）
│   │
│   ├── local/                       # 本地存储
│   │   ├── PreferencesManager.kt   # DataStore 封装
│   │   └── LocalModule.kt          # 本地模块（Hilt）
│   │
│   └── repository/                  # 仓库实现
│       ├── AppRepository.kt
│       └── TaskRepository.kt
│
├── domain/                          # 业务逻辑层
│   ├── model/                       # 领域模型
│   │   ├── App.kt
│   │   ├── Task.kt
│   │   └── AppStatus.kt
│   │
│   └── usecase/                     # 用例
│       ├── GetAppsUseCase.kt
│       ├── StartTaskUseCase.kt
│       └── StopTaskUseCase.kt
│
└── di/                              # 依赖注入
    └── AppModule.kt                # 应用模块
```

---

## 核心代码实现

### 1. 领域模型（domain/model/App.kt）

```kotlin
package com.mhxy.assistant.domain.model

data class App(
    val virtualName: String,
    val characterName: String,
    val status: AppStatus,
    val currentTask: Task?,
    val startTime: Long?,
    val runningDuration: Long?
)

enum class AppStatus {
    RUNNING,
    STOPPED,
    ERROR
}

data class Task(
    val name: String,
    val type: String,
    val progress: TaskProgress?
)

data class TaskProgress(
    val current: Int,
    val total: Int,
    val silverCurrent: Int,
    val silverTarget: Int
)
```


### 2. API 接口（data/remote/ApiService.kt）

```kotlin
package com.mhxy.assistant.data.remote

import com.mhxy.assistant.data.remote.dto.*
import retrofit2.http.*

/**
 * game_assistant FastAPI 服务接口
 * 对接：game_assistant/src/app_server.py
 */
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
    
    @POST("/app_destroy_all")
    suspend fun destroyAllApps(): ApiResponse<AppDestroyAllData>
    
    @POST("/app_msg")
    suspend fun sendMessage(@Body request: AppMsgRequest): ApiResponse<AppMsgData>
    
    @POST("/app_action")
    suspend fun executeAction(@Body request: AppActionRequest): ApiResponse<AppActionData>
    
    @GET("/task_status")
    suspend fun getTaskStatus(@Query("virtual_name") virtualName: String): ApiResponse<TaskStatusData>
    
    @GET("/action_status")
    suspend fun getActionStatus(@Query("virtual_name") virtualName: String): ApiResponse<ActionStatusData>
    
    // ==================== 应用配置 ====================
    
    @GET("/app_configs")
    suspend fun listAppConfigs(): ApiResponse<AppConfigsData>
    
    @GET("/app_configs/{config_id}")
    suspend fun getAppConfig(@Path("config_id") configId: String): ApiResponse<AppConfigData>
    
    @POST("/app_configs")
    suspend fun createAppConfig(@Body request: AppConfigCreateRequest): ApiResponse<ConfigIdData>
    
    @PUT("/app_configs/{config_id}")
    suspend fun updateAppConfig(
        @Path("config_id") configId: String,
        @Body request: AppConfigCreateRequest
    ): ApiResponse<ConfigIdData>
    
    @DELETE("/app_configs/{config_id}")
    suspend fun deleteAppConfig(@Path("config_id") configId: String): ApiResponse<ConfigIdData>
    
    // ==================== 任务配置 ====================
    
    @GET("/task_configs")
    suspend fun listTaskConfigs(@Query("task_type") taskType: String? = null): ApiResponse<TaskConfigsData>
    
    @GET("/task_configs/{config_id}")
    suspend fun getTaskConfig(@Path("config_id") configId: String): ApiResponse<TaskConfigData>
    
    @POST("/task_configs")
    suspend fun createTaskConfig(@Body request: TaskConfigCreateRequest): ApiResponse<ConfigIdData>
    
    @PUT("/task_configs/{config_id}")
    suspend fun updateTaskConfig(
        @Path("config_id") configId: String,
        @Body request: TaskConfigCreateRequest
    ): ApiResponse<ConfigIdData>
    
    @DELETE("/task_configs/{config_id}")
    suspend fun deleteTaskConfig(@Path("config_id") configId: String): ApiResponse<ConfigIdData>
}
```

### 2.1 DTO 定义（data/remote/dto/）

```kotlin
package com.mhxy.assistant.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== 统一响应 ====================

data class ApiResponse<T>(
    val status: String,           // "success" | "error"
    val code: Int,                // 0 = 成功
    val message: String,
    val data: T?,
    val meta: Map<String, Any>? = null
)

// ==================== 健康检查 ====================

data class HealthData(
    val service: String,
    val version: String
)

// ==================== 应用控制 ====================

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
    @SerializedName("is_starting") val isStarting: Boolean,
    val config: Map<String, Any>?
)

data class AppDestroyRequest(
    @SerializedName("virtual_name") val virtualName: String
)

data class AppDestroyData(
    @SerializedName("virtual_name") val virtualName: String,
    @SerializedName("is_destroyed") val isDestroyed: Boolean
)

data class AppDestroyAllData(
    @SerializedName("destroyed_count") val destroyedCount: Int,
    @SerializedName("virtual_names") val virtualNames: List<String>
)

data class AppMsgRequest(
    @SerializedName("virtual_name") val virtualName: String,
    val msg: String  // AppMsg 枚举值
)

data class AppMsgData(
    @SerializedName("virtual_name") val virtualName: String,
    val msg: String
)

data class AppActionRequest(
    @SerializedName("virtual_name") val virtualName: String,
    val action: String,  // AppAction 枚举值
    val params: Map<String, Any> = emptyMap()
)

data class AppActionData(
    @SerializedName("virtual_name") val virtualName: String,
    val action: String,
    val params: Map<String, Any>
)

data class TaskStatusData(
    @SerializedName("virtual_name") val virtualName: String
)

data class ActionStatusData(
    @SerializedName("virtual_name") val virtualName: String,
    val actions: Map<String, Any>?
)

// ==================== 应用配置 ====================

data class AppConfigsData(
    val configs: List<Map<String, Any>>,
    val count: Int
)

data class AppConfigData(
    val config: Map<String, Any>
)

data class AppConfigCreateRequest(
    @SerializedName("config_id") val configId: String,
    @SerializedName("config_name") val configName: String,
    @SerializedName("virtual_name") val virtualName: String,
    @SerializedName("cache_prefix") val cachePrefix: String,
    @SerializedName("main_role") val mainRole: String = "剑侠客",
    @SerializedName("warehouse_password") val warehousePassword: String = "",
    @SerializedName("dm_debug") val dmDebug: Boolean = false,
    @SerializedName("auto_init_actions") val autoInitActions: Boolean = false
)

data class ConfigIdData(
    @SerializedName("config_id") val configId: String
)

// ==================== 任务配置 ====================

data class TaskConfigsData(
    val configs: List<Map<String, Any>>,
    val count: Int
)

data class TaskConfigData(
    val config: Map<String, Any>
)

data class TaskConfigCreateRequest(
    @SerializedName("config_id") val configId: String,
    @SerializedName("config_name") val configName: String,
    @SerializedName("task_type") val taskType: String,
    val action: String,
    val params: Map<String, Any> = emptyMap()
)
```

### 2.2 枚举定义（domain/model/Enums.kt）

```kotlin
package com.mhxy.assistant.domain.model

/**
 * 应用消息类型
 * 对应：game_assistant/src/soda/msg.py -> AppMsg
 */
enum class AppMsg(val value: String) {
    BIND_WINDOW("APP_BIND_WINDOW"),
    UNBIND_WINDOW("APP_UNBIND_WINDOW"),
    GAME_INIT("APP_GAME_INIT"),
    GAME_DESTROY("APP_GAME_DESTROY"),
    GAME_PAUSE("APP_GAME_PAUSE"),
    GAME_RESUME("APP_GAME_RESUME"),
    INIT_THREADS("APP_INIT_THREADS"),
    DESTROY_THREADS("APP_DESTROY_THREADS"),
    INIT_ALL("APP_INIT_ALL"),
    DESTROY_ALL("APP_DESTROY_ALL")
}

/**
 * 应用动作类型
 * 对应：game_assistant/src/soda/msg.py -> AppAction
 */
enum class AppAction(val value: String, val displayName: String) {
    APP_INIT("APP_INIT", "应用初始化"),
    OPEN_GAME("OPEN_GAME_ACTION", "打开游戏"),
    INIT("INIT_ACTION", "初始化"),
    RESET_WINDOW("APP_RESET_WINDOW_ACTION", "重置窗口"),
    CATCH_GHOST("CATCH_GHOST_ACTION", "捉鬼任务"),
    FLYING_THIEF("FLYING_THIEF_ACTION", "飞贼任务"),
    BP_TRADE("BP_TRADE_ACTION", "跑商任务")
}

/**
 * 应用状态
 */
enum class AppStatus {
    RUNNING,    // 运行中
    STOPPED,    // 已停止
    STARTING,   // 启动中
    ERROR       // 错误
}
```

### 3. 仓库实现（data/repository/AppRepository.kt）

```kotlin
package com.mhxy.assistant.data.repository

import com.mhxy.assistant.data.remote.ApiService
import com.mhxy.assistant.data.remote.dto.*
import com.mhxy.assistant.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    fun getApps(): Flow<Result<List<App>>> = flow {
        try {
            val response = apiService.getAppList()
            if (response.code == 0 && response.data != null) {
                val apps = response.data.instances.map { it.toDomain() }
                emit(Result.success(apps))
            } else {
                emit(Result.failure(Exception(response.message)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun createApp(
        virtualName: String,
        characterName: String,
        password: String
    ): Result<Unit> {
        return try {
            val request = CreateAppRequest(virtualName, characterName, password)
            val response = apiService.createApp(request)
            if (response.code == 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun destroyApp(virtualName: String): Result<Unit> {
        return try {
            val request = DestroyAppRequest(virtualName)
            val response = apiService.destroyApp(request)
            if (response.code == 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun startTask(
        virtualName: String,
        action: String,
        params: Map<String, Any>
    ): Result<Unit> {
        return try {
            val request = StartActionRequest(virtualName, action, params)
            val response = apiService.startAction(request)
            if (response.code == 0) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// DTO → Domain 映射
private fun AppDto.toDomain(): App {
    return App(
        virtualName = virtual_name,
        characterName = character_name,
        status = when (status) {
            "running" -> AppStatus.RUNNING
            "stopped" -> AppStatus.STOPPED
            else -> AppStatus.ERROR
        },
        currentTask = current_task?.toDomain(),
        startTime = start_time,
        runningDuration = start_time?.let { System.currentTimeMillis() - it }
    )
}

private fun TaskDto.toDomain(): Task {
    return Task(
        name = name,
        type = type,
        progress = progress?.let {
            TaskProgress(
                current = it.current,
                total = it.total,
                silverCurrent = it.silver_current,
                silverTarget = it.silver_target
            )
        }
    )
}
```

### 4. ViewModel（ui/home/HomeViewModel.kt）

```kotlin
package com.mhxy.assistant.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhxy.assistant.data.repository.AppRepository
import com.mhxy.assistant.domain.model.App
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {
    
    // UI 状态
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    // 轮询控制
    private val _isPolling = MutableStateFlow(false)
    
    init {
        startPolling()
    }
    
    // 启动轮询（5 秒间隔）
    private fun startPolling() {
        viewModelScope.launch {
            _isPolling.value = true
            while (_isPolling.value) {
                fetchApps()
                delay(5000) // 5 秒轮询
            }
        }
    }
    
    // 停止轮询
    fun stopPolling() {
        _isPolling.value = false
    }
    
    // 获取应用列表
    private fun fetchApps() {
        viewModelScope.launch {
            appRepository.getApps().collect { result ->
                _uiState.value = result.fold(
                    onSuccess = { apps -> HomeUiState.Success(apps) },
                    onFailure = { error -> HomeUiState.Error(error.message ?: "未知错误") }
                )
            }
        }
    }
    
    // 手动刷新
    fun refresh() {
        _uiState.value = HomeUiState.Loading
        fetchApps()
    }
    
    // 启动任务
    fun startTask(virtualName: String, action: String, params: Map<String, Any>) {
        viewModelScope.launch {
            // 乐观更新：立即显示"启动中"
            _uiState.value = (_uiState.value as? HomeUiState.Success)?.let {
                HomeUiState.Success(it.apps, isLoading = true)
            } ?: _uiState.value
            
            val result = appRepository.startTask(virtualName, action, params)
            
            result.fold(
                onSuccess = {
                    // 成功：刷新列表
                    fetchApps()
                },
                onFailure = { error ->
                    // 失败：显示错误，回滚状态
                    _uiState.value = (_uiState.value as? HomeUiState.Success)?.let {
                        HomeUiState.Success(it.apps, error = error.message)
                    } ?: _uiState.value
                }
            )
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}

// UI 状态密封类
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val apps: List<App>,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
```

### 5. Compose UI（ui/home/HomeScreen.kt）

```kotlin
package com.mhxy.assistant.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mhxy.assistant.ui.components.AppCard
import com.mhxy.assistant.ui.components.LoadingScreen

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onAppClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🏠 游戏助手") }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                LoadingScreen()
            }
            
            is HomeUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 服务器状态
                    item {
                        ServerStatusCard()
                    }
                    
                    // 应用列表
                    items(state.apps) { app ->
                        AppCard(
                            app = app,
                            onClick = { onAppClick(app.virtualName) },
                            onStartTask = { action, params ->
                                viewModel.startTask(app.virtualName, action, params)
                            }
                        )
                    }
                }
                
                // 错误提示
                state.error?.let { error ->
                    Snackbar(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(error)
                    }
                }
            }
            
            is HomeUiState.Error -> {
                ErrorScreen(
                    message = state.message,
                    onRetry = { viewModel.refresh() }
                )
            }
        }
    }
}

@Composable
fun ServerStatusCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("●", color = MaterialTheme.colorScheme.primary)
            Text("已连接 192.168.1.100")
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(message)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("重试")
        }
    }
}
```

### 6. 应用卡片组件（ui/components/AppCard.kt）

```kotlin
package com.mhxy.assistant.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mhxy.assistant.domain.model.App
import com.mhxy.assistant.domain.model.AppStatus

@Composable
fun AppCard(
    app: App,
    onClick: () -> Unit,
    onStartTask: (String, Map<String, Any>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${app.virtualName} · ${app.characterName}",
                    style = MaterialTheme.typography.titleMedium
                )
                StatusBadge(status = app.status)
            }
            
            // 任务信息
            if (app.currentTask != null) {
                Text("📍 ${app.currentTask.name}")
                app.runningDuration?.let { duration ->
                    Text("⏱️  ${formatDuration(duration)}")
                }
            } else {
                Text("📍 无任务", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (app.status) {
                    AppStatus.RUNNING -> {
                        Button(onClick = { /* 暂停 */ }) {
                            Text("⏸️ 暂停")
                        }
                        OutlinedButton(onClick = { /* 停止 */ }) {
                            Text("⏹️ 停止")
                        }
                    }
                    AppStatus.STOPPED -> {
                        Button(
                            onClick = {
                                onStartTask("newbee_merchant", emptyMap())
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("▶️ 启动任务")
                        }
                    }
                    AppStatus.ERROR -> {
                        Button(onClick = { /* 重试 */ }) {
                            Text("🔄 重试")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: AppStatus) {
    val (text, color) = when (status) {
        AppStatus.RUNNING -> "● 运行中" to MaterialTheme.colorScheme.primary
        AppStatus.STOPPED -> "○ 已停止" to MaterialTheme.colorScheme.onSurfaceVariant
        AppStatus.ERROR -> "✖️ 错误" to MaterialTheme.colorScheme.error
    }
    
    Text(text, color = color)
}

private fun formatDuration(millis: Long): String {
    val hours = millis / 3600000
    val minutes = (millis % 3600000) / 60000
    return "${hours}h ${minutes}m"
}
```

---

## 依赖注入配置

### NetworkModule.kt

```kotlin
package com.mhxy.assistant.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.100:8000/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

---

## 主题配置

### Color.kt

```kotlin
package com.mhxy.assistant.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF2196F3)
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFC107)
val Error = Color(0xFFF44336)
val Neutral = Color(0xFF9E9E9E)
```

### Theme.kt

```kotlin
package com.mhxy.assistant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    error = Error,
    background = Color.White,
    surface = Color(0xFFF5F5F5)
)

@Composable
fun AssistantTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## 入口配置

### MainActivity.kt

```kotlin
package com.mhxy.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mhxy.assistant.ui.home.HomeScreen
import com.mhxy.assistant.ui.theme.AssistantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AssistantTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onAppClick = { virtualName ->
                    navController.navigate("detail/$virtualName")
                }
            )
        }
        
        composable("detail/{virtualName}") {
            // DetailScreen(...)
        }
    }
}
```

---

## 构建与运行

```bash
# 1. 同步依赖
./gradlew build

# 2. 运行到设备
./gradlew installDebug

# 3. 查看日志
adb logcat | grep "AssistantApp"
```

---

**文档版本**：v1.0  
**创建时间**：2026-02-05
