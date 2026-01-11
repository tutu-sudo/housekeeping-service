# 数据库设计说明

## 文件说明

- `schema.sql` - 核心业务表结构（用户、服务、预约、订单、评价等）
- `schema_extension.sql` - 系统支撑模块表结构（消息、日志、文件等）

## 数据库初始化步骤

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `housekeeping_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 执行SQL脚本

**方式一：命令行执行**
```bash
# Windows
mysql -u root -p < schema.sql
mysql -u root -p < schema_extension.sql

# Linux/Mac
mysql -u root -p housekeeping_db < schema.sql
mysql -u root -p housekeeping_db < schema_extension.sql
```

**方式二：MySQL客户端执行**
1. 打开MySQL客户端（如Navicat、MySQL Workbench等）
2. 连接到MySQL服务器
3. 依次执行 `schema.sql` 和 `schema_extension.sql` 文件

**方式三：IDE执行**
1. 在IDEA或其他IDE中打开SQL文件
2. 连接到数据库
3. 执行SQL脚本

## 数据库表结构说明

### 4.1 用户相关实体表

1. **user** - 用户基础信息表
   - 存储所有用户的基础信息（顾客、服务人员、管理员）
   - 用户类型：1-顾客，2-服务人员，3-管理员

2. **customer_detail** - 顾客详细信息表
   - 存储顾客的详细信息
   - 与user表一对一关系

3. **staff** - 家政服务人员表
   - 存储服务人员的详细信息
   - 包含验证状态、评分等信息

### 4.2 服务相关实体表

1. **service_type** - 服务类型表
   - 服务类型配置（日常保洁、深度保洁等）

2. **service** - 服务表
   - 具体的服务项目

3. **staff_service_skill** - 服务技能关联表
   - 服务人员与服务技能的关联关系
   - 包含熟练程度、证书等信息

4. **appointment** - 预约记录表
   - 预约的核心业务表
   - 状态：0-待确认，1-已确认，2-进行中，3-已完成，4-已取消，5-已拒绝

5. **payment** - 订单支付表
   - 支付记录
   - 支持支付宝、微信支付

6. **review** - 服务评价表
   - 服务评价信息
   - 包含多维度评分

### 4.3 运营管理实体表

1. **company** - 公司信息表
   - 公司基本信息

2. **schedule** - 员工排班表
   - 服务人员的排班信息

### 系统支撑模块表

1. **message** - 站内信表
   - 用户消息通知

2. **sms_record** - 短信发送记录表
   - 短信发送记录

3. **email_record** - 邮件发送记录表
   - 邮件发送记录

4. **file_info** - 文件信息表
   - 文件管理

5. **operation_log** - 业务操作日志表
   - 业务操作日志

6. **access_log** - 访问日志表
   - 访问日志

7. **system_config** - 系统配置表
   - 系统配置信息

## 注意事项

1. **字符集**：所有表使用 `utf8mb4` 字符集，支持emoji等特殊字符
2. **外键约束**：部分表设置了外键约束，删除时需要注意级联关系
3. **索引**：已为常用查询字段创建索引，提升查询性能
4. **默认数据**：schema.sql中包含了一些初始化数据（管理员用户、服务类型等）

## 默认管理员账号

- 用户名：admin
- 密码：admin123（需要在实际使用时修改为加密后的密码）
- 手机号：13800138000

**注意**：生产环境部署前，请务必修改默认管理员密码！

## 数据库维护建议

1. **定期备份**：建议每天备份数据库
2. **日志清理**：定期清理 `operation_log` 和 `access_log` 表中的历史数据
3. **索引优化**：根据实际查询情况调整索引
4. **性能监控**：监控慢查询，优化SQL语句

