# 贡献指南

欢迎参与 personal-homepage 个人主页项目的贡献！以下是参与方式。

---

## 提交 Issue

如果你发现了 Bug 或有功能建议，欢迎提交 Issue：

1. **Bug 反馈**：请描述复现步骤、期望行为、实际行为，并附上截图
2. **功能建议**：请描述使用场景和期望效果

提交前请先搜索是否已有类似的 Issue，避免重复。

---

## 提交 Pull Request

1. **Fork** 本仓库到你的账号
2. **创建分支**：从 `master` 切出新分支
   ```bash
   git checkout -b feature/your-feature
   ```
3. **开发**：完成代码编写，确保本地能正常编译运行
4. **提交**：使用清晰的 commit message
   ```bash
   git commit -m "feat: 新增 XXX 功能"
   ```
5. **推送**：推送到你的 Fork 仓库
   ```bash
   git push origin feature/your-feature
   ```
6. **发起 PR**：在 Gitee/GitHub 上发起 Pull Request，描述修改内容

### Commit 规范

| 前缀 | 用途 |
|------|------|
| `feat:` | 新功能 |
| `fix:` | 修复 Bug |
| `docs:` | 文档变更 |
| `style:` | 代码格式（不影响功能） |
| `refactor:` | 重构（非新功能、非修复） |
| `perf:` | 性能优化 |
| `chore:` | 构建/工具链变更 |

---

## 代码规范

### 后端（Java）

- 遵循 [阿里巴巴 Java 开发手册](https://github.com/alibaba/p3c) 规范
- 类名使用大驼峰命名，方法名使用小驼峰命名
- 每个类和方法都需要 Javadoc 注释，标注 `@author`
- Controller 层只做参数校验和转发，业务逻辑放在 Service 层

### 前端（Vue / TypeScript）

- 使用 Composition API + `<script setup>` 语法
- 组件名使用大驼峰命名，文件名与组件名一致
- 公共样式使用 Tailwind CSS 优先，自定义样式写在 `styles/` 目录
- API 请求统一通过 `utils/request.ts` 封装的方法发起

---

## 分支管理

| 分支 | 用途 |
|------|------|
| `master` | 主分支，稳定可发布版本 |
| `feature/*` | 功能开发分支 |
| `fix/*` | Bug 修复分支 |

PR 合入 `master` 前会进行 Code Review。

---

## 环境搭建

参考 [README.md](./README.md) 的「快速开始」章节和 [docs/notice.md](./docs/notice.md) 的部署注意事项。

---

感谢你的贡献！
