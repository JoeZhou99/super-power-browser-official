<script setup>
import Icon from './Icon.vue'
defineProps({ compact: Boolean, mode: { type: Number, default: 0 } })
const shops = [
  { letter: 'a', name: 'Amazon · 北美旗舰店', sub: 'amazon.com', color: 'orange', country: 'US', region: '美国', user: '运营一组', state: '运行中' },
  { letter: '♪', name: 'TikTok Shop · 英国店', sub: 'seller-uk.tiktok.com', color: 'dark', country: 'GB', region: '英国', user: '运营二组', state: '就绪' },
  { letter: 'S', name: 'Shopee · 新加坡店', sub: 'seller.shopee.sg', color: 'coral', country: 'SG', region: '新加坡', user: '东南亚团队', state: '就绪' },
  { letter: 'L', name: 'Lazada · 马来西亚店', sub: 'sellercenter.lazada.com.my', color: 'purple', country: 'MY', region: '马来西亚', user: '东南亚团队', state: '就绪' },
]
</script>
<template>
  <div class="product-window" :class="{ compact }" role="img" aria-label="产品能力示意：店铺管理、独立环境、代理设备和团队权限。表格为展示用示例数据。">
    <div class="window-top"><div class="window-dots"><i></i><i></i><i></i></div><span><Icon name="lock" :size="11" /> SuperPower Browser</span><span class="window-top-right">让运营更专注</span></div>
    <div class="product-body">
      <aside class="product-sidebar"><div class="mini-brand"><img src="/brand.svg" alt="" /><b>SuperPower</b></div><div class="workspace-label">我的工作空间 <Icon name="down" :size="12" /></div><div v-for="(item, index) in [['grid','店铺管理'],['globe','代理设备'],['users','团队成员'],['shield','角色权限']]" :key="item[0]" class="product-nav" :class="{ selected: mode === 1 ? index === 1 : mode === 2 ? index === 2 : index === 0 }"><Icon :name="item[0]" :size="16" />{{ item[1] }}</div><div class="sidebar-bottom"><span class="avatar">S</span><span>我的团队<small>企业管理员</small></span></div></aside>
      <div class="product-content"><div class="product-toolbar"><span>工作空间 <span class="muted">/ {{ mode === 1 ? '代理设备' : mode === 2 ? '团队成员' : '店铺管理' }}</span></span><span class="avatar tiny">SP</span></div>
        <div class="product-heading"><div><h3>{{ mode === 1 ? '代理设备' : mode === 2 ? '团队成员' : '我的店铺' }} <span>04</span></h3><p>{{ mode === 1 ? '让每个环境连接到合适的网络' : mode === 2 ? '分工明确，协作更有序' : '每一个账号，都有自己的独立空间' }}</p></div><span class="mock-primary">＋ {{ mode === 1 ? '添加设备' : mode === 2 ? '添加成员' : '新建店铺' }}</span></div>
        <div class="mock-stats"><div><span>独立环境</span><b>4 <small>个</small></b></div><div><span>代理配置</span><b>4 <small>组</small></b></div><div><span>授权成员</span><b>3 <small>人</small></b></div></div>
        <div class="mock-filter"><span>全部{{ mode === 1 ? '设备' : mode === 2 ? '成员' : '店铺' }} <b>4</b></span><span class="mock-search"><Icon name="search" :size="13" />搜索名称</span></div>
        <div class="mock-table"><div class="mock-row table-head"><span>{{ mode === 1 ? '设备名称' : mode === 2 ? '成员 / 团队' : '店铺名称' }}</span><span>{{ mode === 2 ? '角色' : '网络地区' }}</span><span>{{ mode === 2 ? '可访问店铺' : '成员归属' }}</span><span>状态</span></div>
          <div v-for="(shop, i) in shops" :key="shop.name" class="mock-row"><div class="shop-cell"><span class="platform-icon" :class="shop.color">{{ mode === 2 ? ['林','陈','李','周'][i] : shop.letter }}</span><span><strong>{{ mode === 1 ? `${shop.region} · 专用代理` : mode === 2 ? ['林经理','陈运营','李运营','周运营'][i] : shop.name }}</strong><small>{{ mode === 2 ? shop.user : shop.sub }}</small></span></div><span class="region"><span v-if="mode !== 2">{{ shop.country }}</span>{{ mode === 2 ? (i === 0 ? '管理员' : '运营成员') : shop.region }}</span><span>{{ mode === 2 ? (i === 0 ? '全部店铺' : '授权店铺') : shop.user }}</span><span class="status" :class="{ running: i === 0 }"><i></i>{{ mode === 2 ? '已启用' : shop.state }}</span></div>
        </div><div class="mock-footer"><Icon name="shield" :size="12" /> 独立环境 · 权限协作 <span>示例数据，仅用于产品展示</span></div>
      </div>
    </div>
  </div>
</template>
