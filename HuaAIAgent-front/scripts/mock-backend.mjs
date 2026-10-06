/**
 * 前端独立联调用的假后端（零依赖）：
 *   node scripts/mock-backend.mjs
 *
 * 监听 http://localhost:8123，模拟 SpringBoot 的 SSE 分片输出。
 */
import http from 'node:http'

const PORT = Number(process.env.MOCK_PORT || 8123)
const DELAY = Number(process.env.MOCK_DELAY || 90)

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

function writeSseHead(res) {
  res.writeHead(200, {
    'Content-Type': 'text/event-stream; charset=utf-8',
    'Cache-Control': 'no-cache, no-transform',
    Connection: 'keep-alive',
    'X-Accel-Buffering': 'no',
  })
}

async function streamText(res, pieces, delay = DELAY) {
  for (const piece of pieces) {
    // SSE 规范：data 里的换行要拆成多条 data: 行，Spring 的 SSE 编码器也是这么做的
    for (const line of String(piece).split(/\r\n|\r|\n/)) {
      res.write(`data: ${line}\n`)
    }
    res.write('\n')
    await sleep(delay)
  }
  res.end()
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host}`)

  if (url.pathname === '/api/ai/love_app/chat/sse') {
    const message = url.searchParams.get('message') || ''
    const chatId = url.searchParams.get('chatId') || ''

    writeSseHead(res)
    await streamText(res, [
      `收到你的问题（会话 ${chatId.slice(0, 8)}）。`,
      '\n\n先别急着表白，',
      '可以先从日常聊天开始，',
      '观察对方的回应节奏。',
      `\n\n你提到的是「${message}」，`,
      '试着约一次轻松的下午茶，',
      '把话题放在共同兴趣上。',
    ])
    return
  }

  if (url.pathname === '/api/ai/manus/chat') {
    const message = url.searchParams.get('message') || ''

    writeSseHead(res)
    await streamText(res, [
      '正在拆解任务……\n',
      '1. 明确目标\n',
      '2. 规划步骤\n',
      '3. 逐步执行\n\n',
      `任务「${message}」已处理完成。`,
    ])
    return
  }

  res.writeHead(404, { 'Content-Type': 'application/json; charset=utf-8' })
  res.end(JSON.stringify({ message: 'not found' }))
})

server.listen(PORT, () => {
  console.log(`mock backend listening on http://localhost:${PORT}`)
})
