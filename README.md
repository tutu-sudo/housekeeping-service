# Housekeeping Service Platform

一个面向家政服务场景的全栈平台，覆盖 **顾客端、服务人员端、管理员端**，提供从服务浏览、预约下单、支付履约到评价与运营统计的完整闭环。

---

## Three Lines

- **What it is**: 一个基于 `Vue 3 + Spring Boot` 的家政服务预约与运营平台（Monorepo：`frontend` + `backend`）。
- **Pain it solves**: 将家政业务中的预约、排班、支付、评价、审核和统计统一到同一套系统，降低人工协同成本。
- **Use in 3 minutes**: 启动 MySQL/Redis -> 启动后端 -> 启动前端，即可体验完整业务流程。

---

## 项目结构

```text
housekeeping-service/
├─ frontend/   # Vue3 + Vite + Element Plus
└─ backend/    # Spring Boot + MyBatis + MySQL + Redis
```

---

## 核心能力

### 1) 认证与权限
- 登录、注册、修改密码、忘记密码（邮箱验证码流程）
- JWT 鉴权与角色权限控制（顾客 / 员工 / 管理员）

### 2) 服务与员工体系
- 服务类型与服务项目管理
- 员工资料维护、技能关联、熟练度与证书审核
- 用户升级员工、后台审核与状态管理

### 3) 预约与履约
- 预约创建、查询、状态流转
- 员工接单 / 拒单 / 开始服务 / 完成服务
- 定时任务自动推进预约状态、自动清理超时未支付订单

### 4) 支付与订单
- 支付宝下单、异步回调、支付状态同步
- 管理端按预约查询支付记录
- 预留微信支付配置能力

### 5) 评价与服务质量
- 订单评价、追评/附加评价、评价聚合查询
- 员工与服务维度质量统计

### 6) 排班与运营
- 员工排班（员工端 + 管理端）
- 统计看板：预约趋势、收入趋势、支付方式、员工工作量、客户活跃度等

### 7) 系统支撑
- 文件上传与访问
- 站内消息
- 全局异常处理与统一返回结构
- AOP 接口操作日志

---

## 技术栈

### Frontend
- Vue 3.4+
- Vite 5+
- Element Plus 2.5+
- Vue Router 4+
- Vuex 4+
- Axios
- ECharts + vue-echarts
- Day.js

### Backend
- Java 17
- Spring Boot 3.3.x（Web / Validation / Mail / Redis / Scheduling）
- MyBatis + MySQL
- JWT
- Spring AOP
- Springdoc OpenAPI / Swagger UI
- 支付：支付宝（含回调）

---

## API 分层设计（Backend）

- `controller`：按角色与业务域暴露 REST 接口（如 `/api/auth`、`/api/appointments`、`/api/admin/**`）
- `service`：业务逻辑编排（预约流转、支付回调处理、统计聚合等）
- `mapper + mapper.xml`：MyBatis 持久层与 SQL
- `entity / dto / vo`：数据实体、传输对象、展示对象
- `config`：JWT 拦截器、WebMvc 跨域与资源映射、Swagger、定时任务、AOP 日志
- `common`：统一响应、异常体系、权限注解与错误码

---

## 快速开始

### 0. 环境准备
- Node.js 18+
- JDK 17
- MySQL 8+
- Redis

### 1. 克隆项目

```bash
git clone https://github.com/tutu-sudo/housekeeping-service.git
cd housekeeping-service
```

### 2. 启动后端

1. 创建数据库并执行初始化脚本（见 `backend/src/main/resources/db`）  
2. 按需修改 `backend/src/main/resources/application.yml`（数据库、Redis、邮件、支付、文件路径）  
3. 启动项目：

```bash
cd backend
mvn spring-boot:run
```

默认端口：`8080`

接口文档：
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发地址（以 Vite 输出为准，常见为）：
- [http://localhost:5173](http://localhost:5173)

---

## 数据库脚本说明

`backend/src/main/resources/db` 已提供初始化与增量脚本，覆盖：

- 核心业务：用户、员工、服务类型、服务项、预约、支付、评价
- 系统支撑：消息、文件、邮件/短信记录、操作日志等

建议阅读：
- `backend/src/main/resources/db/README.md`

---

## 业务角色说明

- **顾客端**：浏览服务、筛选人员、预约下单、支付、评价、查看个人信息
- **服务人员端**：管理资料、查看排班、处理预约、履约流程
- **管理员端**：用户管理、员工审核、服务管理、订单/支付/评价管理、运营统计

---

## 安全与配置建议（开源前必读）

- 配置文件中涉及数据库账号、JWT 密钥、支付参数等敏感信息，请改为环境变量或示例值
- 默认管理员账号/密码仅用于开发演示，部署前必须修改
- 支付回调地址、文件存储路径需按实际部署环境调整

---

## Roadmap

- [ ] 完善 Docker / docker-compose 一键部署
- [ ] 增加 CI（构建、测试、Lint）
- [ ] 增加接口权限矩阵与 ER 图文档
- [ ] 增加更多支付渠道（微信支付完整实现）

---

## License

建议根据发布计划选择 License（MIT / Apache-2.0 / 私有仓库不添加）。

