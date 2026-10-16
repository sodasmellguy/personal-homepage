# personal-homepage 个人主页 - 部署注意事项

> 本文档列出项目部署前**必须修改**的所有配置项，请逐项核对后再上线。

---

## 1. 替换邮箱推送账号

项目中多处使用了占位邮箱 `youxiangtuisongzhnaghao@qq.com`，部署前需全局搜索并替换为你自己的 QQ 邮箱。

**涉及文件：**

| 文件 | 位置 | 说明 |
|------|------|------|
| `grzy-backend/src/main/resources/application-dev.yml` | `spring.mail.username` 和 `app.admin-email` | 开发环境邮箱配置 |
| `grzy-backend/src/main/resources/application-master.yml` | `spring.mail.username` 和 `app.admin-email` | 生产环境邮箱配置 |
| `grzy-backend/src/main/java/com/example/grzy/common/config/Knife4jConfig.java` | `Contact.email(...)` | API 文档联系方式 |

**操作方式：** 全局搜索 `youxiangtuisongzhnaghao@qq.com`，替换成你自己的邮箱地址。

---

## 2. 替换建站日期

文件：`grzy-backend/src/main/resources/application.yml`

```yaml
site:
  # 建站日期，用于计算运行天数
  build-date: 2024-11-04
```

将 `2024-11-04` 修改为你网站实际开始运行的日期，格式为 `YYYY-MM-DD`。

---

## 3. 系统数据库文件

文件：`grzy-backend/src/main/resources/db/b_tech_stack.sql`

此文件包含建表语句和初始数据。导入数据库后，建议根据实际情况修改以下内容：

- `tb_profile` 表中的个人信息（姓名、学校、专业、邮箱、QQ 号等）
- `sys_user` 表中的管理员账号
- 默认密码哈希值

---

## 4. 替换数据库密码

文件：`grzy-backend/src/main/resources/application-dev.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/personal-homepage?...
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}
```

| 配置项 | 当前默认值 | 说明 |
|--------|-----------|------|
| 数据库名 | `personal-homepage` | 根据实际情况修改 |
| 账号 | `root` | 建议使用独立账号 |
| 密码 | `123456` | **务必修改为强密码** |

> 同一文件中 Druid 监控面板的 `login-password` 默认值也为 `123456`，请一并修改。

---

## 5. 生产环境 CORS 域名

文件：`grzy-backend/src/main/resources/application-master.yml`

```yaml
cors:
  allowed-origins: https://www.ceshi.com

app:
  site-url: ${SITE_URL:https://www.ceshi.com}
```

将 `https://www.ceshi.com` 替换为你自己的域名地址。

---

## 6. 线上环境部署

### 前端

```bash
cd grzy_front
npm install
npm run build
```

将 `dist` 目录部署到 Nginx，配置反向代理即可。

### 后端

**步骤一：配置环境变量文件**

文件：`docs/grzy.env`

```bash
DB_PASSWORD=XXXXX       # 数据库密码
REDIS_PASSWORD=XXXX     # Redis 密码
MAIL_PASSWORD=XXXX      # QQ 邮箱 SMTP 授权码
```

> 将以上 `XXXX` 替换为你自己的真实密码/授权码。

**步骤二：修改部署脚本**

文件：`docs/grzy.sh`

将脚本中 `/data/www/grzy/` 路径替换为你自己服务器的实际路径。确保 `grzy.sh`、构建的 jar 包、`grzy.env` 三个文件放在同一目录下。

**步骤三：启动服务**

```bash
chmod +x grzy.sh
./grzy.sh start
```

---

## 7. 默认管理员账号

| 账号 | 密码 | 角色 |
|------|------|------|
| `admin` | `admin123` | 超级管理员 |

> **安全提示：** 首次登录后请立即修改密码！

---

## ⚠️ 隐私安全检查清单

部署前请确认以下敏感信息已全部替换：

- [ ] 邮箱推送账号 `youxiangtuisongzhnaghao@qq.com` → 替换为你的邮箱
- [ ] 数据库密码 `123456` → 替换为强密码
- [ ] Druid 监控面板密码 `123456` → 替换为强密码
- [ ] CORS 域名 `https://www.ceshi.com` → 替换为你的域名
- [ ] `grzy.env` 中的所有密码占位符 → 替换为真实密码
- [ ] `grzy.sh` 中的服务器路径 → 替换为你的实际路径
- [ ] `tb_profile` 表中的个人信息 → 替换为你的真实信息 线上可以登陆后直接修改个人信息
- [ ] 管理员默认密码 `admin123` → 首次登录后立即修改
