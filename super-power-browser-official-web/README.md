# SuperPower Browser 官网

独立的 Vue 3 + Vite 官网项目，位于 `super-power-browser-official/super-power-browser-official-web`。与官网后台 `super-power-browser-official-server` 放在同级目录，不依赖浏览器项目的前端源码。

## 页面与交互

- 首页：浏览器环境、代理设备、账号管理、团队权限、独立 AI 工作台、入门步骤、常见问题、联系我们。
- 帮助与支持：问题搜索、分类筛选、可展开的指南与问答。
- 注册：账号/密码/确认密码、说明确认、服务端注册、失败提示和成功状态。密码不写入 localStorage，关闭弹窗即清除。
- 下载：Windows / macOS 切换。Windows 安装包目前占位，macOS 明确标注暂未提供；没有配置下载地址时禁用下载按钮。
- 联系：微信 `15675826196` 可复制；电话和邮箱暂为“待公布”。
- 桌面与手机布局；键盘导航、弹窗焦点限制、Escape 关闭、减少动态效果偏好。

## 本地启动

需要 Node.js 20.19+（建议 Node.js 22 或 24）。

```powershell
npm ci
npm run dev
```

打开 `http://127.0.0.1:5178/`，帮助页为 `http://127.0.0.1:5178/#/help`。

开发服务器默认把 `/api` 转发到独立官网后台 `http://127.0.0.1:18081`。可复制 `.env.example` 为 `.env.local` 调整。先按同级 `super-power-browser-official-server/README.md` 启动后台；后台未启动时表单会显示服务不可用，不会假装注册成功。

## 上线配置

```powershell
npm run build
```

发布 `dist/` 到站点根目录。使用 HTTPS，按 `deploy/nginx.conf.example` 把注册相关 `/api` 路径反向代理到官网后台。`npm run preview` 仅用于查看构建产物，不包含开发代理，联调注册请使用开发服务器或生产反向代理。

编辑 `public/site-config.json`（发布后也可直接编辑 `dist/site-config.json`），无需改源码：

| 字段 | 用途 |
| --- | --- |
| `wechat` / `phone` / `email` | 联系信息；电话、邮箱空值显示“待公布” |
| `registrationEndpoint` | 默认 `/api/auth/register`；留空时明确展示注册未开放 |
| `windows.url` | 真实 Windows 安装包链接；留空则禁用下载。支持 HTTPS 或同源地址 |
| `windows.version` / `size` / `publishedAt` / `sha256` | 安装包版本、大小、日期、校验值；未填不虚构 |
| `icp` | 正式备案信息；留空不展示虚构备案 |

安装包可放在自己的 HTTPS 下载域名/CDN。较大的二进制文件不应提交进官网源码仓库；`public/downloads/` 已忽略。页面不会下载其他品牌客户端或把浏览器内核压缩包当作客户端安装包。

## 内容边界

官网介绍基于 SuperPower Browser 的店铺、环境、设备、成员、角色、账号助手等实现。AI 工作台介绍模型接入、技能管理、任务执行、定时计划与企业消息功能，页面明确标记为独立客户端能力、新版集成中。不宣称已内置尚未迁入的能力。

产品画面为 HTML/CSS 示意图，包含明确的演示数据标记。品牌图标来自本产品的 `branding/mark.svg`。

公开上线前请填写真实运营主体，并将当前使用/隐私说明替换为与实际服务一致的正式条款。修改说明版本时，同步修改注册表单的 `policyVersion` 和后台 `RegistrationService.POLICY_VERSION`。
