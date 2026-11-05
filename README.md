# SuperPower Browser 官网

官网前端与注册服务端集中存放于此目录，分别构建和部署。

| 项目 | 说明 | 本地地址 |
| --- | --- | --- |
| [super-power-browser-official-web](super-power-browser-official-web/README.md) | Vue 3 + Vite 官网 | http://127.0.0.1:5178/ |
| [super-power-browser-official-server](super-power-browser-official-server/README.md) | Java 17 + Spring Boot 注册服务 | http://127.0.0.1:18081/ |

在前端项目目录执行 `npm ci` 安装依赖、`npm run dev` 启动开发服务、`npm run build` 生成 `dist/`。

在服务端项目目录执行 `mvn clean package` 生成 `target/super-power-browser-official-server-0.1.0.jar`。按服务端说明配置与浏览器后台共用的 MySQL 数据库，再启动服务。注册默认关闭，通过 `REGISTRATION_ENABLED` 控制。

前端开发服务将 `/api` 转发至 `127.0.0.1:18081`。生产部署配置见 [Nginx 示例](super-power-browser-official-web/deploy/nginx.conf.example)。
