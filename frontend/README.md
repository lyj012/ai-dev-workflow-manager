# 前端说明

本目录是 `ai-dev-workflow-manager` 的 Vue 3 + TypeScript + Vite 前端项目。

## 技术栈

- Vue 3
- TypeScript
- Vite
- Vue Router
- Element Plus
- Axios
- Pinia

当前前端已包含路由、布局、任务页、模板页、交付预览页、API 封装和 mock 数据骨架。

## 本地开发

```bash
npm install
npm run dev
```

`npm run dev` 会启动 Vite 开发服务，当前固定前端端口为：

```text
http://localhost:8080
```

默认通过 Vite 代理把 `/api` 转发到后端：

```text
http://localhost:8081
```

## 构建

```bash
npm run build
```

## 当前范围

当前前端覆盖 MVP 主链路页面：

- `/tasks`：任务列表、筛选、进入详情。
- `/tasks/create`：任务创建。
- `/tasks/:taskId`：任务详情、workflow 匹配入口、阶段初始化、阶段操作入口、Prompt 生成、阶段输出回填、测试清单、交付总结、Markdown 预览。
- `/templates`：Workflow 模板列表。
- `/templates/:templateId`：Workflow 模板详情。
- `/delivery/:taskId`：交付记录和 Markdown 预览。

## Mock 与真实接口切换

前端通过 `VITE_USE_MOCK` 控制是否使用 mock 数据：

- 默认不启用 mock，页面请求真实后端接口。
- 设置 `VITE_USE_MOCK=true` 时启用 mock 数据。

示例：

```bash
set VITE_USE_MOCK=true
npm run dev
```

真实联调时不要设置 `VITE_USE_MOCK=true`，或显式设置为 `false`。
