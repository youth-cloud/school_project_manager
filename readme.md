# 学校实训管理平台

一个面向校园实训场景的前后端分离管理系统，覆盖课题发布、选题申请、建组申请、正式项目组、阶段任务、阶段提交、周报、答辩、成绩、用户管理等核心流程。

## 项目特点
- 具备 `ADMIN / TEACHER / STUDENT` 三类角色和对应的数据访问边界
- 已完成项目组、选题、提交、附件、评审、答辩、成绩等主流程闭环
- 已完成主要业务页的产品化整理、名称化展示、分页、详情弹窗和乱码清理
- 已具备本地运行、GitHub Actions 自动检查的基础能力

## 技术栈

### 前端
- `Vue 3`
- `TypeScript`
- `Vite`
- `Element Plus`
- `Pinia`
- `Vue Router`
- `Axios`
- `Sass`

### 后端
- `Spring Boot 4`
- `Spring Security`
- `JWT`
- `MyBatis-Plus`
- `MySQL 8`
- `Redis 7`
- `Springdoc OpenAPI`

## 主要功能
- 认证与账号：登录、获取当前用户、修改密码、管理员重置密码、刷新 token、退出登录
- 基础数据：课程管理、班级管理、实训批次管理、用户管理、个人信息维护
- 课题流程：课题管理、选题申请、审批、锁题约束
- 建组流程：建组申请、教师审核、自动生成正式项目组、成员加入与退出
- 过程管理：阶段任务、阶段提交、提交附件、周报、评审记录
- 结果管理：答辩安排、答辩记录、成绩记录
- 运维辅助：公告管理、操作日志

## 当前状态
- 当前阶段：`MVP 可交付版本`
- 前端最近一次 `npm run build` 已通过
- 后端已完成多轮 `./mvnw -DskipTests package` 验证

## 目录结构

```text
school_project_manager/
├─ backend/                  # Spring Boot 后端
├─ frontend/                 # Vue 3 前端
├─ docker-compose.yml        # Docker Compose 部署文件
├─ .env.example              # 部署环境变量示例
├─ 部署说明.md               # 部署文档
├─ schoolprojectmanager.sql  # 纯净库表结构
└─ schoolprojectmanagerwithdata.sql  # 本地数据导出，不纳入版本控制
```

## 本地开发

### 1. 环境要求
- `Node.js`：建议 `22` 或更高
- `npm`：跟随 Node 安装
- `JDK`：`25`
- `MySQL`：`8.x`
- `Redis`：`7.x`

### 2. 启动后端

```powershell
cd backend
Copy-Item .\src\main\resources\application-dev.yaml.example .\src\main\resources\application-dev.yaml
$env:DB_PASSWORD="替换为本机数据库密码"
.\mvnw.cmd spring-boot:run
```

首次从仓库下载时复制开发配置模板；已有本地配置无需再次复制。实际的 `application-dev.yaml` 已忽略，请通过环境变量或本地配置保存连接信息。项目根目录的 `.env` 用于 Docker Compose，直接运行 Maven 时不会自动加载它。

或先打包再运行：

```powershell
cd backend
.\mvnw.cmd -DskipTests package
java -jar .\target\backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 3. 启动前端

```powershell
cd frontend
npm ci
npm run dev
```

### 4. 前端构建

```powershell
cd frontend
npm run build
```

如果需要部署到路径前缀，例如 `/manager/`，可在构建时指定：

```powershell
$env:VITE_APP_BASE="/manager/"
npm run build
```

## 数据库初始化
- 纯净环境：使用 `schoolprojectmanager.sql`
- 本地演示环境：可使用自行准备并脱敏的 `schoolprojectmanagerwithdata.sql`；该文件可能包含账号与业务记录，已忽略，仓库不提供数据导出
- Docker Compose 首次启动时默认会自动导入 `schoolprojectmanager.sql`

## Docker 部署

### 1. 准备环境变量

```powershell
copy .env.example .env
```

至少应修改：
- `MYSQL_ROOT_PASSWORD`
- `DB_PASSWORD`
- `JWT_SECRET`
- `APP_CORS_ALLOWED_ORIGINS`

### 2. 启动服务

```powershell
docker compose up -d --build
```

### 3. 默认访问
- 前端：`http://localhost/`
- 后端接口：`http://localhost/api/...`
- 上传文件：`http://localhost/uploads/...`

更详细的部署说明请看 [部署说明.md](./部署说明.md)。


## 相关文档
- [部署说明.md](./部署说明.md)
- [计划书.md](./计划书.md)
