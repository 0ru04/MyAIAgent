/**
 * SSE 增量解析冒烟测试（不依赖浏览器和后端）：
 *   node scripts/sse-smoke.mjs
 *
 * 用假的 axios client 模拟流式分片，验证跨分片的 data:/event:/空行解析。
 */
import assert from 'node:assert/strict'
import { streamSse } from '../src/utils/sse.js'

function createFakeClient(chunks, { fail = null } = {}) {
  return {
    get(_url, config) {
      return (async () => {
        let accumulated = ''
        for (const chunk of chunks) {
          accumulated += chunk
          config.onDownloadProgress?.({ event: { target: { responseText: accumulated } } })
          await new Promise((resolve) => setTimeout(resolve, 0))
        }
        if (fail) throw fail
        return { status: 200 }
      })()
    },
  }
}

function collect(client) {
  return new Promise((resolve, reject) => {
    const received = []
    const timer = setTimeout(() => reject(new Error('timeout')), 3000)
    streamSse({
      client,
      url: '/ai/mock',
      onMessage: (message) => received.push(message),
      onEnd: () => {
        clearTimeout(timer)
        resolve(received)
      },
      onError: (error) => {
        clearTimeout(timer)
        reject(error)
      },
    })
  })
}

// 1. data 片段跨 chunk 拼接
let received = await collect(createFakeClient(['data: 你', '好\n\n', 'data: 世界\n\n']))
assert.deepEqual(
  received.map((item) => item.data),
  ['你好', '世界'],
)

// 2. CRLF 分隔符被切分在 chunk 边界上
received = await collect(createFakeClient(['data: a\r', '\n\r\ndata: b\r\n\r\n']))
assert.deepEqual(
  received.map((item) => item.data),
  ['a', 'b'],
)

// 3. 多行 data 合并为换行，event 名称保留
received = await collect(createFakeClient(['event: message\ndata: line1\ndata: line2\n\n']))
assert.equal(received[0].data, 'line1\nline2')
assert.equal(received[0].event, 'message')

// 4. 注释行忽略，结尾缺空行时也要把最后一块吐出
received = await collect(createFakeClient([': keep-alive\n\ndata: tail']))
assert.deepEqual(
  received.map((item) => item.data),
  ['tail'],
)

// 5. 流中报错要把错误抛给调用方
await assert.rejects(
  collect(createFakeClient(['data: x\n\n'], { fail: new Error('boom') })),
  /boom/,
)

// 6. 主动中断不应触发 onEnd / onError
const abortClient = {
  get(_url, config) {
    return new Promise((_resolve, reject) => {
      config.signal.addEventListener('abort', () => {
        const error = new Error('canceled')
        error.code = 'ERR_CANCELED'
        reject(error)
      })
    })
  },
}

const abortable = streamSse({
  client: abortClient,
  url: '/ai/mock',
  onMessage: () => assert.fail('aborted stream should not emit messages'),
  onEnd: () => assert.fail('aborted stream should not call onEnd'),
  onError: () => assert.fail('aborted stream should not call onError'),
})
abortable.abort()
await abortable.request

console.log('sse smoke: 6 checks passed')
