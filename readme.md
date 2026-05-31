# 🛒 Mall 微服务商城系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.10-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2021.0.5-blue.svg" alt="Spring Cloud">
  <img src="https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2021.0.4.0-orange.svg" alt="Spring Cloud Alibaba">
  <img src="https://img.shields.io/badge/MySQL-8.0.33-blue.svg" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-5.0+-red.svg" alt="Redis">
  <img src="https://img.shields.io/badge/Elasticsearch-7.17.9-green.svg" alt="Elasticsearch">
  <img src="https://img.shields.io/badge/RabbitMQ-3.12+-orange.svg" alt="RabbitMQ">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License">
</p>

## 📖 项目简介

**Mall** 是一个基于 **Spring Cloud** 微服务架构的**企业级电商平台**，采用**领域驱动设计**，提供完整的**B2C电商解决方案**。项目遵循**微服务最佳实践**，实现了**高可用、高性能、可伸缩**的电商系统架构。

### ✨ 核心特性

- 🔐 **完整的用户认证体系** - JWT + Spring Security，支持单点登录
- 🛍️ **商品管理** - 商品分类、商品详情、商品搜索
- 🛒 **购物车服务** - Redis实现，支持多端同步
- 📦 **订单系统** - 订单创建、支付、取消、查询
- ⚡ **秒杀系统** - Redis原子操作 + RabbitMQ异步处理
- 🔍 **全文搜索** - Elasticsearch实现商品搜索
- 🚪 **API网关** - 统一路由、鉴权、限流
- 📊 **配置中心** - Nacos实现配置统一管理
- 📈 **服务监控** - Spring Boot Admin监控服务状态

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                       客户端 (Web/App)                         │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     API网关 (Spring Cloud Gateway)           │
│                      (路由、认证、限流、日志)                     │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌───────────────────┬───────────────────┐
        ▼                   ▼                   ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   认证服务     │    │   用户服务     │    │   商品服务     │
│   mall-auth   │    │  mall-user    │    │ mall-product  │
└───────────────┘    └───────────────┘    └───────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   订单服务     │    │   购物车服务   │    │   秒杀服务     │
│  mall-order   │    │   mall-cart   │    │ mall-seckill  │
└───────────────┘    └───────────────┘    └───────────────┘
        │                   │                   │
        ▼                   ▼                   ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   搜索服务     │    │   公共模块     │    │   消息队列     │
│  mall-search  │    │  mall-common  │    │   RabbitMQ    │
└───────────────┘    └───────────────┘    └───────────────┘

┌─────────────────────────────────────────────────────────────┐
│                 基础设施层 (MySQL、Redis、ES)                   │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                 注册配置中心 (Nacos)                           │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 技术栈

### 后端核心
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.10 | 微服务基础框架 |
| Spring Cloud | 2021.0.5 | 微服务全家桶 |
| Spring Cloud Alibaba | 2021.0.4.0 | 阿里微服务组件 |
| Spring Security | 5.7.7 | 认证授权 |
| Spring Cloud Gateway | 3.1.5 | API网关 |
| Spring Cloud OpenFeign | 3.1.5 | 声明式服务调用 |

### 数据存储
| 技术 | 版本 | 说明 |
|------|------|------|
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | 5.0+ | 缓存数据库 |
| Elasticsearch | 7.17.9 | 搜索引擎 |

### 中间件
| 技术 | 版本 | 说明 |
|------|------|------|
| Nacos | 2.0.4 | 注册中心/配置中心 |
| RabbitMQ | 3.12+ | 消息队列 |
| MyBatis-Plus | 3.5.3.1 | ORM框架 |

### 工具库
| 技术 | 版本 | 说明 |
|------|------|------|
| Lombok | 1.18.30 | 代码简化 |
| JWT | 0.11.5 | 令牌认证 |
| Swagger3 | 3.0.0 | API文档 |
| Fastjson2 | 2.0.25 | JSON处理 |
| Hutool | 5.8.18 | Java工具类库 |

## 📦 模块详解

### 1. mall-auth - 认证中心 🔐
- **功能**: 用户认证、JWT令牌颁发、权限控制
- **技术亮点**: Spring Security + JWT
- **API接口**:
  - `POST /auth/login` - 用户登录
  - `POST /auth/register` - 用户注册
  - `POST /auth/logout` - 用户登出
  - `POST /auth/refresh` - 刷新令牌
- **端口**: 8081

### 2. mall-user - 用户服务 👤
- **功能**: 用户信息管理、地址管理
- **技术亮点**: MyBatis-Plus
- **API接口**:
  - `GET /user/info/{id}` - 获取用户信息
  - `PUT /user/info` - 更新用户信息
  - `GET /user/current` - 获取当前用户
- **端口**: 8082

### 3. mall-product - 商品服务 📦
- **功能**: 商品管理、分类管理、库存管理
- **技术亮点**: MyBatis-Plus + Redis缓存
- **API接口**:
  - `GET /product/list` - 商品列表
  - `GET /product/detail/{id}` - 商品详情
  - `POST /product` - 添加商品
  - `PUT /product/{id}` - 更新商品
- **端口**: 8083

### 4. mall-order - 订单服务 📋
- **功能**: 订单创建、支付、取消、查询
- **技术亮点**: 分布式事务 + 状态机
- **API接口**:
  - `POST /order/create` - 创建订单
  - `GET /order/list` - 订单列表
  - `GET /order/detail/{orderNo}` - 订单详情
  - `POST /order/cancel/{orderNo}` - 取消订单
  - `POST /order/pay/{orderNo}` - 支付订单
- **端口**: 8084

### 5. mall-cart - 购物车服务 🛒
- **功能**: 购物车管理
- **技术亮点**: Redis存储
- **API接口**:
  - `GET /cart` - 获取购物车
  - `POST /cart/add` - 添加商品
  - `PUT /cart/update` - 更新数量
  - `DELETE /cart/remove` - 删除商品
  - `DELETE /cart/clear` - 清空购物车
- **端口**: 8085

### 6. mall-seckill - 秒杀服务 ⚡
- **功能**: 秒杀活动、库存扣减
- **技术亮点**: Redis原子操作 + RabbitMQ异步
- **API接口**:
  - `GET /seckill/list` - 秒杀商品列表
  - `POST /seckill/{seckillId}` - 参与秒杀
  - `GET /seckill/result/{seckillId}` - 秒杀结果
- **端口**: 8086

### 7. mall-search - 搜索服务 🔍
- **功能**: 商品搜索、搜索建议
- **技术亮点**: Elasticsearch
- **API接口**:
  - `POST /search/products` - 搜索商品
  - `GET /search/suggest` - 搜索建议
  - `POST /search/sync/{productId}` - 同步商品
- **端口**: 8087

### 8. mall-gateway - 网关服务 🚪
- **功能**: 路由转发、统一认证、限流、日志
- **技术亮点**: Spring Cloud Gateway
- **端口**: 8080

### 9. mall-common - 公共模块 📚
- **功能**: 通用工具类、公共配置、统一返回结果
- **包含组件**:
  - 统一返回结果 `Result`
  - JWT工具类 `JwtUtils`
  - 异常处理 `BusinessException`
  - 公共实体类 `BaseEntity`

## 🚀 快速开始

### 环境要求
- ✅ JDK 11+
- ✅ Maven 3.6+
- ✅ MySQL 5.7+
- ✅ Redis 5.0+
- ✅ Nacos 2.0.4
- ✅ RabbitMQ 3.12+ (可选)
- ✅ Elasticsearch 7.17.9 (可选)

### 基础设施启动

#### 1. 启动Nacos
```bash
# Windows
cd D:\nacos\bin
startup.cmd -m standalone

# Linux/Mac
cd /path/to/nacos/bin
sh startup.sh -m standalone
```
访问控制台: http://localhost:8848/nacos (用户名/密码: nacos/nacos)

#### 2. 启动Redis
```bash
redis-server
# 验证
redis-cli ping  # 返回 PONG
```

#### 3. 启动RabbitMQ（可选）
```bash
# Windows
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-3.12.0\sbin"
rabbitmq-server start
rabbitmq-plugins enable rabbitmq_management
```
访问控制台: http://localhost:15672 (用户名/密码: guest/guest)

#### 4. 启动Elasticsearch（可选）
```bash
cd D:\elasticsearch\bin
elasticsearch.bat
```
验证: http://localhost:9200

### 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS mall_user DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS mall_product DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS mall_order DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS mall_seckill DEFAULT CHARACTER SET utf8mb4;

-- 执行SQL脚本（在项目doc目录下）
mysql -uroot -p mall_user < doc/sql/mall_user.sql
mysql -uroot -p mall_product < doc/sql/mall_product.sql
mysql -uroot -p mall_order < doc/sql/mall_order.sql
mysql -uroot -p mall_seckill < doc/sql/mall_seckill.sql
```

### 项目构建

```bash
# 克隆项目
git clone https://github.com/your-repo/mall.git
cd mall

# 编译打包
mvn clean install

# 或者跳过测试
mvn clean install -DskipTests
```

### 服务启动顺序

按以下顺序启动各个微服务（每个服务单独开一个命令行窗口）：

```bash
# 1. 启动认证服务 (端口: 8081)
cd mall-auth
mvn spring-boot:run

# 2. 启动用户服务 (端口: 8082)
cd ../mall-user
mvn spring-boot:run

# 3. 启动商品服务 (端口: 8083)
cd ../mall-product
mvn spring-boot:run

# 4. 启动订单服务 (端口: 8084)
cd ../mall-order
mvn spring-boot:run

# 5. 启动购物车服务 (端口: 8085)
cd ../mall-cart
mvn spring-boot:run

# 6. 启动秒杀服务 (端口: 8086)
cd ../mall-seckill
mvn spring-boot:run

# 7. 启动搜索服务 (端口: 8087)
cd ../mall-search
mvn spring-boot:run

# 8. 启动网关服务 (端口: 8080)
cd ../mall-gateway
mvn spring-boot:run
```

### 一键启动脚本

创建 `start-all.bat` (Windows):

```batch
@echo off
echo Starting all mall services...

start "mall-auth" cmd /c "cd /d E:\Code\mall\mall-auth && mvn spring-boot:run"
timeout /t 10

start "mall-user" cmd /c "cd /d E:\Code\mall\mall-user && mvn spring-boot:run"
timeout /t 10

start "mall-product" cmd /c "cd /d E:\Code\mall\mall-product && mvn spring-boot:run"
timeout /t 10

start "mall-order" cmd /c "cd /d E:\Code\mall\mall-order && mvn spring-boot:run"
timeout /t 10

start "mall-cart" cmd /c "cd /d E:\Code\mall\mall-cart && mvn spring-boot:run"
timeout /t 10

start "mall-seckill" cmd /c "cd /d E:\Code\mall\mall-seckill && mvn spring-boot:run"
timeout /t 10

start "mall-search" cmd /c "cd /d E:\Code\mall\mall-search && mvn spring-boot:run"
timeout /t 10

start "mall-gateway" cmd /c "cd /d E:\Code\mall\mall-gateway && mvn spring-boot:run"

echo All services started!
pause
```

## 📚 API文档

启动所有服务后，访问以下地址查看API文档：

| 服务 | Swagger地址 |
|------|-------------|
| 网关服务 | http://localhost:8080/swagger-ui/ |
| 认证服务 | http://localhost:8081/swagger-ui/ |
| 用户服务 | http://localhost:8082/swagger-ui/ |
| 商品服务 | http://localhost:8083/swagger-ui/ |
| 订单服务 | http://localhost:8084/swagger-ui/ |
| 购物车服务 | http://localhost:8085/swagger-ui/ |
| 秒杀服务 | http://localhost:8086/swagger-ui/ |
| 搜索服务 | http://localhost:8087/swagger-ui/ |

## 🧪 测试用例

### 1. 用户注册登录测试
```bash
# 注册
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456",
    "phone": "13800138000",
    "email": "test@example.com"
  }'

# 登录
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 2. 商品搜索测试
```bash
# 获取token
TOKEN="your_jwt_token_here"

# 搜索商品
curl -X POST http://localhost:8080/search/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "keyword": "手机",
    "page": 1,
    "size": 10
  }'
```

## 📊 监控管理

### Nacos控制台
- 地址: http://localhost:8848/nacos
- 查看服务注册情况、配置管理

### Spring Boot Admin（可选）
- 地址: http://localhost:8088
- 监控所有微服务状态

### RabbitMQ管理
- 地址: http://localhost:15672
- 查看消息队列状态

## 🐛 常见问题

### Q1: 服务启动失败，端口被占用
```bash
# 查看端口占用
netstat -ano | findstr 8081
# 结束进程
taskkill /PID <进程ID> /F
```

### Q2: 数据库连接失败
检查 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mall_user?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### Q3: Redis连接失败
```bash
# 检查Redis是否启动
redis-cli ping
# 应该返回 PONG
```

### Q4: Nacos连接失败
```bash
# 检查Nacos是否启动
curl http://localhost:8848/nacos
```

## 📞 联系方式

- **项目负责人**:丁春儒（2022211636@bupt.cn）
- **项目地址**: https://github.com/Dingchunru/Mall
- **问题反馈**: [Issues](https://github.com/Dingchunru/Mall/issues)

## 🌟 致谢

感谢所有为这个项目做出贡献的开发者！

---

<p align="center">
  ⭐️ 如果这个项目对你有帮助，欢迎 Star！
</p>

<p align="center">
  Made with ❤️ by Mall Team
</p>
