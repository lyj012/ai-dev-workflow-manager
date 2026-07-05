# 后端说明

## 当前状态

当前后端已创建 Spring Boot MVP 骨架，并已实现主链路基础模块：

- `task`：任务创建、分页查询、详情查询。
- `template`：内置 workflow 模板初始化、模板列表和详情查询。
- `matching`：根据任务类型、复杂度和风险标签匹配 workflow 模板。
- `stage`：任务阶段初始化、阶段状态推进、阶段输出回填。
- `prompt`：阶段 Prompt 生成。
- `delivery`：测试清单、交付总结、Markdown 导出和交付预览。

## 技术栈

- Spring Boot
- MyBatis-Plus
- MySQL
- Knife4j

## 本地启动准备

启动服务前需要准备 MySQL：

1. 创建数据库 `ai_dev_workflow_manager`。
2. 执行 `../sql/schema.sql`。
3. 配置数据库环境变量：

```bash
set DB_URL=jdbc:mysql://localhost:3306/ai_dev_workflow_manager?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
set DB_USERNAME=root
set DB_PASSWORD=your_password
```

后端默认端口为 `8081`，前端 Vite 代理会将 `/api` 转发到 `http://localhost:8081`。

## 本地验证

```bash
mvn test
```
