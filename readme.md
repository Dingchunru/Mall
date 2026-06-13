# 🛒 Mall 微服务商城系统

## 📖 项目简介

**Mall** 是一个基于 **Spring Cloud** 微服务架构的**企业级电商平台**，提供完整的 B2C 电商解决方案。项目遵循微服务最佳实践，实现了**高可用、高性能**的电商系统架构。

### ✨ 核心特性

- 🔐 **JWT 双 Token 认证** — Access Token + Refresh Token，支持刷新与黑名单
- 🛍️ **商品管理** — 分类、详情、库存扣减/恢复
- 🛒 **购物车** — Redis 存储，支持选中/全选/清空
- 📦 **订单系统** — 创建、支付、取消、确认收货、超时自动取消
- ⚡ **秒杀系统** — Lua 原子扣库存 + RocketMQ 异步下单 + 幂等防刷
- 🔍 **全文搜索** — Elasticsearch 商品搜索 + 定时索引同步
- 🚪 **API 网关** — 统一路由、JWT 鉴权、令牌桶限流、TraceId 链路追踪
- 🛡️ **安全加固** — BCrypt 加密、登录失败限流、XSS 防护、幂等性保护
- 📝 **统一返回** — Result 包装 + 全局异常处理 + 错误码规范


## 🚀 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | 微服务基础框架 |
| Spring Cloud | 2023.0.1 | 微服务全家桶 |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos 注册中心 |
| Spring Cloud Gateway | — | API 网关 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 7.0+ | 缓存 / 限流 / Token / 购物车 / 秒杀库存 |
| Elasticsearch | 7.17.18 | 搜索引擎 |
| RocketMQ | 2.3.0 | 消息队列（订单延迟取消、秒杀削峰） |
| JWT | 0.12.5 | 令牌认证 |
| BCrypt | 6.2.4 | 密码加密 |
| Lombok | 1.18.30 | 代码简化 |

## 📦 模块详解

| 模块 | 端口 | MySQL | Redis | ES | RocketMQ | 职责 |
|------|:---:|:---:|:---:|:---:|:---:|------|
| mall-gateway | 8080 | — | ✅ | — | — | 统一入口：路由、鉴权、限流、日志、TraceId |
| mall-auth | 8081 | ✅ | ✅ | — | — | 认证中心：登录、注册、Token 签发/刷新/验证 |
| mall-user | 8083 | ✅ | ✅ | — | — | 用户服务：个人信息、密码、管理员锁定/解锁 |
| mall-product | 8082 | ✅ | ✅ | — | — | 商品服务：CRUD、库存扣减/恢复、批量查询 |
| mall-order | 8084 | ✅ | ✅ | — | ✅ | 订单服务：创建、支付、取消、超时自动取消 |
| mall-cart | 8085 | — | ✅ | — | — | 购物车：添加、修改、选中/全选、清空 |
| mall-seckill | 8086 | ✅ | ✅ | — | ✅ | 秒杀服务：Lua 原子扣库存、MQ 异步下单 |
| mall-search | 8087 | — | — | ✅ | — | 搜索服务：ES 商品搜索、定时索引同步 |
| mall-common | — | — | — | — | — | 公共模块：Result、异常、JWT、Redis、幂等 |

### 服务间调用

```
mall-order  ──Feign──►  mall-product  （扣库存、查商品）
mall-order  ──Feign──►  mall-user     （查地址）
mall-search ──Feign──►  mall-product  （同步商品索引）
mall-seckill───MQ───►  mall-seckill   （异步落库）
```

## 🚀 快速开始

### 环境要求

| 组件 | 版本 | 必须 |
|------|------|:---:|
| JDK | 21+ | ✅ |
| Maven | 3.6+ | ✅ |
| MySQL | 8.0+ | ✅ |
| Redis | 7.0+ | ✅ |
| Nacos | 2.x | ✅ |
| Elasticsearch | 7.17.x | ⬜ 搜索功能需要 |
| RocketMQ | 5.x | ⬜ 订单/秒杀需要 |

---


#### 方式一：一键启动

项目根目录执行 `start.bat`，自动打开 8 个窗口依次启动。

#### 方式二：逐个启动（推荐首次）

每个服务单独终端：

```bash
cd mall-gateway   && mvn spring-boot:run
cd mall-auth      && mvn spring-boot:run
cd mall-user      && mvn spring-boot:run
cd mall-product   && mvn spring-boot:run
cd mall-order     && mvn spring-boot:run
cd mall-cart      && mvn spring-boot:run
cd mall-seckill   && mvn spring-boot:run
cd mall-search    && mvn spring-boot:run
```

---

### 3. 验证

打开 Nacos 控制台 http://localhost:8848/nacos ，服务列表应有 **8 个在线实例**。

测试接口：
```bash
# 登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 查询商品
curl http://localhost:8080/api/products?page=1&size=10
```

## 🐛 常见问题

| 问题 | 解决 |
|------|------|
| 端口被占用 | `netstat -ano \| findstr 8080` → `taskkill /PID xxx /F` |
| MySQL 连接失败 | 检查 MySQL 服务和 `application.yml` 数据库配置 |
| Redis 连接失败 | `redis-cli ping` 确认 Redis 运行 |
| Nacos 连接失败 | 确认 `startup.cmd -m standalone` 已执行 |
| mall-cart/search 报 DataSource 错误 | 这两个模块无需数据库，已排除自动配置 |
| Token 401 | 重新登录获取 Token；检查 Gateway JWT 过滤器 |
| Elasticsearch 连接失败 | 搜索功能不可用，不影响其他模块 |

---

<p align="center">
  ⭐️ 如果这个项目对你有帮助，欢迎 Star！
</p>

<p align="center">
  Made with ❤️ by Mall Team
</p>
```