# API 接口映射文档

## 后端服务信息

**服务地址**：`http://<server_ip>:8000`  
**API 文档**：`http://<server_ip>:8000/docs`  
**技术栈**：FastAPI + Pydantic

---

## 统一响应格式

```json
{
  "status": "success" | "error",
  "code": 0,
  "message": "响应消息",
  "data": { ... },
  "meta": { ... }
}
```

---

## API 端点清单

### 1. 健康检查

| 属性 | 值 |
|------|-----|
| 端点 | `GET /` |
| 用途 | 检查服务器连接状态 |

**响应示例**：
```json
{
  "status": "success",
  "code": 0,
  "message": "Hello MHXY",
  "data": {
    "service": "MHXY Game Automation API",
    "version": "1.0.0"
  }
}
```

---

### 2. 应用控制 API

#### 2.1 获取应用列表

| 属性 | 值 |
|------|-----|
| 端点 | `GET /app_list` |
| 用途 | 获取所有运行中的应用实例 |

**响应示例**：
```json
{
  "status": "success",
  "code": 0,
  "message": "当前运行 2 个应用实例",
  "data": {
    "count": 2,
    "instances": [
      {
        "virtual_name": "GH-03 - VMware Workstation 16 Player",
        "cache_prefix": "gh03",
        "main_role": "剑侠客",
        "is_running": true
      },
      {
        "virtual_name": "GH-04 - VMware Workstation 16 Player",
        "cache_prefix": "gh04",
        "main_role": "飞燕女",
        "is_running": true
      }
    ]
  }
}
```

#### 2.2 创建应用实例

| 属性 | 值 |
|------|-----|
| 端点 | `POST /app_create` |
| 用途 | 创建并启动应用实例 |

**请求体**：
```json
{
  "virtual_name": "GH-03 - VMware Workstation 16 Player",
  "cache_prefix": "gh03",
  "warehouse_password": "123456",
  "main_role": "剑侠客",
  "dm_debug": false,
  "auto_init_actions": false
}
```

**响应示例**：
```json
{
  "status": "success",
  "code": 0,
  "message": "GH-03 - VMware Workstation 16 Player 正在启动",
  "data": {
    "virtual_name": "GH-03 - VMware Workstation 16 Player",
    "is_starting": true,
    "config": { ... }
  }
}
```

#### 2.3 销毁应用实例

| 属性 | 值 |
|------|-----|
| 端点 | `POST /app_destroy` |
| 用途 | 停止并清理应用实例 |

**请求体**：
```json
{
  "virtual_name": "GH-03 - VMware Workstation 16 Player"
}
```

**响应示例**：
```json
{
  "status": "success",
  "code": 0,
  "message": "GH-03 - VMware Workstation 16 Player 已销毁",
  "data": {
    "virtual_name": "GH-03 - VMware Workstation 16 Player",
    "is_destroyed": true
  }
}
```

#### 2.4 销毁所有应用

| 属性 | 值 |
|------|-----|
| 端点 | `POST /app_destroy_all` |
| 用途 | 停止所有运行中的应用 |

**响应示例**：
```json
{
  "status": "success",
  "code": 0,
  "message": "已销毁 2 个应用实例",
  "data": {
    "destroyed_count": 2,
    "virtual_names": ["GH-03 - ...", "GH-04 - ..."]
  }
}
```

#### 2.5 发送消息

| 属性 | 值 |
|------|-----|
| 端点 | `POST /app_msg` |
| 用途 | 发送控制消息（暂停/恢复等） |

**请求体**：
```json
{
  "virtual_name": "GH-03 - VMware Workstation 16 Player",
  "msg": "APP_GAME_PAUSE"
}
```

**可用消息类型（AppMsg）**：
- `APP_BIND_WINDOW` - 绑定窗口
- `APP_UNBIND_WINDOW` - 解绑窗口
- `APP_GAME_INIT` - 初始化游戏
- `APP_GAME_DESTROY` - 销毁游戏
- `APP_GAME_PAUSE` - 暂停游戏
- `APP_GAME_RESUME` - 恢复游戏
- `APP_INIT_THREADS` - 初始化线程
- `APP_DESTROY_THREADS` - 销毁线程
- `APP_INIT_ALL` - 初始化全部
- `APP_DESTROY_ALL` - 销毁全部

#### 2.6 执行动作

| 属性 | 值 |
|------|-----|
| 端点 | `POST /app_action` |
| 用途 | 执行自动化任务（捉鬼/跑商等） |

**请求体**：
```json
{
  "virtual_name": "GH-03 - VMware Workstation 16 Player",
  "action": "BP_TRADE_ACTION",
  "params": {
    "target_silver": 50000,
    "max_rounds": 50,
    "start_x": 100,
    "start_y": 200
  }
}
```

**可用动作类型（AppAction）**：
- `APP_INIT` - 应用初始化
- `OPEN_GAME_ACTION` - 打开游戏
- `INIT_ACTION` - 初始化动作
- `APP_RESET_WINDOW_ACTION` - 重置窗口
- `CATCH_GHOST_ACTION` - 捉鬼任务
- `FLYING_THIEF_ACTION` - 飞贼任务
- `BP_TRADE_ACTION` - 跑商任务

#### 2.7 获取任务状态

| 属性 | 值 |
|------|-----|
| 端点 | `GET /task_status?virtual_name=xxx` |
| 用途 | 获取任务执行状态 |

#### 2.8 获取动作状态

| 属性 | 值 |
|------|-----|
| 端点 | `GET /action_status?virtual_name=xxx` |
| 用途 | 获取当前运行的动作状态 |

**响应示例**：
```json
{
  "status": "success",
  "code": 0,
  "message": "获取动作状态成功",
  "data": {
    "virtual_name": "GH-03 - VMware Workstation 16 Player",
    "actions": { ... }
  }
}
```

---

### 3. 应用配置 API

#### 3.1 获取配置列表

| 属性 | 值 |
|------|-----|
| 端点 | `GET /app_configs` |
| 用途 | 获取所有应用配置模板 |

#### 3.2 获取单个配置

| 属性 | 值 |
|------|-----|
| 端点 | `GET /app_configs/{config_id}` |
| 用途 | 获取指定配置详情 |

#### 3.3 创建配置

| 属性 | 值 |
|------|-----|
| 端点 | `POST /app_configs` |
| 用途 | 创建新的应用配置 |

**请求体**：
```json
{
  "config_id": "gh03_default",
  "config_name": "GH-03 默认配置",
  "virtual_name": "GH-03 - VMware Workstation 16 Player",
  "cache_prefix": "gh03",
  "main_role": "剑侠客",
  "warehouse_password": "123456",
  "dm_debug": false,
  "auto_init_actions": false
}
```

#### 3.4 更新配置

| 属性 | 值 |
|------|-----|
| 端点 | `PUT /app_configs/{config_id}` |
| 用途 | 更新现有配置 |

#### 3.5 删除配置

| 属性 | 值 |
|------|-----|
| 端点 | `DELETE /app_configs/{config_id}` |
| 用途 | 删除指定配置 |

---

### 4. 任务配置 API

#### 4.1 获取配置列表

| 属性 | 值 |
|------|-----|
| 端点 | `GET /task_configs` |
| 用途 | 获取所有任务配置模板 |
| 参数 | `task_type`（可选）- 按任务类型过滤 |

#### 4.2 获取单个配置

| 属性 | 值 |
|------|-----|
| 端点 | `GET /task_configs/{config_id}` |
| 用途 | 获取指定任务配置详情 |

#### 4.3 创建配置

| 属性 | 值 |
|------|-----|
| 端点 | `POST /task_configs` |
| 用途 | 创建新的任务配置 |

**请求体**：
```json
{
  "config_id": "newbee_merchant",
  "config_name": "NewBee 跑商",
  "task_type": "merchant",
  "action": "BP_TRADE_ACTION",
  "params": {
    "target_silver": 50000,
    "max_rounds": 50,
    "start_x": 100,
    "start_y": 200
  }
}
```

#### 4.4 更新配置

| 属性 | 值 |
|------|-----|
| 端点 | `PUT /task_configs/{config_id}` |
| 用途 | 更新现有任务配置 |

#### 4.5 删除配置

| 属性 | 值 |
|------|-----|
| 端点 | `DELETE /task_configs/{config_id}` |
| 用途 | 删除指定任务配置 |

---

## Android App 数据模型映射

### AppConfig（应用配置）

```kotlin
data class AppConfig(
    val virtualName: String,           // virtual_name
    val cachePrefix: String = "default", // cache_prefix
    val warehousePassword: String = "", // warehouse_password
    val mainRole: String = "剑侠客",    // main_role
    val dmDebug: Boolean = false,       // dm_debug
    val autoInitActions: Boolean = false // auto_init_actions
)
```

### AppInstance（应用实例）

```kotlin
data class AppInstance(
    val virtualName: String,    // virtual_name
    val cachePrefix: String,    // cache_prefix
    val mainRole: String,       // main_role
    val isRunning: Boolean      // is_running
)
```

### AppAction（动作类型）

```kotlin
enum class AppAction(val value: String) {
    APP_INIT("APP_INIT"),
    OPEN_GAME("OPEN_GAME_ACTION"),
    INIT("INIT_ACTION"),
    RESET_WINDOW("APP_RESET_WINDOW_ACTION"),
    CATCH_GHOST("CATCH_GHOST_ACTION"),
    FLYING_THIEF("FLYING_THIEF_ACTION"),
    BP_TRADE("BP_TRADE_ACTION")
}
```

### AppMsg（消息类型）

```kotlin
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
```

---

## 错误处理

### HTTP 状态码

| 状态码 | 含义 |
|--------|------|
| 200 | 成功 |
| 201 | 创建成功 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 错误响应格式

```json
{
  "detail": "应用实例不存在: GH-03 - VMware Workstation 16 Player"
}
```

---

## App 功能与 API 映射

| App 功能 | API 端点 | 方法 |
|----------|----------|------|
| 首页状态 | `/app_list` | GET |
| 健康检查 | `/` | GET |
| 创建应用 | `/app_create` | POST |
| 销毁应用 | `/app_destroy` | POST |
| 暂停任务 | `/app_msg` (APP_GAME_PAUSE) | POST |
| 恢复任务 | `/app_msg` (APP_GAME_RESUME) | POST |
| 启动捉鬼 | `/app_action` (CATCH_GHOST_ACTION) | POST |
| 启动飞贼 | `/app_action` (FLYING_THIEF_ACTION) | POST |
| 启动跑商 | `/app_action` (BP_TRADE_ACTION) | POST |
| 查看动作状态 | `/action_status` | GET |
| 获取应用配置 | `/app_configs` | GET |
| 获取任务配置 | `/task_configs` | GET |

---

**文档版本**：v1.0  
**创建时间**：2026-02-05  
**数据来源**：game_assistant/src/api_*.py
