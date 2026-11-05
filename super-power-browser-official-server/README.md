# SuperPower Browser 官网注册服务

独立的 Java 17 / Spring Boot 后台，位于 `super-power-browser-official/super-power-browser-official-server`，与官网前端 `super-power-browser-official-web` 同级。它不依赖浏览器项目源码或 Maven 模块，专门为官网提供公开注册接口。

## 与新版账号互通

官网后台与新版浏览器后台连接 **同一个已完成新版数据库迁移的 MySQL 数据库**。注册使用新版现有 `tenant`、`app_user`、`wallet`、`audit_event` 表：在一个事务内创建独立企业、企业所有者账号、零余额钱包及说明确认记录。

密码使用 BCrypt，与新版 `AuthService` 的算法、成本和格式相同。调用者不能指定已有企业、钱包余额、角色或资源授权。新注册账号只能管理自己的企业。官网不创建桌面登录会话；注册成功后用相同账号密码登录连接该数据库的新版客户端。

数据库表结构由新版后台管理。本服务不自动建表、修改表或运行新版迁移，避免多个独立应用争抢迁移。不要连接未初始化的空数据库后直接开放注册。

## 构建

```powershell
mvn clean package
```

需要 Java 17+、Maven 3.9+。在本项目目录执行构建，生成的可执行 JAR 位于 `target/`。

## 连接新版数据库启动

在启动终端中设置环境变量。密码从部署环境注入，不提交到仓库。

```powershell
$env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/super_power_browser?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC'
$env:DB_USER = 'superpower_official'
$env:DB_PASSWORD = '<数据库密码>'
$env:REGISTRATION_ENABLED = 'true'
java -jar target/super-power-browser-official-server-0.1.0.jar
```

默认地址 `127.0.0.1:18081`。开发官网默认将 `/api` 代理到该端口。`REGISTRATION_ENABLED` 默认是 `false`，配置数据库并准备好正式条款后再开启。数据库账号只需这些权限：`SELECT, INSERT` on `app_user`；`INSERT` on `tenant`、`wallet`、`audit_event`。请用独立数据库账号，不复用超级管理员。

`GET /api/health` 表示进程可响应，并非数据库就绪检查。`GET /api/auth/registration-status` 返回注册开关与说明版本。

## 接口

`POST /api/auth/register`，JSON 示例：

```json
{
  "username": "your.account",
  "password": "a-long-unique-password",
  "acceptedTerms": true,
  "policyVersion": "2026-11-05"
}
```

成功：HTTP 201，`{"id":"…","username":"your.account"}`。不返回密码、哈希或登录令牌。

- 400：账号、密码、说明版本或 JSON 格式不正确。
- 409：账号已存在，包括并发注册冲突。
- 411 / 413：请求没有内容长度或超过 8 KiB。
- 429：请求过于频繁，附带 `Retry-After`。
- 503：注册关闭或数据库服务不可用。

账号为 4–40 位，以字母/数字开头，可包含 `_ . -`；密码至少 12 位且 UTF-8 长度不超过 72 字节。账号大小写唯一性遵循共享数据库排序规则，与新版登录保持一致。

## 部署

生产通过 HTTPS 反向代理，参见同级官网前端的 `../super-power-browser-official-web/deploy/nginx.conf.example`。仅将公开注册相关路径暴露给官网，不代理新版整个管理后台。默认只监听本机，Tomcat 只信任来自本机代理的转发地址；Nginx 必须覆盖 `X-Forwarded-For`。

默认每来源地址 15 分钟最多 10 次、每个进程每分钟总共 60 次注册请求，内存记录数量有上限。可用 `REGISTER_ATTEMPTS_PER_15_MINUTES` 和 `REGISTER_ATTEMPTS_PER_MINUTE` 调整；多实例部署应在统一入口增加共享限流，示例 Nginx 已提供入口限制。

未实现短信验证、找回密码、CMS 管理或安装包上传，这些不属于当前注册后台范围。电话/邮箱/下载版本由官网配置文件管理。
