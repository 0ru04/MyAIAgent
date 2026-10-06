/**
 * 真实浏览器端到端检查：验证 SSE 是否边生成边显示、气泡左右布局是否正确。
 *
 * 前置条件：
 *   1. npm run dev        （http://localhost:5173）
 *   2. npm run mock       （http://localhost:8123 假后端）
 *
 * 运行：
 *   node scripts/e2e-check.mjs
 */
import assert from 'node:assert/strict'
import path from 'node:path'
import { existsSync } from 'node:fs'
import { chromium } from 'playwright-core'

const BROWSER_CANDIDATES = [
  process.env.E2E_BROWSER_PATH,
  'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
  'C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe',
  '/usr/bin/microsoft-edge',
  '/usr/bin/google-chrome',
].filter(Boolean)

const BASE_URL = process.env.E2E_BASE_URL || 'http://localhost:5173'
const OUT_DIR = process.env.E2E_OUT_DIR || process.cwd()
const HEADLESS = process.env.E2E_HEADLESS !== '0'

function resolveBrowserPath() {
  const found = BROWSER_CANDIDATES.find((candidate) => existsSync(candidate))
  if (!found) {
    throw new Error(`找不到可用浏览器，请设置 E2E_BROWSER_PATH，已尝试：${BROWSER_CANDIDATES.join(', ')}`)
  }
  return found
}

/** 发送一条消息，并记录 AI 气泡文本长度随时间的变化，用来证明是流式渲染。 */
async function sendAndTrackGrowth(page, question) {
  await page.fill('textarea', question)
  await page.click('button[type="submit"]')

  const lengths = new Set()
  const deadline = Date.now() + 20000

  while (Date.now() < deadline) {
    const text = await page
      .locator('.msg--ai .msg__bubble')
      .last()
      .innerText()
      .catch(() => '')
    if (text) lengths.add(text.length)

    const done = await page.locator('.msg--ai .msg__bubble.is-done').count()
    if (done) break

    await page.waitForTimeout(50)
  }

  return [...lengths].sort((a, b) => a - b)
}

const browser = await chromium.launch({ executablePath: resolveBrowserPath(), headless: HEADLESS })
const page = await browser.newPage({ viewport: { width: 1280, height: 860 } })

const consoleErrors = []
page.on('console', (message) => {
  if (message.type() === 'error') consoleErrors.push(message.text())
})

const badResponses = []
page.on('response', (response) => {
  if (response.status() >= 400) {
    badResponses.push(`${response.status()} ${response.url()}`)
  }
})

// 1. 恋爱大师：自动生成会话 ID + 流式输出 + 气泡左右分布
await page.goto(`${BASE_URL}/love`, { waitUntil: 'domcontentloaded' })
await page.waitForSelector('textarea')

const subtitle = await page.locator('.chat__subtitle').innerText()
assert.match(subtitle, /会话 [0-9a-f]{8}/i, `应自动生成会话 ID，实际：${subtitle}`)

const loveLengths = await sendAndTrackGrowth(page, '我喜欢一个同事，怎么开口？')
assert.ok(
  loveLengths.length >= 3,
  `AI 恋爱大师应分多次增量渲染，实际观测到 ${loveLengths.length} 个长度：${loveLengths}`,
)

const loveReply = await page.locator('.msg--ai .msg__bubble').first().innerText()
for (const expected of ['先别急着表白', '你提到的是']) {
  assert.ok(loveReply.includes(expected), `AI 回复应包含「${expected}」，实际：${loveReply}`)
}

const userBubble = await page.locator('.msg--user .msg__bubble').first().boundingBox()
const aiBubble = await page.locator('.msg--ai .msg__bubble').first().boundingBox()
assert.ok(userBubble.x > aiBubble.x + 100, '用户消息应靠右，AI 消息应靠左')

await page.screenshot({ path: path.join(OUT_DIR, 'e2e-love.png') })

// 2. 超级智能体：同样需要流式输出
await page.goto(`${BASE_URL}/manus`, { waitUntil: 'domcontentloaded' })
await page.waitForSelector('textarea')

const manusLengths = await sendAndTrackGrowth(page, '帮我安排一次三人周末出行')
assert.ok(
  manusLengths.length >= 3,
  `超级智能体应分多次增量渲染，实际观测到 ${manusLengths.length} 个长度：${manusLengths}`,
)

const manusReply = await page.locator('.msg--ai .msg__bubble').first().innerText()
for (const expected of ['正在拆解任务', '已处理完成']) {
  assert.ok(manusReply.includes(expected), `智能体回复应包含「${expected}」，实际：${manusReply}`)
}

await page.screenshot({ path: path.join(OUT_DIR, 'e2e-manus.png') })

await browser.close()

console.log('e2e: 恋爱大师增量长度 =', loveLengths.join(' -> '))
console.log('e2e: 超级智能体增量长度 =', manusLengths.join(' -> '))

const fatalResponses = badResponses.filter((entry) => !/favicon/i.test(entry))
assert.deepEqual(fatalResponses, [], `出现异常请求：${fatalResponses.join(' | ')}`)

const fatalErrors = consoleErrors.filter((text) => !/favicon|404/i.test(text))
assert.deepEqual(fatalErrors, [], `浏览器控制台出现错误：${fatalErrors.join(' | ')}`)

console.log('e2e: 全部检查通过')
