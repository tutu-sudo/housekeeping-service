# 家政服务预约平台 - 前端项目

基于 Vue3 + Vite + Element Plus 开发的家政服务预约平台前端应用。

## 技术栈

- **框架**: Vue 3.4+
- **构建工具**: Vite 5+
- **UI组件库**: Element Plus 2.5+
- **状态管理**: Vuex 4+
- **路由**: Vue Router 4+
- **HTTP客户端**: Axios
- **图表库**: ECharts + vue-echarts
- **日期处理**: Day.js

## 项目结构

```
housekeeping/
├── src/
│   ├── api/              # API接口封装
│   │   ├── auth.js       # 认证相关接口
│   │   ├── services.js   # 服务相关接口
│   │   ├── appointments.js # 预约相关接口
│   │   ├── reviews.js    # 评价相关接口
│   │   ├── admin.js      # 管理后台接口
│   │   └── files.js      # 文件上传接口
│   ├── components/       # 组件
│   │   ├── common/       # 通用组件
│   │   │   ├── Navigation.vue      # 导航栏
│   │   │   ├── FilterComponent.vue # 筛选器
│   │   │   └── Pagination.vue      # 分页器
│   │   ├── business/     # 业务组件
│   │   │   ├── HousekeeperCard.vue  # 服务人员卡片
│   │   │   ├── AppointmentForm.vue # 预约表单
│   │   │   └── ReviewComponent.vue  # 评价组件
│   │   └── admin/        # 管理组件
│   │       ├── DataTable.vue        # 数据表格
│   │       └── Charts.vue           # 统计图表
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义和守卫
│   ├── store/            # Vuex状态管理
│   │   ├── index.js      # Store入口
│   │   └── modules/      # 状态模块
│   │       ├── user.js   # 用户状态
│   │       ├── business.js # 业务状态
│   │       └── order.js  # 订单状态
│   ├── utils/            # 工具函数
│   │   ├── auth.js       # 认证工具
│   │   └── request.js    # Axios配置
│   ├── views/            # 页面视图
│   │   ├── auth/         # 认证页面
│   │   │   ├── LoginView.vue
│   │   │   └── RegisterView.vue
│   │   ├── customer/     # 客户页面
│   │   │   ├── HomeView.vue
│   │   │   ├── HousekeeperListView.vue
│   │   │   ├── HousekeeperDetailView.vue
│   │   │   ├── AppointmentView.vue
│   │   │   └── ProfileView.vue
│   │   ├── staff/        # 服务人员页面
│   │   │   ├── UserProfileView.vue
│   │   │   └── MyScheduleView.vue
│   │   └── admin/        # 管理后台页面
│   │       ├── AdminDashboardView.vue
│   │       ├── OrderManagementView.vue
│   │       └── VerificationView.vue
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── vite.config.js        # Vite配置
├── package.json          # 项目依赖
└── README.md            # 项目说明
```

## 安装依赖

```bash
npm install
```

## 开发运行

```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动

## 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist` 目录

## 环境变量配置

项目使用 `.env.development` 和 `.env.production` 文件配置环境变量：

- `VITE_API_BASE_URL`: API基础URL地址

开发环境默认: `http://localhost:8080/api`
生产环境默认: `/api`

## 功能模块

### 客户功能
- 浏览服务类型和服务人员
- 筛选服务人员（性别、籍贯、评分、年限）
- 查看服务人员详情和评价
- 创建预约
- 查看个人预约记录
- 个人信息管理

### 服务人员功能
- 个人资料管理
- 查看和管理个人日程
- 确认/拒绝预约

### 管理后台功能
- 数据仪表盘
- 订单管理（查询、筛选、状态更新）
- 服务人员审核
- 服务类型和服务项管理
- 排班管理
- 评价管理
- 支付明细查询

## API接口说明

所有API接口封装在 `src/api` 目录下，已根据后端接口文档完成封装：

- **认证**: `/api/auth/*`
- **服务**: `/api/services/*`
- **预约**: `/api/appointments/*`
- **评价**: `/api/reviews/*`
- **管理后台**: `/api/admin/*`
- **文件**: `/api/files/*`

## 路由说明

- `/` - 重定向到首页
- `/home` - 首页
- `/housekeepers` - 服务人员列表
- `/housekeeper/:id` - 服务人员详情
- `/appointment` - 预约页面（需登录，客户角色）
- `/profile` - 个人中心（需登录，客户角色）
- `/staff/profile` - 服务人员个人资料（需登录，服务人员角色）
- `/staff/schedule` - 服务人员日程（需登录，服务人员角色）
- `/admin/dashboard` - 管理后台仪表盘（需登录，管理员角色）
- `/admin/orders` - 订单管理（需登录，管理员角色）
- `/admin/verification` - 人员审核（需登录，管理员角色）

## 状态管理

使用 Vuex 管理应用状态，分为三个模块：

1. **user模块**: 用户信息、登录状态、用户角色
2. **business模块**: 服务人员列表、筛选条件、购物车状态
3. **order模块**: 当前订单、订单历史

## 注意事项

1. 项目已配置路由守卫，会根据用户角色进行权限控制
2. Token存储在localStorage中，key为 `housekeeping_token`
3. 用户信息存储在localStorage中，key为 `housekeeping_user_info`
4. API请求会自动携带Token（如果存在）
5. 401错误会自动清除认证信息并跳转到登录页

## 开发建议

1. 根据实际后端API响应格式调整 `src/utils/request.js` 中的响应拦截器
2. 根据实际业务需求完善各个视图组件的功能
3. 根据实际数据格式调整组件的数据绑定
4. 完善错误处理和用户提示
5. 添加加载状态和骨架屏提升用户体验

## License

MIT

