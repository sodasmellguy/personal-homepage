#!/bin/bash
# ============================================================
#  服务名称：grzy-backend
#  服务路径：/data/www/grzy/grzy-backend-1.0.0.jar
#  日志路径：/data/www/grzy/logs/grzy-backend.log
# ============================================================

# ---------- 基本配置 ----------
APP_NAME="grzy-backend"
APP_HOME="/data/www/grzy"
JAR_FILE="${APP_HOME}/${APP_NAME}-1.0.0.jar"
LOG_DIR="${APP_HOME}/logs"
LOG_FILE="${LOG_DIR}/${APP_NAME}.log"
PID_FILE="${APP_HOME}/${APP_NAME}.pid"

# ---------- 敏感配置环境变量 ----------
# 密钥不写在本脚本里（脚本会进 Git），统一放在服务器本地 env 文件中：
#   /data/www/grzy/grzy.env 内容格式：
#     DB_PASSWORD=xxx
#     REDIS_PASSWORD=xxx
#     MAIL_PASSWORD=xxx
# 文件权限应为 600（chmod 600 /data/www/grzy/grzy.env）
ENV_FILE="/data/www/grzy/grzy.env"
REQUIRED_ENV_VARS="DB_PASSWORD REDIS_PASSWORD MAIL_PASSWORD"

# ---------- JVM 参数（根据服务器内存调整） ----------
JAVA_OPTS="-Xms512m -Xmx1024m -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m"

# ---------- Spring 参数 ----------
# 可以在这里切换环境，如 dev / master
SPRING_OPTS="--spring.profiles.active=master"


# ============================================================
#  以下为功能函数，一般不需要修改
# ============================================================

# 确保日志目录存在
mkdir -p "${LOG_DIR}"

# 获取 PID
get_pid() {
    if [ -f "${PID_FILE}" ]; then
        local pid
        pid=$(cat "${PID_FILE}")
        if [ -n "${pid}" ] && kill -0 "${pid}" 2>/dev/null; then
            echo "${pid}"
            return 0
        fi
    fi
    return 1
}

# 加载敏感配置环境变量（DB_PASSWORD / REDIS_PASSWORD / MAIL_PASSWORD）
load_env() {
    if [ ! -f "${ENV_FILE}" ]; then
        echo "[${APP_NAME}] 错误: 环境变量文件不存在: ${ENV_FILE}"
        echo "[${APP_NAME}] 请创建该文件并写入 DB_PASSWORD / REDIS_PASSWORD / MAIL_PASSWORD，"
        echo "[${APP_NAME}] 然后执行: chmod 600 ${ENV_FILE}"
        return 1
    fi

    if [ "$(stat -c '%a' "${ENV_FILE}")" != "600" ]; then
        echo "[${APP_NAME}] 警告: ${ENV_FILE} 权限不是 600，自动修正 ..."
        chmod 600 "${ENV_FILE}"
    fi
    # set -a 使 source 进来的变量自动导出为子进程可见的环境变量
    set -a
    # shellcheck disable=SC1090
    source "${ENV_FILE}"
    set +a
    # 校验必需变量是否齐全
    for var in ${REQUIRED_ENV_VARS}; do
        if [ -z "$(eval echo \$$var)" ]; then
            echo "[${APP_NAME}] 错误: ${ENV_FILE} 中缺少必需变量: ${var}"
            return 1
        fi
    done
    return 0
}

# ---------- 启动 ----------
start() {
    local pid
    if pid=$(get_pid); then
        echo "[${APP_NAME}] 已经在运行中，PID: ${pid}"
        return 1
    fi

    # 启动前加载环境变量，缺失则拒绝启动（避免密码未注入时静默失败）
    if ! load_env; then
        return 1
    fi

    echo "=========================================="
    echo "  正在启动 ${APP_NAME} ..."
    echo "  JAR:  ${JAR_FILE}"
    echo "  日志: ${LOG_FILE}"
    echo "=========================================="

    # 后台启动，日志输出到文件
    nohup java ${JAVA_OPTS} -jar "${JAR_FILE}" ${SPRING_OPTS} \
        > "${LOG_FILE}" 2>&1 &

    # 保存 PID
    echo $! > "${PID_FILE}"

    # 等几秒确认启动
    sleep 3
    if pid=$(get_pid); then
        echo "[${APP_NAME}] 启动成功，PID: ${pid}"
        echo "[${APP_NAME}] 日志查看: tail -f ${LOG_FILE}"
    else
        echo "[${APP_NAME}] 启动失败，请查看日志: ${LOG_FILE}"
        return 1
    fi
}

# ---------- 停止 ----------
stop() {
    local pid
    if ! pid=$(get_pid); then
        echo "[${APP_NAME}] 未在运行"
        return 1
    fi

    echo "[${APP_NAME}] 正在停止，PID: ${pid} ..."

    # 先优雅关闭
    kill "${pid}" 2>/dev/null

    # 等待进程退出，最多等 30 秒
    local count=0
    while kill -0 "${pid}" 2>/dev/null; do
        sleep 1
        count=$((count + 1))
        if [ ${count} -ge 30 ]; then
            echo "[${APP_NAME}] 优雅停止超时，强制终止 ..."
            kill -9 "${pid}" 2>/dev/null
            break
        fi
    done

    rm -f "${PID_FILE}"
    echo "[${APP_NAME}] 已停止"
}

# ---------- 重启 ----------
restart() {
    stop
    sleep 2
    start
}

# ---------- 查看状态 ----------
status() {
    local pid
    if pid=$(get_pid); then
        echo "[${APP_NAME}] 运行中，PID: ${pid}"
        echo ""
        echo "---------- 进程信息 ----------"
        ps -p "${pid}" -o pid,ppid,%cpu,%mem,etime,cmd --no-headers
        echo ""
        echo "---------- 内存信息 ----------"
        ps -p "${pid}" -o rss,vsz --no-headers | awk '{printf "RSS: %.1f MB  VSZ: %.1f MB\n", $1/1024, $2/1024}'
    else
        echo "[${APP_NAME}] 未在运行"
    fi
}

# ---------- 查看日志 ----------
logs() {
    if [ -f "${LOG_FILE}" ]; then
        tail -f "${LOG_FILE}"
    else
        echo "[${APP_NAME}] 日志文件不存在: ${LOG_FILE}"
    fi
}

# ---------- 主入口 ----------
case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    logs)
        logs
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status|logs}"
        echo ""
        echo "  start    - 启动服务"
        echo "  stop     - 停止服务"
        echo "  restart  - 重启服务"
        echo "  status   - 查看运行状态"
        echo "  logs     - 实时查看日志"
        exit 1
        ;;
esac

exit 0