# 学生宿舍管理系统（毕业设计）

前后端分离架构 · Spring Boot 3 + Vue 3

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2 |
| 安全认证 | Spring Security + JWT (jjwt) |
| 数据层   | Spring Data JPA + Hibernate |
| 数据库   | MySQL 8.0 |
| 前端框架 | Vue 3 (Composition API) |
| 构建工具 | Vite 5 |
| 状态管理 | Pinia |
| UI 组件库 | Element Plus 2 |
| 图表     | ECharts 5 |
| HTTP 客户端 | Axios |

---

## 功能模块

- **数据看板** — 实时统计卡片 + ECharts 图表
- **楼栋管理** — 增删改查，支持宿管分配
- **房间管理** — 分楼栋/状态筛选，床位占用可视化，房间详情抽屉
- **学生管理** — 入住/退宿/调换宿舍全流程
- **报修管理** — 工单提交→处理→解决流转，优先级标记
- **访客管理** — 进出登记，到访/离开状态追踪
- **水电费管理** — 自动计算水电费，缴费状态管理
- **公告管理** — 发布/置顶/分类公告
- **用户管理** — 系统账号与角色权限（管理员专属）

---

## 角色权限

| 角色 | 权限说明 |
|------|---------|
| ADMIN（管理员）| 全部功能 + 用户管理 + 楼栋删除 |
| MANAGER（宿管）| 楼栋/房间/学生/报修/访客/费用/公告管理 |
| STUDENT（学生）| 查看宿舍信息、提交报修、查看公告 |

---

## 快速启动

### 前置条件

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Node.js 18+

### 1. 数据库准备

```sql
CREATE DATABASE dorm_system DEFAULT CHARACTER SET utf8mb4;
```

### 2. 修改数据库配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dorm_system?...
    username: root
    password: 你的MySQL密码   # ← 修改这里
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
# 服务启动在 http://localhost:8080/api
# 首次启动自动建表 + 写入测试数据
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

---

## 测试账号

| 账号 | 密码 | 角色 |
|------|------|------|
| admin    | admin123   | 管理员 |
| manager1 | manager123 | 宿管   |
| S2021001 | student123 | 学生   |

---

## 项目结构

```
dorm-system/
├── backend/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/dorm/
│       ├── DormApplication.java      # 启动类
│       ├── config/
│       │   ├── SecurityConfig.java   # Security + CORS 配置
│       │   ├── JwtAuthFilter.java    # JWT 过滤器
│       │   └── GlobalExceptionHandler.java
│       ├── entity/                   # JPA 实体（7个）
│       ├── repository/               # Spring Data 仓库（7个）
│       ├── service/                  # 业务逻辑层（9个）
│       ├── controller/               # REST 控制器（9个）
│       ├── dto/                      # 数据传输对象
│       └── util/JwtUtil.java
│
└── frontend/                         # Vue 3 前端
    ├── vite.config.js
    └── src/
        ├── api/           # Axios 封装 + 接口定义
        ├── router/        # Vue Router 路由配置
        ├── stores/        # Pinia 状态管理
        ├── components/    # 公共组件
        └── views/         # 页面组件（9个页面）
            ├── Login.vue
            ├── Layout.vue
            ├── Dashboard.vue
            ├── Buildings.vue
            ├── Rooms.vue
            ├── Students.vue
            ├── Repairs.vue
            ├── Visitors.vue
            ├── Fees.vue
            ├── Notices.vue
            └── Users.vue
```

---

## API 接口文档（简要）

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | /api/auth/login | 登录 |
| 认证 | POST | /api/auth/change-password | 修改密码 |
| 统计 | GET  | /api/stats/overview | 首页统计 |
| 楼栋 | GET/POST/PUT/DELETE | /api/buildings | 楼栋 CRUD |
| 房间 | GET/POST/PUT | /api/rooms | 房间管理 |
| 学生 | GET/POST/PUT | /api/students | 学生管理 |
| 学生 | POST | /api/students/{id}/checkout | 退宿 |
| 学生 | POST | /api/students/{id}/transfer | 调宿 |
| 报修 | GET/POST/PUT | /api/repairs | 报修工单 |
| 访客 | GET/POST/PUT | /api/visitors | 访客登记 |
| 费用 | GET/POST | /api/fees | 水电账单 |
| 费用 | POST | /api/fees/{id}/pay | 标记缴费 |
| 公告 | GET/POST/PUT/DELETE | /api/notices | 公告管理 |
| 用户 | GET/POST/PUT | /api/users | 用户管理（管理员） |

所有接口（除 `/auth/login` 和公告查询）均需在请求头携带：
```
Authorization: Bearer <token>
```

---

## 毕设说明

本系统实现了完整的前后端分离架构：
- 后端遵循 **Controller → Service → Repository** 三层架构
- JWT 无状态鉴权，角色权限基于 Spring Security `@PreAuthorize` / 路由规则控制
- 前端 Axios 请求拦截器自动注入 Token，响应拦截器统一处理 401/403
- Vue Router 守卫实现前端路由鉴权
- Pinia 管理全局登录状态

---

*学生宿舍管理系统 · 毕业设计 2024*
