# HuaAIAgent Front

基于 Vue 3 + Vite + Vue Router + Axios 的 AI 应用前端，包含应用中心、AI 恋爱大师、AI 超级智能体三个页面。

## 运行

```bash
npm install
npm run dev
```

默认地址：http://localhost:5173

打包预览：

```bash
npm run build
npm run preview
```

## 接口对接

后端接口前缀：`http://localhost:8123/api`

| 页面 | 接口 | 方式 |
| --- | --- | --- |
| AI 恋爱大师 | `/ai/love_app/chat/sse?message=&chatId=` | GET + SSE 流式 |
| AI 超级智能体 | `/ai/manus/chat?message=` | GET + SSE 流式 |

开发环境通过 Vite 代理转发，`.env` 中配置：

```
VITE_API_BASE_URL=/api
VITE_PROXY_TARGET=http://localhost:8123
```

即浏览器请求 `/api/ai/...`，由 Vite 转发到 `http://localhost:8123/api/ai/...`，因此本机开发不依赖后端 CORS 配置。

如果不想走代理，把 `VITE_API_BASE_URL` 改成 `http://localhost:8123/api` 即可，此时需要后端开启跨域支持。

## 目录说明

```
src/
├─ api/            # axios 实例与接口常量
├─ components/     # 聊天室、消息气泡、输入框
├─ composables/    # useChatRoom：消息列表 + 流式会话状态
├─ utils/          # sse.js 流式解析、id 生成
├─ views/          # HomeView / LoveAppView / ManusAppView
└─ router/         # 路由表
```

## 关于 SSE 的说明

两个后端接口都是 GET + `text/event-stream`。浏览器里 Axios 拿不到 `ReadableStream`，
因此 `src/utils/sse.js` 使用 Axios 的 `onDownloadProgress` 读取累计 `responseText`，
再按照 SSE 协议（`data:` / `event:` / 空行分块）做增量解析，做到边生成边显示，
同时支持随时中断请求。

如果后端返回的是 JSON 字符串片段（例如 `{"type":"text","content":"..."}`），
在 `useChatRoom` 的 `onMessage` 里加一次 `JSON.parse` 后取对应字段即可。

流式解析逻辑可以用下面的命令单独验证（不依赖后端）：

```bash
npm run test:sse
```

## 本地自检

后端没起的时候，可以用仓库里的假后端看效果（零依赖，按 SSE 协议分片输出）：

```bash
npm run mock    # 监听 http://localhost:8123
```

配合 `npm run dev`，再跑一次真实浏览器的端到端检查，会验证「边生成边显示」和消息左右分布：

```bash
npm run test:e2e
```

`test:e2e` 依赖本机已安装的 Chrome/Edge，通过 `E2E_BROWSER_PATH` 可以指定其它浏览器路径；
`E2E_BASE_URL`、`E2E_OUT_DIR` 分别用于覆盖前端地址和截图输出目录。
