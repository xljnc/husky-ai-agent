# Spring AI + WebFlux MCP Search Client

基于 **Spring AI + WebFlux** 实现的 MCP 客户端，集成 **AnySearch MCP** 搜索能力，对外提供 **SSE 异步流式接口**
，可直接对接前端页面实现打字机式流式对话体验。

## 🏗️ 技术架构

```
┌─────────────┐     SSE流      ┌─────────────────────┐    MCP/STDIO    ┌─────────────────┐
│   前端页面   │ ──────────────> │ Spring Boot WebFlux │ ──────────────> │ AnySearch MCP   │
│ (index.html)│ <────────────── │   MCP Client        │ <────────────── │   Server        │
└─────────────┘   逐token返回    └─────────────────────┘    工具调用结果  └─────────────────┘
                                        │
                                        │ 调用大模型
                                        ▼
                                 ┌─────────────────┐
                                 │ DeepSeek/OpenAI │
                                 │ (工具调用推理)   │
                                 └─────────────────┘
```

## ✨ 核心特性

- ✅ **Spring AI 1.0.0** 原生 MCP Client 支持
- ✅ **WebFlux** 全响应式非阻塞架构
- ✅ **AnySearch MCP** 自动接入（STDIO模式，自动npx启动）
- ✅ **SSE 流式接口** 支持实时打字机输出
- ✅ **自动工具调用** 大模型自主判断何时搜索
- ✅ **内置前端测试页面** 开箱即用
- ✅ **心跳保活** 防止连接超时
- ✅ **错误处理** 完善的异常捕获与降级

## 📁 项目结构

```
mcp-search-client/
├── pom.xml                                    # Maven依赖配置
├── src/main/java/com/example/mcpsearch/
│   ├── McpSearchClientApplication.java       # 启动类
│   ├── config/
│   │   └── ChatClientConfig.java             # ChatClient配置（自动挂载MCP工具）
│   ├── controller/
│   │   └── ChatController.java               # SSE流式接口Controller
│   └── service/
│       └── SearchChatService.java            # 聊天业务服务
└── src/main/resources/
    ├── application.yml                        # 应用配置
    ├── mcp-servers.json                       # MCP Server启动配置
    └── static/
        └── index.html                         # 前端测试页面
```

## 🚀 快速启动

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js / npx（用于启动anysearch-mcp-server，STDIO模式需要）

### 2. 获取模型API Key

本项目默认使用 **DeepSeek**（国内可直接访问，工具调用能力优秀），也支持任意OpenAI兼容接口。

- 前往 [DeepSeek开放平台](https://platform.deepseek.com/) 注册获取API Key

### 3. 配置环境变量（可选）

可以通过环境变量配置，也可以直接修改 `application.yml`：

```bash
# Linux/Mac
export OPENAI_API_KEY=your-deepseek-api-key
export OPENAI_BASE_URL=https://api.deepseek.com
export OPENAI_MODEL=deepseek-chat

# AnySearch API Key（可选，匿名模式可留空，有免费额度）
export ANYSEARCH_API_KEY=
```

### 4. 启动项目

```bash
cd mcp-search-client
mvn spring-boot:run
```

首次启动时，Spring AI MCP Client会自动通过npx下载并启动 `anysearch-mcp-server`，请耐心等待。

### 5. 访问测试页面

打开浏览器访问： **http://localhost:8080**

即可看到聊天界面，直接输入问题即可体验AI联网搜索流式回答。

## 🔌 API接口说明

### SSE流式聊天接口（推荐）

**GET /api/chat/stream?message=你的问题**

Content-Type: `text/event-stream`

SSE事件类型：

- `start`: 连接建立成功
- `message`: 文本内容片段（逐token返回）
- `error`: 错误信息
- `end`: 响应结束
- `comment: heartbeat`: 心跳保活（每15秒）

前端调用示例（fetch方式）：

```javascript
const response = await fetch(`/api/chat/stream?message=${encodeURIComponent(message)}`);
const reader = response.body.getReader();
const decoder = new TextDecoder();

while (true) {
    const {done, value} = await reader.read();
    if (done) break;
    const text = decoder.decode(value);
    // 解析SSE数据...
}
```

### POST流式接口

**POST /api/chat/stream**

Content-Type: `application/json`

请求体：

```json
{
    "message": "今天有什么新闻？"
}
```

### 普通非流式接口

**POST /api/chat/send**

Content-Type: `application/json`

请求体：

```json
{
    "message": "你好"
}
```

响应：

```json
{
    "success": true,
    "data": "回答内容..."
}
```

### 健康检查

**GET /api/chat/health**

## ⚙️ 配置说明

### 更换模型

修改 `application.yml` 中的模型配置，支持任意OpenAI兼容接口：

```yaml
spring:
  ai:
    openai:
      api-key: your-api-key
      base-url: https://api.openai.com  # 或其他兼容地址
      chat:
        options:
          model: gpt-4o
```

支持的模型服务：

- DeepSeek: `https://api.deepseek.com`
- OpenAI: `https://api.openai.com/v1`
- 通义千问: `https://dashscope.aliyuncs.com/compatible-mode/v1`
- 智谱AI: `https://open.bigmodel.cn/api/paas/v4`
- Ollama本地模型: `http://localhost:11434`

### 切换MCP连接模式

#### STDIO模式（默认，推荐）

自动通过npx启动本地MCP Server进程，配置在 `mcp-servers.json` 中。

#### SSE远程模式

如需连接远程部署的AnySearch服务，修改 `application.yml`：

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            anysearch:
              url: http://your-mcp-server:8090/sse
```

## 🔧 核心代码说明

### 1. MCP Client自动配置

Spring AI MCP Starter会自动读取 `mcp-servers.json` 配置，启动对应MCP Server进程，并将所有暴露的工具自动包装为
`ToolCallbackProvider`。

### 2. ChatClient挂载MCP工具

```java
@Bean
public ChatClient chatClient(ChatClient.Builder builder, ToolCallbackProvider toolCallbackProvider) {
    return builder
            .defaultSystem("...")
            .defaultToolCallbacks(toolCallbackProvider)  // 自动挂载所有MCP工具
            .build();
}
```

### 3. WebFlux SSE流式返回

```java
@GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ServerSentEvent<String>> streamChat(@RequestParam String message) {
    return searchChatService.streamChat(message)
            .map(chunk -> ServerSentEvent.builder()
                    .event("message")
                    .data(chunk)
                    .build());
}
```

## 📝 常见问题

### 1. npx启动anysearch-mcp-server失败？

确保已安装Node.js（建议18+版本），可以手动执行测试：

```bash
npx -y anysearch-mcp-server
```

### 2. 模型不调用搜索工具？

确保：

- 使用支持工具调用的模型（如deepseek-chat、gpt-4o等）
- 配置中设置了 `tool-choice: auto`
- 问题确实涉及实时信息需要搜索

### 3. SSE连接断开？

项目内置了15秒一次的心跳保活，如仍有问题可检查Nginx/网关的超时配置，建议设置为60秒以上。

## 📄 License

MIT
