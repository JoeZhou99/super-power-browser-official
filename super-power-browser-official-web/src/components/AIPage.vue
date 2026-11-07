<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import Icon from './Icon.vue'

const emit = defineEmits(['register', 'download', 'contact'])

// 店铺
const shops = [
  { id: 'S01', name: 'Amazon · 北美旗舰店', platform: 'Amazon', color: 'orange', letter: 'a', region: 'US · 美国' },
  { id: 'S02', name: 'TikTok Shop · 英国店', platform: 'TikTok', color: 'dark', letter: '♪', region: 'GB · 英国' },
  { id: 'S03', name: 'Shopee · 新加坡店', platform: 'Shopee', color: 'coral', letter: 'S', region: 'SG · 新加坡' },
  { id: 'S04', name: 'Lazada · 马来店', platform: 'Lazada', color: 'purple', letter: 'L', region: 'MY · 马来' },
]

// 结果数据
const resultRows = [
  { shop: 'Amazon · 北美', sku: 'B0-SK-1024', title: 'Wireless Earbuds Pro', price: '$29.99', sold: '1,284', stock: '642' },
  { shop: 'TikTok · 英国', sku: 'TT-UK-2088', title: 'Bluetooth Speaker Mini', price: '£21.99', sold: '872', stock: '318' },
  { shop: 'Shopee · 新加坡', sku: 'SP-SG-3315', title: 'Smart Watch Series', price: 'S$45.00', sold: '2,046', stock: '907' },
  { shop: 'Lazada · 马来', sku: 'LZ-MY-4096', title: 'Portable Charger 20K', price: 'RM 89.90', sold: '1,153', stock: '431' },
]

// 流程阶段（单屏场景切换）
// 场景序列：input -> plan -> open -> view -> collect -> result
const scene = ref('idle')
const sceneIndex = ref(0)

const scenes = [
  { key: 'input', label: '输入指令' },
  { key: 'plan', label: '生成计划' },
  { key: 'open', label: '打开店铺' },
  { key: 'view', label: '查看页面' },
  { key: 'collect', label: '采集数据' },
  { key: 'result', label: '结构化结果' },
]

// 输入框打字机效果
const typedText = ref('')
const fullPrompt = '帮我采集这 4 家店铺的今日销量、价格和库存，导出成 Excel 汇总表'
let typingTimer = null

// 计划步骤
const planSteps = [
  { label: '理解任务，选择店铺与技能', done: false, active: false },
  { label: '并行打开 4 家店铺浏览器', done: false, active: false },
  { label: '逐店采集数据并导出 Excel', done: false, active: false },
  { label: '汇总结果并回传', done: false, active: false },
]

// 店铺打开进度
const openedShops = ref(0)
const collectingShop = ref(0) // 正在采集的店铺索引
const collectedShops = ref(0)

// 当前演示店铺（查看页面/采集数据场景聚焦单店）
const activeShopIndex = ref(0)

// 查看页面的商品数据（逼真表格）
const productRows = [
  { id: 'B0-SK-1024', name: 'Wireless Earbuds Pro', price: '$29.99', sold: '1,284', stock: '642', status: '在售' },
  { id: 'B0-SK-1025', name: 'Bluetooth Speaker Mini', price: '$21.99', sold: '872', stock: '318', status: '在售' },
  { id: 'B0-SK-1026', name: 'Smart Watch Series 5', price: '$45.00', sold: '2,046', stock: '907', status: '在售' },
  { id: 'B0-SK-1027', name: 'Portable Charger 20K', price: '$19.99', sold: '1,153', stock: '431', status: '在售' },
  { id: 'B0-SK-1028', name: 'USB-C Hub 7-in-1', price: '$25.99', sold: '640', stock: '0', status: '缺货' },
]

// 页面扫描字段（逐列高亮扫描）
const pageFields = [
  { label: '商品标题', value: 'Wireless Earbuds Pro', done: false },
  { label: '今日销量', value: '1,284', done: false },
  { label: '当前价格', value: '$29.99', done: false },
  { label: '库存数量', value: '642', done: false },
]

// 侧边导航
const sidebarNav = [
  { label: '数据看板', active: false },
  { label: '商品管理', active: true },
  { label: '订单管理', active: false },
  { label: '库存管理', active: false },
  { label: '营销中心', active: false },
  { label: '评价管理', active: false },
]

// 结果
const resultReady = ref(false)

let timers = []

function clearAll() { timers.forEach(t => clearTimeout(t)); timers = [] }
function later(fn, ms) { timers.push(setTimeout(fn, ms)) }

function goToScene(key) {
  scene.value = key
  sceneIndex.value = scenes.findIndex(s => s.key === key)
}

function resetDemo() {
  clearAll()
  if (typingTimer) clearInterval(typingTimer)
  scene.value = 'idle'
  sceneIndex.value = 0
  typedText.value = ''
  openedShops.value = 0
  collectingShop.value = 0
  collectedShops.value = 0
  activeShopIndex.value = 0
  resultReady.value = false
  planSteps.forEach(p => { p.done = false; p.active = false })
  pageFields.forEach(f => { f.done = false })
}

function startDemo() {
  resetDemo()

  // 场景 1：输入指令（打字机）
  goToScene('input')
  let idx = 0
  typingTimer = setInterval(() => {
    idx += 1
    typedText.value = fullPrompt.slice(0, idx)
    if (idx >= fullPrompt.length) {
      clearInterval(typingTimer)
      // 发送 -> 生成计划
      later(() => {
        goToScene('plan')
        planSteps[0].active = true
        later(() => { planSteps[0].done = true; planSteps[0].active = false; planSteps[1].active = true }, 1000)
        later(() => { planSteps[1].done = true; planSteps[1].active = false; planSteps[2].active = true }, 2000)
        later(() => { planSteps[2].done = true; planSteps[2].active = false; planSteps[3].active = true }, 3000)
        later(() => { planSteps[3].done = true; planSteps[3].active = false }, 4000)
        // 规划完成 -> 打开店铺
        later(() => {
          goToScene('open')
          shops.forEach((_, i) => later(() => { openedShops.value = i + 1 }, i * 650))
          // 全部打开 -> 查看页面（聚焦第一家店）
          later(() => {
            activeShopIndex.value = 0
            goToScene('view')
            runViewCollect()
          }, shops.length * 650 + 900)
        }, 4600)
      }, 650)
    }
  }, 40)
}

// 查看页面 -> 采集数据 -> 输出结果
function runViewCollect() {
  // 查看页面：页面字段逐项被扫描高亮
  pageFields.forEach((f, i) => later(() => { f.done = true }, i * 700))

  // 进入采集场景
  later(() => { goToScene('collect'); collectingShop.value = 0; collectedShops.value = 0 }, pageFields.length * 700 + 900)

  // 逐店采集（每店一个短停留）
  const collectStart = pageFields.length * 700 + 900 + 500
  shops.forEach((_, i) => {
    later(() => { collectingShop.value = i; activeShopIndex.value = i }, collectStart + i * 900)
    later(() => { collectedShops.value = i + 1 }, collectStart + i * 900 + 700)
  })

  // 输出结构化结果
  later(() => {
    resultReady.value = true
    goToScene('result')
  }, collectStart + shops.length * 900 + 800)
}

const statusText = computed(() => {
  switch (scene.value) {
    case 'input': return '正在输入任务指令…'
    case 'plan': return 'AI 正在理解任务，生成执行计划…'
    case 'open': return '正在并行打开店铺浏览器…'
    case 'view': return '正在查看页面，定位数据字段…'
    case 'collect': return `正在多店铺采集…（${collectedShops.value}/4）`
    case 'result': return '采集完成，已输出结构化结果'
    default: return '准备就绪，等待输入任务…'
  }
})

// 滚动到演示区才开始播放；已播过则不再重复触发
let started = false
let observer = null
let demoEl = null

onMounted(() => {
  demoEl = document.querySelector('.ai-demo-section')
  if (!('IntersectionObserver' in window)) { startDemo(); return }

  observer = new IntersectionObserver((entries) => {
    if (entries[0].isIntersecting && !started) {
      started = true
      startDemo()
      observer.disconnect()
    }
  }, { threshold: 0.35 })

  observer.observe(demoEl)
})

onBeforeUnmount(() => {
  clearAll()
  if (typingTimer) clearInterval(typingTimer)
  if (observer) observer.disconnect()
})

// 静态产品介绍
const pains = [
  { quote: '我的运营团队，70% 的时间都在做重复操作', who: '某亚马逊卖家 · 运营总监' },
  { quote: '店铺一多，数据采集和上品就要熬夜做', who: '某 TikTok 卖家 · 创始人' },
  { quote: '知道 AI 能提效，又担心触发平台风控', who: '某 Shopee 卖家 · 运营经理' },
]
const capabilities = [
  {
    icon: 'layers',
    title: '一句话，多店铺并行采集',
    text: '不用再逐店登录、逐页复制。一句自然语言，AI 在多家店铺的独立环境里同时抓取数据、统一汇总成表。',
    points: ['跨店铺、跨站点一次采集', '自动汇总导出 Excel', '跨店对比不再手工复制'],
  },
  {
    icon: 'sparkles',
    title: '批量执行，像安排同事一样',
    text: '上品、提现、回复消息，把高频操作打包成一次指令，AI 按店铺并发推进，关键动作先确认。',
    points: ['批量上品 / 提现 / 自动回复', '按店铺并发、统一节奏', '关键动作执行前先确认'],
  },
  {
    icon: 'shield',
    title: '独立指纹，不触发风控',
    text: '每家店铺沿用独立的浏览器指纹、Cookie 与代理出口，AI 只在已登录环境内操作，天然防关联。',
    points: ['独立指纹 · 独立 Cookie · 独立代理', '只在已登录环境内操作', '从源头规避关联风控'],
  },
  {
    icon: 'clock',
    title: '稳定可追溯，随时可接管',
    text: '任务按队列调度、失败不静默重试，过程与结果全程留痕；遇到验证码时暂停，等你就绪再继续。',
    points: ['队列调度 · 失败自动重试', '每一步操作全程留痕', '可暂停 · 可接管 · 可审计'],
  },
]
const stats = [
  { num: '96%', label: '采集与汇总耗时下降', note: '竞品调研：4 小时 → 10 分钟' },
  { num: '8', label: '店铺并发上限（可配）', note: '一次指令同时跑 8 家店铺' },
  { num: '200', label: '单次批量处理条目', note: '上品 / 采集一次搞定' },
  { num: '100%', label: '操作留痕可追溯', note: '每一步都有记录可查' },
]
const scenarios = [
  {
    name: '批量采集数据',
    tag: '数据采集',
    prompt: '帮我采集 4 家店铺的今日销量、价格和库存，导出成 Excel 汇总表',
    steps: ['并行打开 4 家店铺浏览器', '逐店抓取销量 / 价格 / 库存', '统一汇总并校验', '一键导出 Excel'],
  },
  {
    name: '批量上品',
    tag: '店铺操作',
    prompt: '把这款新品，按各店铺的字段规范，同时上架到 4 家店铺',
    steps: ['读取新品信息与图片素材', '按各平台字段规范自动填充', '逐店提交上架', '回传上架结果与链接'],
  },
  {
    name: '批量提现',
    tag: '资金操作',
    prompt: '汇总 4 家店铺的可提现余额，金额核对无误后提交结算申请',
    steps: ['逐店核对可提现金额', '汇总回款并二次校验', '提交结算申请', '输出提现清单留痕'],
  },
  {
    name: '自动回复消息',
    tag: '消息处理',
    prompt: '把这个月所有 1-2 星差评整理出来，按问题分类并生成回复草稿',
    steps: ['抓取全店铺差评与咨询', 'AI 分类：品质 / 物流 / 描述', '按平台规范生成回复草稿', '关键回复发送前先确认'],
  },
]
</script>

<template>
  <div class="ai-page">
    <!-- Hero -->
    <section class="ai-hero">
      <div class="hero-orb one"></div>
      <div class="hero-orb two"></div>
      <div class="container ai-hero-inner">
        <span class="eyebrow"><Icon name="sparkles" :size="15" /> SUPERPOWER AI 工作台</span>
        <h1 id="page-title" tabindex="-1">多店铺运营，<br /><span>交给 AI 批量完成。</span></h1>
        <p class="hero-description">用一句自然语言下达任务，AI 在多个店铺的独立浏览器环境里并行采集数据、批量执行操作，<br class="desktop-break" />沿用每家店铺的独立指纹与代理，稳定高效、不触发平台风控。</p>
        <div class="hero-actions">
          <button class="button primary large" @click="emit('register')">立即体验<Icon name="arrow" :size="18" /></button>
          <button class="button secondary large" @click="emit('contact')"><Icon name="message" :size="19" />预约产品演示</button>
        </div>
        <div class="hero-notes">
          <span><Icon name="check" :size="14" /> 多店铺批量采集</span>
          <span><Icon name="check" :size="14" /> 批量上品 / 提现 / 回复</span>
          <span><Icon name="check" :size="14" /> 独立指纹防关联</span>
        </div>
      </div>
    </section>

    <!-- 核心演示：单屏场景切换 -->
    <section class="ai-demo-section section">
      <div class="container">
        <div class="section-heading">
          <span class="eyebrow">WATCH THE FULL FLOW</span>
          <h2>从一句指令开始，看 AI 走完全程</h2>
          <p>输入任务 → 生成计划 → 打开店铺 → 查看页面 → 采集数据 → 结构化输出。</p>
        </div>

        <!-- 大屏舞台 -->
        <div class="scene-window">
          <!-- 顶部标题栏 -->
          <div class="scene-topbar">
            <span class="win-dots"><i></i><i></i><i></i></span>
            <span class="agent-title"><Icon name="sparkles" :size="16" /> SuperPower AI 工作台</span>
            <span class="agent-fp"><Icon name="shield" :size="13" /> 指纹隔离 · 已启用</span>
          </div>

          <!-- 场景步骤指示条 -->
          <div class="scene-stepper">
            <div v-for="(s, i) in scenes" :key="s.key" class="scene-step" :class="{ done: i < sceneIndex, active: i === sceneIndex }">
              <span class="scene-dot"><Icon v-if="i < sceneIndex" name="check" :size="11" /></span>
              <span class="scene-label">{{ s.label }}</span>
            </div>
          </div>

          <!-- 场景舞台（单屏切换） -->
          <div class="scene-stage">
            <Transition name="scene" mode="out-in">
              <!-- 场景 1：输入指令 -->
              <div v-if="scene === 'input'" key="input" class="scene-pane scene-input">
                <div class="input-stage">
                  <div class="input-hint"><Icon name="sparkles" :size="15" /> 告诉 AI 你要做什么</div>
                  <div class="input-shop-chips">
                    <span v-for="shop in shops" :key="shop.id" class="shop-chip lg"><span class="platform-dot" :class="shop.color"></span>{{ shop.name }}</span>
                  </div>
                  <div class="input-box">
                    <span class="input-caret"><Icon name="sparkles" :size="18" /></span>
                    <span class="input-text">{{ typedText }}<span v-if="scene === 'input'" class="caret"></span></span>
                    <span class="input-send"><Icon name="arrow" :size="16" /></span>
                  </div>
                  <div class="input-foot"><Icon name="shield" :size="12" /> 仅授权店铺可操作 · 独立指纹环境内执行</div>
                </div>
              </div>

              <!-- 场景 2：生成计划 -->
              <div v-else-if="scene === 'plan'" key="plan" class="scene-pane scene-plan">
                <div class="plan-stage">
                  <div class="plan-card">
                    <div class="plan-card-head"><Icon name="sparkles" :size="16" /> AI 已生成执行计划</div>
                    <div class="plan-list lg">
                      <div v-for="(p, i) in planSteps" :key="p.label" class="plan-item" :class="{ done: p.done, active: p.active }">
                        <span class="plan-dot"><Icon v-if="p.done" name="check" :size="12" /></span>
                        <span class="plan-label">{{ p.label }}</span>
                        <span v-if="p.active" class="plan-running">执行中</span>
                        <span v-else-if="p.done" class="plan-done">完成</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 场景 3：打开店铺 -->
              <div v-else-if="scene === 'open'" key="open" class="scene-pane scene-open">
                <div class="open-stage">
                  <div class="open-grid">
                    <div v-for="(shop, i) in shops" :key="shop.id" class="open-card" :class="{ in: openedShops > i }">
                      <template v-if="openedShops > i">
                        <span class="platform-dot" :class="shop.color"></span>
                        <div class="open-card-name">{{ shop.name }}</div>
                        <div class="open-status"><Icon name="check" :size="12" /> 已打开</div>
                        <div class="open-meta">指纹已初始化 · 代理已连接 · 会话已登录</div>
                      </template>
                      <template v-else>
                        <span class="platform-dot dim" :class="shop.color"></span>
                        <div class="open-card-name dim">{{ shop.name }}</div>
                        <div class="open-wait"><span class="mini-spin"></span> 等待启动</div>
                      </template>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 场景 4：查看页面（真实浏览器页面） -->
              <div v-else-if="scene === 'view'" key="view" class="scene-pane scene-view">
                <div class="browser-page">
                  <!-- 浏览器地址栏 -->
                  <div class="browser-chrome">
                    <span class="win-dots"><i></i><i></i><i></i></span>
                    <span class="browser-tab"><span class="platform-dot" :class="shops[activeShopIndex].color"></span>{{ shops[activeShopIndex].platform }} 卖家中心</span>
                    <div class="browser-url"><Icon name="lock" :size="11" /> seller.{{ shops[activeShopIndex].platform.toLowerCase() }}.com/listing/inventory</div>
                    <span class="browser-actions"><Icon name="eye" :size="13" /><Icon name="settings" :size="13" /></span>
                  </div>
                  <!-- 页面主体：侧边栏 + 内容 -->
                  <div class="seller-layout">
                    <!-- 侧边栏 -->
                    <aside class="seller-side">
                      <div class="seller-logo"><span class="platform-dot" :class="shops[activeShopIndex].color"></span> {{ shops[activeShopIndex].platform }}<small>Seller Central</small></div>
                      <nav class="seller-nav">
                        <a v-for="nav in sidebarNav" :key="nav.label" :class="{ active: nav.active }"><Icon name="grid" :size="14" /> {{ nav.label }}</a>
                      </nav>
                      <div class="seller-side-foot"><Icon name="shield" :size="12" /> 独立指纹环境</div>
                    </aside>
                    <!-- 内容区 -->
                    <div class="seller-main">
                      <!-- 顶部工具栏 -->
                      <div class="seller-toolbar">
                        <h4>商品管理</h4>
                        <div class="toolbar-right">
                          <span class="toolbar-fp"><Icon name="shield" :size="11" /> 指纹隔离</span>
                          <button class="toolbar-btn primary"><Icon name="sparkles" :size="13" /> 正在采集…</button>
                        </div>
                      </div>
                      <!-- KPI 数据卡 -->
                      <div class="kpi-row">
                        <div class="kpi-card" :class="{ scanned: pageFields[1].done }">
                          <span class="kpi-label">今日销量</span>
                          <span class="kpi-value">{{ pageFields[1].done ? pageFields[1].value : '—' }}</span>
                          <span v-if="pageFields[1].done" class="kpi-tag up">▲ 12.4%</span>
                        </div>
                        <div class="kpi-card" :class="{ scanned: pageFields[2].done }">
                          <span class="kpi-label">当前价格</span>
                          <span class="kpi-value">{{ pageFields[2].done ? pageFields[2].value : '—' }}</span>
                        </div>
                        <div class="kpi-card" :class="{ scanned: pageFields[3].done }">
                          <span class="kpi-label">库存数量</span>
                          <span class="kpi-value">{{ pageFields[3].done ? pageFields[3].value : '—' }}</span>
                        </div>
                        <div class="kpi-card">
                          <span class="kpi-label">待处理订单</span>
                          <span class="kpi-value">37</span>
                        </div>
                      </div>
                      <!-- 商品表格 -->
                      <div class="seller-table-wrap">
                        <div class="table-caption">
                          <span>全部商品 <em>{{ productRows.length }}</em></span>
                          <span class="table-search"><Icon name="search" :size="12" /> 搜索商品 / SKU</span>
                        </div>
                        <table class="seller-table">
                          <thead>
                            <tr>
                              <th>商品</th>
                              <th>SKU</th>
                              <th>价格</th>
                              <th>销量</th>
                              <th>库存</th>
                              <th>状态</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(p, i) in productRows" :key="p.id" :class="{ focus: i === 0, scanned: i === 0 && pageFields[0].done }">
                              <td class="prod-cell">
                                <span class="prod-thumb">{{ p.name.slice(0, 1) }}</span>
                                <span class="prod-name">{{ p.name }}</span>
                              </td>
                              <td class="sku">{{ p.id }}</td>
                              <td :class="{ 'hl': i === 0 }">{{ p.price }}</td>
                              <td :class="{ 'hl': i === 0 && pageFields[1].done }">{{ i === 0 && pageFields[1].done ? pageFields[1].value : p.sold }}</td>
                              <td :class="{ 'hl': i === 0 && pageFields[3].done }">{{ i === 0 && pageFields[3].done ? pageFields[3].value : p.stock }}</td>
                              <td><span class="status-pill" :class="{ out: p.status === '缺货' }">{{ p.status }}</span></td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                      <!-- 扫描状态提示 -->
                      <div class="scan-notice">
                        <span v-if="pageFields.every(f => f.done)"><Icon name="check" :size="13" /> 页面数据已读取完成</span>
                        <span v-else><span class="mini-spin"></span> AI 正在读取页面字段…</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- 场景 5：采集数据 -->
              <div v-else-if="scene === 'collect'" key="collect" class="scene-pane scene-collect">
                <div class="collect-stage">
                  <div class="collect-shops">
                    <div v-for="(shop, i) in shops" :key="shop.id" class="collect-shop" :class="{ current: collectingShop === i && collectedShops <= i, done: collectedShops > i }">
                      <span class="platform-dot" :class="shop.color"></span>
                      <div class="collect-shop-name">{{ shop.name }}</div>
                      <div class="collect-shop-data">
                        <span>销量 <b>{{ collectedShops > i ? resultRows[i].sold : '…' }}</b></span>
                        <span>价格 <b>{{ collectedShops > i ? resultRows[i].price : '…' }}</b></span>
                        <span>库存 <b>{{ collectedShops > i ? resultRows[i].stock : '…' }}</b></span>
                      </div>
                      <div class="collect-shop-state">
                        <Icon v-if="collectedShops > i" name="check" :size="12" /><span>{{ collectedShops > i ? '已采集' : '采集中' }}</span>
                        <span v-if="collectedShops <= i && collectingShop === i" class="mini-spin"></span>
                      </div>
                    </div>
                  </div>
                  <div class="collect-progress"><span class="collect-bar" :style="{ width: (collectedShops / shops.length * 100) + '%' }"></span></div>
                </div>
              </div>

              <!-- 场景 6：结构化结果 -->
              <div v-else-if="scene === 'result'" key="result" class="scene-pane scene-result">
                <div class="result-stage">
                  <div class="result-card">
                    <div class="result-card-head">
                      <span class="result-check"><Icon name="check" :size="18" /></span>
                      <div>
                        <h4>采集完成，已输出结构化结果</h4>
                        <p>4 家店铺数据已汇总并校验，可直接导出 Excel。</p>
                      </div>
                      <button class="export-mini lg"><Icon name="download" :size="14" /> 导出 Excel</button>
                    </div>
                    <table class="result-table lg">
                      <thead><tr><th>店铺</th><th>SKU</th><th>商品</th><th>销量</th><th>价格</th><th>库存</th></tr></thead>
                      <tbody>
                        <tr v-for="(row, i) in resultRows" :key="row.sku" class="filled">
                          <td class="r-shop"><span class="platform-dot" :class="shops[i].color"></span>{{ row.shop }}</td>
                          <td class="sku">{{ row.sku }}</td>
                          <td class="title-cell">{{ row.title }}</td>
                          <td>{{ row.sold }}</td>
                          <td>{{ row.price }}</td>
                          <td>{{ row.stock }}</td>
                        </tr>
                      </tbody>
                    </table>
                    <div class="result-foot"><Icon name="shield" :size="12" /> 每家店铺沿用独立指纹 · 独立 Cookie · 独立代理 · 全程留痕</div>
                  </div>
                </div>
              </div>
            </Transition>
          </div>

          <!-- 底部状态栏 -->
          <div class="scene-status">
            <span class="stage-dot" :class="{ running: scene !== 'result' && scene !== 'idle', done: scene === 'result' }"></span>
            <span class="scene-status-text">{{ statusText }}</span>
            <button class="replay-btn" @click="startDemo"><Icon name="clock" :size="13" /> 重播演示</button>
          </div>
        </div>
      </div>
    </section>

    <!-- 能力 -->
    <section class="ai-cap-section section">
      <div class="container">
        <div class="section-heading">
          <span class="eyebrow">CORE CAPABILITIES</span>
          <h2>像安排同事一样，安排你的 AI</h2>
          <p>不用学复杂菜单，一句业务语言，AI 自动拆解、并行执行，过程全程可追溯。</p>
        </div>
        <div class="cap-grid">
          <article v-for="cap in capabilities" :key="cap.title" class="cap-card">
            <span class="card-icon"><Icon :name="cap.icon" :size="24" /></span>
            <h3>{{ cap.title }}</h3>
            <p>{{ cap.text }}</p>
            <ul class="cap-points">
              <li v-for="pt in cap.points" :key="pt"><Icon name="check" :size="13" /> {{ pt }}</li>
            </ul>
          </article>
        </div>
      </div>
    </section>

    <!-- 数据 -->
    <section class="ai-stats-section section">
      <div class="container">
        <div class="stats-grid">
          <div v-for="s in stats" :key="s.label" class="stat-item"><b>{{ s.num }}</b><span>{{ s.label }}</span><small>{{ s.note }}</small></div>
        </div>
      </div>
    </section>

    <!-- 场景 -->
    <section class="ai-scenario-section section">
      <div class="container">
        <div class="section-heading">
          <span class="eyebrow">HIGH-FREQUENCY SCENARIOS</span>
          <h2>说一句话，完成一个复杂任务</h2>
          <p>下面是高频运营场景的真实指令，AI 会自动拆解成完整执行流程。</p>
        </div>
        <div class="scenario-grid">
          <article v-for="sc in scenarios" :key="sc.name" class="scenario-card">
            <span class="scenario-tag">{{ sc.tag }}</span>
            <h3>{{ sc.name }}</h3>
            <div class="scenario-prompt"><Icon name="message" :size="13" /> “{{ sc.prompt }}”</div>
            <ul class="scenario-steps">
              <li v-for="(st, i) in sc.steps" :key="st"><span class="step-num">{{ i + 1 }}</span>{{ st }}</li>
            </ul>
          </article>
        </div>
      </div>
    </section>

    <!-- 痛点 -->
    <section class="ai-pain-section section">
      <div class="container">
        <div class="section-heading">
          <span class="eyebrow">DO YOU FEEL THE SAME</span>
          <h2>跨境卖家的三大痛苦</h2>
          <p>多店铺运营者每天都要面对的重复劳动。</p>
        </div>
        <div class="pain-grid">
          <blockquote v-for="p in pains" :key="p.who" class="pain-quote">
            <p>“{{ p.quote }}”</p>
            <footer>— {{ p.who }}</footer>
          </blockquote>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="ai-cta">
      <div class="container ai-cta-inner">
        <h2>把重复劳动，还给机器。</h2>
        <p>从第一个多店铺批量任务开始，体验采集、执行、留痕的完整流程。</p>
        <div class="hero-actions">
          <button class="button primary large" @click="emit('register')">立即体验<Icon name="arrow" :size="18" /></button>
          <button class="button secondary large" @click="emit('contact')"><Icon name="message" :size="19" />预约产品演示</button>
        </div>
      </div>
    </section>
  </div>
</template>
