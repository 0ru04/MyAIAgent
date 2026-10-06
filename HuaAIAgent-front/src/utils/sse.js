import axios from 'axios'

const SEPARATORS = ['\r\n\r\n', '\n\n', '\r\r']

function findSeparator(buffer) {
  let index = -1
  let length = 0

  for (const token of SEPARATORS) {
    const at = buffer.indexOf(token)
    if (at === -1) continue
    if (index === -1 || at < index) {
      index = at
      length = token.length
    }
  }

  return { index, length }
}

function parseBlock(block) {
  let event = 'message'
  const dataLines = []

  for (const line of block.split(/\r\n|\r|\n/)) {
    if (!line || line.startsWith(':')) continue

    const colon = line.indexOf(':')
    const field = colon === -1 ? line : line.slice(0, colon)
    let value = colon === -1 ? '' : line.slice(colon + 1)
    if (value.startsWith(' ')) value = value.slice(1)

    if (field === 'event') event = value
    else if (field === 'data') dataLines.push(value)
  }

  if (!dataLines.length) return null
  return { event, data: dataLines.join('\n') }
}

/**
 * 用 Axios(XHR) 消费 SSE 流。
 *
 * 浏览器里的 SSE 是长连接分片响应，Axios 无法像 fetch 那样拿到 ReadableStream，
 * 但可以通过 onDownloadProgress 读到累计的 responseText，这里按 SSE 协议做增量解析，
 * 从而做到「边生成边显示」，而不是等整段响应结束。
 *
 * @returns {{ abort: () => void, request: Promise<void> }}
 */
export function streamSse({
  client = axios,
  url,
  params,
  headers,
  onMessage,
  onEnd,
  onError,
  signal,
}) {
  const controller = new AbortController()
  let settled = false
  let consumed = 0
  let buffer = ''

  const drain = (flushAll = false) => {
    let separator = findSeparator(buffer)

    while (separator.index !== -1) {
      const block = buffer.slice(0, separator.index)
      buffer = buffer.slice(separator.index + separator.length)
      const payload = parseBlock(block)
      if (payload) onMessage?.(payload)
      separator = findSeparator(buffer)
    }

    if (flushAll && buffer.trim()) {
      const payload = parseBlock(buffer)
      buffer = ''
      if (payload) onMessage?.(payload)
    }
  }

  const finish = (error) => {
    if (settled) return
    settled = true
    drain(true)
    if (error) onError?.(error)
    else onEnd?.()
  }

  const abort = () => {
    if (settled) return
    settled = true
    controller.abort()
  }

  const request = client
    .get(url, {
      params,
      headers: { Accept: 'text/event-stream', ...headers },
      responseType: 'text',
      timeout: 0,
      signal: controller.signal,
      onDownloadProgress: (event) => {
        const raw = event?.event?.target?.responseText
        if (typeof raw !== 'string' || raw.length <= consumed) return
        buffer += raw.slice(consumed)
        consumed = raw.length
        drain()
      },
    })
    .then(() => finish())
    .catch((error) => {
      if (settled) return
      if (axios.isCancel?.(error) || error?.code === 'ERR_CANCELED' || error?.name === 'CanceledError') {
        finish()
        return
      }
      finish(error)
    })

  if (signal) {
    if (signal.aborted) abort()
    else signal.addEventListener('abort', abort, { once: true })
  }

  return { abort, request }
}
