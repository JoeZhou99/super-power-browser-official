<script setup>
import { ref, watch, nextTick, onBeforeUnmount, computed } from 'vue'
import Icon from './Icon.vue'
const props = defineProps({ kind: String, config: Object })
const emit = defineEmits(['close', 'open', 'copy'])
const dialog = ref(null)
const username = ref(''), password = ref(''), confirmPassword = ref(''), agreed = ref(false)
const visiblePassword = ref(false), error = ref(''), busy = ref(false), success = ref(false)
const platform = ref('windows'), notice = ref('')
const legalBack = ref(false)
let previousFocus, abort
const title = computed(() => ({ register: '开启你的独立工作空间', download: '把独立空间，带到桌面', contact: '聊聊你的业务需求', terms: '使用说明', privacy: '隐私说明' }[props.kind]))
const downloadUrl = computed(() => {
  try { const url = new URL(props.config.windows?.url || '', location.origin); return props.config.windows?.url && (url.protocol === 'https:' || url.origin === location.origin) ? url.href : '' } catch { return '' }
})
function close() { if (legalBack.value && ['terms', 'privacy'].includes(props.kind)) { legalBack.value = false; emit('open', 'register') } else emit('close') }
function openLegal(kind) { legalBack.value = true; emit('open', kind) }
function keyboard(event) {
  if (event.key === 'Escape') { event.preventDefault(); close(); return }
  if (event.key !== 'Tab') return
  const elements = [...(dialog.value?.querySelectorAll('button:not([disabled]), a[href], input:not([disabled]), [tabindex="0"]') || [])].filter(el => el.getClientRects().length)
  const first = elements[0], last = elements.at(-1)
  if (!first) { event.preventDefault(); return }
  if (event.shiftKey && (document.activeElement === first || document.activeElement === dialog.value)) { event.preventDefault(); last.focus() }
  else if (!event.shiftKey && (document.activeElement === last || !dialog.value.contains(document.activeElement))) { event.preventDefault(); first.focus() }
}
watch(() => props.kind, async (kind, old) => {
  if (kind) {
    if (!old) { previousFocus = document.activeElement; document.body.style.overflow = 'hidden'; document.getElementById('app').inert = true; document.addEventListener('keydown', keyboard) }
    await nextTick(); dialog.value?.focus()
  } else {
    abort?.abort(); abort = null; busy.value = false; password.value = ''; confirmPassword.value = ''; error.value = ''; success.value = false; legalBack.value = false
    document.body.style.overflow = ''; document.getElementById('app').inert = false; document.removeEventListener('keydown', keyboard); previousFocus?.focus()
  }
})
onBeforeUnmount(() => { abort?.abort(); document.body.style.overflow = ''; document.getElementById('app').inert = false; document.removeEventListener('keydown', keyboard) })
async function register() {
  if (busy.value) return
  error.value = ''
  if (!/^[a-zA-Z0-9][a-zA-Z0-9_.-]{3,39}$/.test(username.value.trim())) { error.value = '账号需为 4–40 位字母、数字、下划线、点或短横线，并以字母或数字开头。'; return }
  if (password.value.length < 12 || new TextEncoder().encode(password.value).length > 72) { error.value = '密码至少 12 位，且 UTF-8 编码不超过 72 字节。'; return }
  if (password.value !== confirmPassword.value) { error.value = '两次输入的密码不一致。'; return }
  if (!agreed.value) { error.value = '请先阅读并确认使用说明和隐私说明。'; return }
  if (!props.config.registrationEndpoint) { error.value = '公开注册即将开放，请添加微信 15675826196 申请体验。你的账号和密码未提交或保存。'; return }
  busy.value = true; const operation = new AbortController(); abort = operation
  const timer = setTimeout(() => operation.abort(), 15000)
  try {
    const endpoint = new URL(props.config.registrationEndpoint, location.origin)
    if (endpoint.origin !== location.origin) throw new Error('注册服务配置异常，请联系支持人员。')
    const response = await fetch(endpoint, { method: 'POST', headers: { 'Content-Type': 'application/json' }, credentials: 'omit', signal: operation.signal, body: JSON.stringify({ username: username.value.trim(), password: password.value, acceptedTerms: true, policyVersion: '2026-11-05' }) })
    const data = await response.json().catch(() => null)
    if (abort !== operation) return
    if (!response.ok) throw new Error(data?.message || (response.status === 429 ? '操作较频繁，请稍后再试。' : '注册服务暂不可用，请联系微信客服。'))
    if (!data?.id || data?.username !== username.value.trim()) throw new Error('注册服务未返回有效结果，请联系支持人员确认。')
    success.value = true; password.value = ''; confirmPassword.value = ''
  } catch (e) { if (abort === operation) error.value = e.name === 'AbortError' ? '请求未完成，请稍后重试；若已创建账号，可直接尝试登录客户端。' : e.message }
  finally { clearTimeout(timer); if (abort === operation) busy.value = false }
}
</script>
<template>
  <Teleport to="body"><Transition name="modal"><div v-if="kind" class="modal-backdrop" @click.self="close"><section ref="dialog" class="dialog" :class="[`dialog-${kind}`, { 'is-success': success }]" role="dialog" aria-modal="true" aria-labelledby="dialog-title" tabindex="-1">
    <button class="icon-button dialog-close" aria-label="关闭弹窗" @click="close"><Icon name="close" /></button>
    <template v-if="kind === 'register'">
      <aside class="register-art"><img src="/brand.svg" alt="" /><span class="eyebrow">YOUR SPACE. YOUR POWER.</span><h2>独立每一个环境<br />连接每一份可能</h2><div class="register-orbit"><div class="orbit-ring"></div><div class="orbit-ring second"></div><span class="orbit-center"><Icon name="layers" :size="48" /></span><span class="orbit-item one"><Icon name="shield" :size="26" /></span><span class="orbit-item two"><Icon name="globe" :size="26" /></span><span class="orbit-item three"><Icon name="users" :size="26" /></span></div><p><Icon name="check" :size="15" /> 独立环境 <Icon name="check" :size="15" /> 团队协作</p></aside>
      <div class="register-form"><span class="eyebrow">WELCOME TO SUPERPOWER</span><h2 id="dialog-title">{{ success ? '账号创建成功' : title }}</h2><p class="dialog-intro">{{ success ? '使用刚创建的账号，登录同一服务下的客户端。' : '从一个账号开始，让日常运营更有条理。' }}</p>
        <div v-if="success" class="success-panel"><span class="success-icon"><Icon name="check" :size="32" /></span><h3>{{ username }}</h3><p>你的独立工作空间已创建。</p><button class="button primary full" @click="emit('open', 'download')">前往下载中心<Icon name="arrow" :size="17" /></button></div>
        <form v-else @submit.prevent="register">
          <div v-if="!config.registrationEndpoint" class="availability-note"><span class="status-dot"></span> 公开注册即将开放，可先联系微信申请体验</div>
          <label class="field">账号<input v-model="username" autocomplete="username" placeholder="4–40 位字母或数字组合" maxlength="40" required :disabled="busy" /></label>
          <label class="field">密码<span class="password-field"><input v-model="password" :type="visiblePassword ? 'text' : 'password'" autocomplete="new-password" placeholder="至少 12 位，建议混合字母与数字" maxlength="72" required :disabled="busy" /><button type="button" class="icon-button" :aria-label="visiblePassword ? '隐藏密码' : '显示密码'" :aria-pressed="visiblePassword" @click="visiblePassword = !visiblePassword"><Icon name="eye" :size="18" /></button></span></label>
          <label class="field">确认密码<input v-model="confirmPassword" :type="visiblePassword ? 'text' : 'password'" autocomplete="new-password" placeholder="再次输入密码" maxlength="72" required :disabled="busy" /></label>
          <div class="consent"><input id="consent" v-model="agreed" type="checkbox" :disabled="busy" /><label for="consent">我已阅读并同意</label><button type="button" @click="openLegal('terms')">使用说明</button><span>和</span><button type="button" @click="openLegal('privacy')">隐私说明</button></div>
          <p v-if="error" class="form-error" role="alert">{{ error }}</p><button class="button primary full" type="submit" :disabled="busy">{{ busy ? '正在创建账号…' : '立即注册' }}<Icon v-if="!busy" name="arrow" :size="18" /></button>
          <p class="form-footnote">已有账号？安装客户端后即可登录。<button type="button" @click="emit('open', 'download')">查看下载</button></p>
        </form>
      </div>
    </template>
    <template v-else-if="kind === 'download'"><div class="dialog-header"><span class="large-icon"><Icon name="download" :size="28" /></span><span class="eyebrow">DOWNLOAD CENTER</span><h2 id="dialog-title">{{ title }}</h2><p>为日常运营，准备一个更有条理的工作空间。</p></div><div class="download-platforms" role="tablist" aria-label="操作系统"><button id="windows-tab" role="tab" :aria-selected="platform === 'windows'" aria-controls="download-panel" @click="platform = 'windows'" @keydown.right="platform = 'mac'; $refs.macTab.focus()" @keydown.left="platform = 'mac'; $refs.macTab.focus()" ref="windowsTab" :tabindex="platform === 'windows' ? 0 : -1"><Icon name="windows" :size="24" /><b>Windows</b><small>64 位客户端</small></button><button id="mac-tab" role="tab" :aria-selected="platform === 'mac'" aria-controls="download-panel" @click="platform = 'mac'" @keydown.right="platform = 'windows'; $refs.windowsTab.focus()" @keydown.left="platform = 'windows'; $refs.windowsTab.focus()" ref="macTab" :tabindex="platform === 'mac' ? 0 : -1"><Icon name="laptop" :size="24" /><b>macOS</b><small>暂未提供</small></button></div>
      <div id="download-panel" class="download-panel" role="tabpanel" :aria-labelledby="platform === 'windows' ? 'windows-tab' : 'mac-tab'">
        <template v-if="platform === 'windows'"><span class="release-state"><i></i>{{ downloadUrl ? '可下载' : '安装包准备中' }}</span><h3>SuperPower Browser for Windows</h3><p>Windows x64 · {{ config.windows?.version ? `v${config.windows.version}` : '首个公开安装包即将开放' }}</p><p v-if="config.windows?.size || config.windows?.publishedAt" class="release-meta">{{ config.windows.size }} {{ config.windows.publishedAt }}</p><a v-if="downloadUrl" :href="downloadUrl" class="button primary full" download @click="notice = '下载已请求。如未开始，请检查浏览器下载列表。'">下载 Windows 客户端<Icon name="download" :size="18" /></a><button v-else class="button primary full" disabled>敬请期待<Icon name="clock" :size="18" /></button><code v-if="downloadUrl && config.windows?.sha256" class="checksum">SHA-256: {{ config.windows.sha256 }}</code></template>
        <template v-else><span class="release-state pending"><i></i>暂无 macOS 客户端</span><h3>期待与你在更多设备相遇</h3><p>当前仅提供 Windows x64 方向的客户端。<br />macOS 版本的发布计划，请联系微信咨询。</p><button class="button primary full" disabled>暂未提供下载</button></template>
        <p v-if="notice" role="status">{{ notice }}</p><button class="text-button" @click="emit('open', 'contact')">联系微信，了解发布进展 <Icon name="arrow" :size="15" /></button>
      </div><p class="download-note"><Icon name="shield" :size="15" /> 请通过官方渠道获取客户端与更新。</p>
    </template>
    <template v-else-if="kind === 'contact'"><div class="dialog-header"><span class="large-icon green"><Icon name="message" :size="28" /></span><span class="eyebrow">LET’S TALK</span><h2 id="dialog-title">{{ title }}</h2><p>产品体验、使用问题或团队合作，欢迎联系。</p></div><div class="wechat-contact"><Icon name="message" :size="30" /><div><small>添加微信，开始沟通</small><strong>{{ config.wechat }}</strong></div><button class="button primary" @click="emit('copy', config.wechat)"><Icon name="copy" :size="16" />复制微信号</button></div><p class="contact-hint">在微信中选择「添加朋友」，粘贴上方微信号进行搜索。</p><div class="other-contact"><span><Icon name="phone" :size="17" />联系电话 <b>{{ config.phone || '待公布' }}</b></span><span><Icon name="mail" :size="17" />联系邮箱 <b>{{ config.email || '待公布' }}</b></span></div>
    </template>
    <template v-else><div class="legal-content"><span class="eyebrow">SUPERPOWER BROWSER</span><h2 id="dialog-title">{{ title }}</h2><p class="muted">说明版本：2026 年 11 月 5 日</p><template v-if="kind === 'terms'"><h3>产品与当前可用性</h3><p>本网站介绍 SuperPower Browser 及独立 AI 工作台的产品能力。安装包、公开注册及各项服务的开放状态以页面实际提示为准。AI 工作台尚未完整集成到新版浏览器。</p><h3>账号与使用</h3><p>注册开放后，请使用真实可控的账号信息并妥善保管密码。账号仅用于其所属服务，团队成员应使用独立账号及管理员授权的资源。请遵守所访问平台的规则，不得利用产品开展违法活动。</p><h3>功能边界</h3><p>环境隔离与代理配置不能保证账号免于平台审核、关联判断或封禁。AI 执行结果依赖所配置模型与技能，需要你检查任务计划并按实际需求授权。网站示意图中的店铺和数据均为演示内容。</p><h3>联系与后续更新</h3><p>公开商业服务的主体、价格及完整服务条款将在正式发布时公布。当前体验与疑问请联系微信 {{ config.wechat }}。</p></template><template v-else><h3>官网收集的信息</h3><p>本官网不使用第三方广告追踪或分析脚本。公开注册未开放时，表单内容仅存在于当前页面内存中，不会提交到服务器或保存至本地存储；关闭弹窗会清除密码。</p><h3>公开注册开放后</h3><p>提交注册会将账号、密码、说明确认状态及版本发送到配置的同源注册服务。密码应在服务端进行不可逆哈希保存；服务端可能保留请求时间与网络地址以防止滥用。官网不会自动提交你正在浏览的店铺信息。</p><h3>联系与第三方服务</h3><p>复制微信号不会向微信发送消息。你在微信联系时提供的信息由实际沟通过程处理。AI 工作台中使用的模型服务及消息渠道遵循各自的隐私规则，调用前请检查配置与授权范围。</p><h3>管理你的信息</h3><p>如需咨询账号信息更正、删除或隐私事项，请联系微信 {{ config.wechat }}。正式上线前将补充运营主体与完整隐私政策。</p></template><button class="button primary" @click="close">{{ legalBack ? '返回注册' : '我已了解' }}</button></div></template>
  </section></div></Transition></Teleport>
</template>
