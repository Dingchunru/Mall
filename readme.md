把 readme 里的端口改成实际端口，启动顺序也更新了。完整的替换如下：

```md
# 🛒 Mall 微服务商城系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2023.0.1-blue.svg" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2023.0.1.0-orange.svg" alt="Spring Cloud Alibaba">
  <img src="https://img.shields.io/badge/MySQL-8.0.33-blue.svg" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-7.0+-red.svg" alt="Redis">
  <img src="https://img.shields.io/badge/Elasticsearch-7.17.18-green.svg" alt="Elasticsearch">
  <img src="https://img.shields.io/badge/RocketMQ-2.3.0-orange.svg" alt="RocketMQ">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

## 📖 项目简介

**Mall** 是一个基于 **Spring Cloud** 微服务架构的**企业级电商平台**，采用**领域驱动设计**，提供完整的**B2C电商解决方案**。项目遵循**微服务最佳实践**，实现了**高可用、高性能、可伸缩**的电商系统架构。

### ✨ 核心特性

- 🔐 **完整的用户认证体系** - JWT 双 Token 机制，支持 Token 刷新与黑名单
- 🛍️ **商品管理** - 商品分类、商品详情、商品搜索
- 🛒 **购物车服务** - Redis 实现，支持多端同步
- 📦 **订单系统** - 订单创建、支付、取消、查询
- ⚡ **秒杀系统** - Redis 原子操作 + RocketMQ 异步处理
- 🔍 **全文搜索** - Elasticsearch 实现商品搜索
- 🚪 **API网关** - 统一路由、鉴权、限流、日志
- 📊 **配置中心** - Nacos 实现注册发现
- 🛡️ **密码加密** - BCrypt 加密存储
- 📝 **统一异常处理** - 全局异常拦截，友好提示
- 📋 **操作日志** - 关键操作日志记录
- ⛓️ **登录保护** - 密码错误次数限制，防暴力破解

## 🏗️ 系统架构

```
                              ┌─────────────────────────────────────────────────────────────┐
                              │                    客户端 (Web/App)                           │
                              └─────────────────────────────────────────────────────────────┘
                                                        │
                                                        ▼
                              ┌─────────────────────────────────────────────────────────────┐
                              │               API网关 (Spring Cloud Gateway)                  │
                              │              (路由、认证、限流、日志、CORS)                      │
                              │                     端口: 8080                                │
                              └─────────────────────────────────────────────────────────────┘
                                                        │
                   ┌──────────┬──────────┬──────────┬──────────┬──────────┐
                   ▼          ▼          ▼          ▼          ▼          ▼
            ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
            │ 认证服务  │ │ 用户服务  │ │ 商品服务  │ │ 订单服务  │ │ 购物车   │
            │mall-auth │ │mall-user │ │mall-prod │ │mall-order│ │mall-cart │
            │  :8081   │ │  :8083   │ │  :8082   │ │  :8084   │ │  :8085   │
            └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘
                   │          │          │          │          │
                   └──────────┴──────────┴──────────┴──────────┘
                                        │
                         ┌──────────────┴──────────────┐
                         ▼                             ▼
                  ┌──────────┐ ┌──────────┐ ┌─────────────────────────────────────┐
                  │ 秒杀服务  │ │ 搜索服务  │ │         中间件 & 基础设施              │
                  │mall-seck │ │mall-srch │ │ MySQL Redis ES RocketMQ Nacos       │
                  │  :8086   │ │  :8087   │ │                                     │
                  └──────────┘ └──────────┘ └─────────────────────────────────────┘
```

## 🚀 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 微服务基础框架 |
| Spring Cloud | 2023.0.1 | 微服务全家桶 |
| Spring Cloud Alibaba | 2023.0.1.0 | 阿里微服务组件 |
| Spring Cloud Gateway | — | API 网关 |
| Nacos | 2.x | 注册中心 / 配置中心 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 7.0+ | 缓存 / 限流 / Token |
| Elasticsearch | 7.17.18 | 搜索引擎 |
| RocketMQ | 2.3.0 | 消息队列 |
| JWT | 0.12.5 | 令牌认证 |
| BCrypt | 6.2.4 | 密码加密 |
| Hutool | 5.8.18 | Java 工具类库 |
| Lombok | 1.18.30 | 代码简化 |

## 📦 模块详解

| 模块 | 端口 | 数据库 | Redis | ES | RocketMQ | 说明 |
|------|:---:|:---:|:---:|:---:|:---:|------|
| mall-gateway | 8080 | — | ✅ | — | — | API 网关，统一入口 |
| mall-auth | 8081 | ✅ | ✅ | — | — | 认证中心 |
| mall-user | 8083 | ✅ | ✅ | — | — | 用户服务 |
| mall-product | 8082 | ✅ | ✅ | — | — | 商品服务 |
| mall-order | 8084 | ✅ | ✅ | — | ✅ | 订单服务 |
| mall-cart | 8085 | — | ✅ | — | — | 购物车服务 |
| mall-seckill | 8086 | ✅ | ✅ | — | ✅ | 秒杀服务 |
| mall-search | 8087 | — | — | ✅ | — | 搜索服务 |
| mall-common | — | — | — | — | — | 公共模块 |

## 🚀 快速开始

### 环境要求

| 组件 | 版本 | 必须 |
|------|------|:---:|
| JDK | 21+ | ✅ |
| Maven | 3.6+ | ✅ |
| MySQL | 8.0+ | ✅ |
| Redis | 7.0+ | ✅ |
| Nacos | 2.x | ✅ |
| Elasticsearch | 7.17.x | ⬜ |
| RocketMQ | 5.x | ⬜ |

---

### 基础设施启动（按顺序）

```bash
# 1️⃣ MySQL
# 确保 MySQL 服务已启动，端口 3306

# 2️⃣ Redis
redis-server
redis-cli ping  # 返回 PONG

# 3️⃣ Nacos（Windows）
cd D:\nacos\bin
startup.cmd -m standalone
# 控制台: http://localhost:8848/nacos (nacos/nacos)

# 4️⃣ Elasticsearch（搜索功能需要）
cd D:\elasticsearch\bin
elasticsearch.bat
# 验证: http://localhost:9200

# 5️⃣ RocketMQ（订单/秒杀需要）
# 启动 NameServer
start mqnamesrv.cmd
# 启动 Broker
start mqbroker.cmd -n localhost:9876
```

---

### 微服务启动

#### 方式一：一键脚本

项目根目录下有 `start.bat`，双击运行。

```bat
@echo off
chcp 65001 >nul
echo ========== 启动 Mall 微服务 ==========

start "mall-gateway" cmd /k "cd /d E:\Mall\mall-gateway && mvn spring-boot:run"
timeout /t 8 >nul

start "mall-auth" cmd /k "cd /d E:\Mall\mall-auth && mvn spring-boot:run"
start "mall-user" cmd /k "cd /d E:\Mall\mall-user && mvn spring-boot:run"
start "mall-product" cmd /k "cd /d E:\Mall\mall-product && mvn spring-boot:run"
start "mall-order" cmd /k "cd /d E:\Mall\mall-order && mvn spring-boot:run"
start "mall-cart" cmd /k "cd /d E:\Mall\mall-cart && mvn spring-boot:run"
start "mall-seckill" cmd /k "cd /d E:\Mall\mall-seckill && mvn spring-boot:run"
start "mall-search" cmd /k "cd /d E:\Mall\mall-search && mvn spring-boot:run"

echo ========== 全部启动完成 ==========
pause
```

#### 方式二：逐个启动（推荐首次）

每个服务打开一个终端：

```bash
cd E:\Mall\mall-gateway   && mvn spring-boot:run
cd E:\Mall\mall-auth      && mvn spring-boot:run
cd E:\Mall\mall-user      && mvn spring-boot:run
cd E:\Mall\mall-product   && mvn spring-boot:run
cd E:\Mall\mall-order     && mvn spring-boot:run
cd E:\Mall\mall-cart      && mvn spring-boot:run
cd E:\Mall\mall-seckill   && mvn spring-boot:run
cd E:\Mall\mall-search    && mvn spring-boot:run
```

---

### 验证

所有服务启动后，打开 Nacos 控制台 http://localhost:8848/nacos ，服务列表应有 **8 个在线实例**。

---

## 📊 监控管理

| 组件 | 地址 | 账号/密码 |
|------|------|-----------|
| Nacos | http://localhost:8848/nacos | nacos/nacos |
| Elasticsearch | http://localhost:9200 | — |

## 🐛 常见问题

| 问题 | 解决 |
|------|------|
| 端口被占用 | `netstat -ano \| findstr 8080` → `taskkill /PID xxx /F` |
| 数据库连接失败 | 检查 MySQL 服务和 `application.yml` 配置 |
| Redis 连接失败 | `redis-cli ping` 确认 Redis 运行 |
| Nacos 连接失败 | 确认 Nacos 已 `startup.cmd -m standalone` |
| 启动报 DataSource 错误 | mall-cart/search 已排除 DataSource，检查配置 |
| Token 401 | 登录获取新 Token，或检查 Gateway JWT 过滤器 |

## 📞 联系方式

- **项目负责人**: 丁春儒（dingchunru@foxmail.com）
- **项目地址**: https://github.com/Dingchunru/Mall

---

<p align="center">
  ⭐️ 如果这个项目对你有帮助，欢迎 Star！
</p>

<p align="center">
  Made with ❤️ by Mall Team
</p>