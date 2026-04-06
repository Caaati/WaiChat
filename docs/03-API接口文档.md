# WaiChat API 接口文档

## 基本约定

- 开发环境前缀：前端使用 `/api` 代理到后端 `http://127.0.0.1:8080`
- 统一返回格式（后端 `Result<T>`）：
  - `code`: 状态码（成功 `200`，失败常见 `100`）
  - `message`: 提示信息
  - `data`: 业务数据
- 鉴权：
  - 放行：`/login`、`/register`、`/api/chat/send`、`/uploads/**`
  - 其余接口默认需要登录态

---

## 1. 用户与鉴权

### 1.1 登录

- **URL**: `POST /login`（前端调用 `/api/login`）
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `username` string
  - `password` string
- **成功响应示例**:

```json
{
  "code": 200,
  "message": "请求成功!",
  "data": {
    "id": 1,
    "username": "tom",
    "nickname": "Tom"
  }
}
```

### 1.2 注册

- **URL**: `POST /register`（前端调用 `/api/register`）
- **Content-Type**: `application/json`
- **请求体**:

```json
{
  "username": "tom",
  "password": "123456"
}
```

- **说明**: 用户名重复返回错误信息。

### 1.3 搜索用户

- **URL**: `GET /user/search?key=xxx`（前端调用 `/api/user/search`）
- **参数**:
  - `key` string（支持 username / nickname / id 模糊检索）
- **返回 data**: `UserContactDTO[]`

---

## 2. 聊天接口

### 2.1 发送消息（HTTP）

- **URL**: `POST /chat/send`（前端调用 `/api/chat/send`）
- **Content-Type**: `application/json`
- **请求体（Chat）关键字段**:
  - `userId` number
  - `targetId` number
  - `content` string
- **说明**: 实际推荐走 WebSocket；HTTP 接口作为发送兜底入口。

### 2.2 广播消息（测试）

- **URL**: `POST /chat/sendall?message=xxx`
- **用途**: 向所有在线连接广播（调试用途）。

### 2.3 获取聊天记录

- **URL**: `GET /chat/history?userId={id}&targetId={id}`（前端 `/api/chat/history`）
- **返回 data**: `Chat[]`

### 2.4 获取联系人列表

- **URL**: `GET /chat/getContactList?userId={id}`（前端 `/api/chat/getContactList`）
- **返回 data**: `UserContactDTO[]`

### 2.5 隐藏聊天记录

- **URL**: `POST /chat/removeHistory`（前端 `/api/chat/removeHistory`）
- **请求体**:

```json
{
  "userId": 1,
  "targetId": 2
}
```

### 2.6 恢复聊天记录

- **URL**: `POST /chat/recoverHistory`（前端 `/api/chat/recoverHistory`）
- **请求体**:

```json
{
  "userId": 1,
  "targetId": 2
}
```

---

## 3. AI 接口

### 3.1 获取语言列表

- **URL**: `GET /ai/languages`（前端 `/api/ai/languages`）
- **返回 data**: 语言列表（包含 `displayName` 等字段）

### 3.2 文本翻译

- **URL**: `POST /ai/translate`（前端 `/api/ai/translate`）
- **请求体**:

```json
{
  "text": "hello",
  "target": "zh"
}
```

- **返回 data**:

```json
{
  "original": "hello",
  "translated": "你好"
}
```

### 3.3 语音转文字（URL）

- **URL**: `POST /ai/voiceToText`（前端 `/api/ai/voiceToText`）
- **请求体**:

```json
{
  "audioUrl": "http://xxx/uploads/audio/2026/0406/a.webm"
}
```

- **返回 data**:

```json
{
  "text": "识别出的文本",
  "language": "zh"
}
```

### 3.4 文本润色

- **URL**: `POST /ai/polish`（前端 `/api/ai/polish`）
- **请求体**:

```json
{
  "text": "帮我改一下语气",
  "style": "business"
}
```

- **style 可选**: `business` / `casual`

### 3.5 智能回复

- **URL**: `POST /ai/smartReply`（前端 `/api/ai/smartReply`）
- **请求体**: `ChatDTO[]`
- **说明**:
  - 数组一般包含历史消息，消息 `userId` 使用“我/对方”标签；
  - 最后可能附带真实 `userId/targetId` 用于补充身份上下文。

### 3.6 聊天总结

- **URL**: `POST /ai/summarize`（前端 `/api/ai/summarize`）
- **请求体**: `ChatDTO[]`
- **返回**: 分点总结文本

### 3.7 聊天分析

- **URL**: `POST /ai/analysis`（前端 `/api/ai/analysis`）
- **请求体**: `[{ "userId": "...", "content": "..." }]`
- **返回 data 结构**:
  - `keywords`: 高频词数组
  - `sentiment`: 最近消息情感数组
  - `summary`: 关系摘要

### 3.8 语音转写（文件上传）

- **URL**: `POST /ai/audio/stt`（前端 `/api/ai/audio/stt`）
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `file` 音频文件
- **返回 data**: 识别文本字符串

---

## 4. 文件上传与静态资源

### 4.1 上传音频

- **URL**: `POST /upload`（前端 `/api/upload`）
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `file` 音频文件（后端保存为 `.webm`）
- **返回 data**: 可访问音频 URL（用于后续语音识别）

### 4.2 静态访问

- **URL 前缀**: `/uploads/**`
- **用途**: 访问上传后的音频文件资源

---

## 5. WebSocket 通道

- **连接地址**: `ws://{host}:{port}/ws/{userId}`
- **前端开发代理**: `ws://127.0.0.1:5173/ws/{userId}` -> 后端
- **消息体**: 建议发送 `Chat` JSON
- **后端行为**:
  - 服务端补齐 `userId/createTime`
  - 写入 Redis 热数据
  - 异步写入 MySQL
  - 向目标用户在线会话推送消息

## 6. 错误码

- `200`: 请求成功
- `100`: 通用失败
- `101`: 插入失败
- `102`: 更新失败
- `103`: 删除失败
- `104`: 查询失败
