/*
 Navicat Premium Dump SQL

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 50744 (5.7.44-log)
 Source Host           : localhost:3306
 Source Schema         : personal-homepage

 Target Server Type    : MySQL
 Target Server Version : 50744 (5.7.44-log)
 File Encoding         : 65001

 Date: 17/10/2026 02:14:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for b_tech_stack
-- ----------------------------
DROP TABLE IF EXISTS `b_tech_stack`;
CREATE TABLE `b_tech_stack`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ame` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '技术名称',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标URL',
  `eatured` int(1) NULL DEFAULT 0 COMMENT '是否Featured',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` int(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '技术栈管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of b_tech_stack
-- ----------------------------
INSERT INTO `b_tech_stack` VALUES (1, 'Vue', '/tech-icons/vue.svg', 1, 1, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (2, 'React', '/tech-icons/react.svg', 1, 2, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (3, 'TypeScript', '/tech-icons/typescript.svg', 1, 3, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (4, 'Java', '/tech-icons/java.svg', 1, 4, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (5, 'Spring Boot', '/tech-icons/spring.svg', 1, 5, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (6, 'MySQL', '/tech-icons/mysql.svg', 0, 6, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (7, 'Redis', '/tech-icons/redis.svg', 0, 7, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (8, 'Docker', '/tech-icons/docker.svg', 0, 8, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (9, 'Git', '/tech-icons/git.svg', 0, 9, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);
INSERT INTO `b_tech_stack` VALUES (10, 'Linux', '/tech-icons/linux.svg', 0, 10, 1, '2026-06-18 23:42:14', '2026-06-18 23:42:14', 0);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父菜单ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `type` tinyint(4) NULL DEFAULT 0 COMMENT '类型：0-目录，1-菜单，2-按钮',
  `permission` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 443 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 0, '仪表盘', '/admin/dashboard', 'admin/Dashboard', 'DashboardOutlined', 1, 1, 'system:dashboard:view', 1, '2026-06-14 01:18:52', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (2, 0, '内容管理', NULL, NULL, 'EditOutlined', 2, 0, NULL, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (3, 0, '系统管理', NULL, NULL, 'SettingOutlined', 3, 0, NULL, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (4, 0, '发布管理', NULL, NULL, 'ReadOutlined', 4, 0, NULL, 1, '2026-06-14 03:00:00', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (5, 3, '角色管理', '/admin/role', 'admin/RoleManage', 'SafetyOutlined', 2, 1, 'system:role:view', 1, '2026-06-18 00:13:48', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (21, 2, '个人信息管理', '/admin/profile', 'admin/ProfileManage', 'UserOutlined', 1, 1, 'content:profile:view', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (22, 2, '技能管理', '/admin/skill', 'admin/SkillManage', 'ThunderboltOutlined', 2, 1, 'content:skill:view', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (23, 2, '项目管理', '/admin/project', 'admin/ProjectManage', 'ProjectOutlined', 4, 1, 'content:project:view', 1, '2026-06-14 01:18:52', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (24, 2, '实习管理', '/admin/internship', 'admin/InternshipManage', 'SolutionOutlined', 5, 1, 'content:internship:view', 1, '2026-06-14 01:18:52', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (25, 2, '获奖管理', '/admin/award', 'admin/AwardManage', 'TrophyOutlined', 6, 1, 'content:award:view', 1, '2026-06-14 01:18:52', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (31, 3, '用户管理', '/admin/user', 'admin/UserManage', 'TeamOutlined', 1, 1, 'system:user:view', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (41, 4, '文章管理', '/admin/article', 'admin/ArticleManage', 'FileTextOutlined', 1, 1, 'blog:article:view', 1, '2026-06-14 03:00:00', '2026-06-14 03:00:00', 0);
INSERT INTO `sys_menu` VALUES (42, 4, '评论管理', '/admin/comment', 'admin/CommentManage', 'CommentOutlined', 2, 1, 'blog:comment:view', 1, '2026-06-14 03:00:00', '2026-06-14 03:00:00', 0);
INSERT INTO `sys_menu` VALUES (43, 4, '专栏管理', '/admin/series', 'admin/SeriesManage', 'BookOutlined', 3, 1, 'blog:series:view', 1, '2026-06-19 00:00:00', '2026-06-19 00:00:00', 0);
INSERT INTO `sys_menu` VALUES (211, 21, '个人信息编辑', NULL, NULL, NULL, 1, 2, 'content:profile:edit', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (221, 22, '技能新增', NULL, NULL, NULL, 1, 2, 'content:skill:add', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (222, 22, '技能编辑', NULL, NULL, NULL, 2, 2, 'content:skill:edit', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (223, 22, '技能删除', NULL, NULL, NULL, 3, 2, 'content:skill:delete', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (231, 23, '项目新增', NULL, NULL, NULL, 1, 2, 'content:project:add', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (232, 23, '项目编辑', NULL, NULL, NULL, 2, 2, 'content:project:edit', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (233, 23, '项目删除', NULL, NULL, NULL, 3, 2, 'content:project:delete', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (241, 24, '实习新增', NULL, NULL, NULL, 1, 2, 'content:internship:add', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (242, 24, '实习编辑', NULL, NULL, NULL, 2, 2, 'content:internship:edit', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (243, 24, '实习删除', NULL, NULL, NULL, 3, 2, 'content:internship:delete', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (251, 25, '获奖新增', NULL, NULL, NULL, 1, 2, 'content:award:add', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (252, 25, '获奖编辑', NULL, NULL, NULL, 2, 2, 'content:award:edit', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (253, 25, '获奖删除', NULL, NULL, NULL, 3, 2, 'content:award:delete', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (311, 31, '用户新增', NULL, NULL, NULL, 1, 2, 'system:user:add', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (312, 31, '用户编辑', NULL, NULL, NULL, 2, 2, 'system:user:edit', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (313, 31, '用户删除', NULL, NULL, NULL, 3, 2, 'system:user:delete', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (314, 31, '重置密码', NULL, NULL, NULL, 4, 2, 'system:user:reset-password', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_menu` VALUES (411, 41, '文章新增', NULL, NULL, NULL, 1, 2, 'blog:article:add', 1, '2026-06-14 03:00:00', '2026-06-14 03:00:00', 0);
INSERT INTO `sys_menu` VALUES (412, 41, '文章编辑', NULL, NULL, NULL, 2, 2, 'blog:article:edit', 1, '2026-06-14 03:00:00', '2026-06-14 03:00:00', 0);
INSERT INTO `sys_menu` VALUES (413, 41, '文章删除', NULL, NULL, NULL, 3, 2, 'blog:article:delete', 1, '2026-06-14 03:00:00', '2026-06-14 03:00:00', 0);
INSERT INTO `sys_menu` VALUES (421, 42, '评论审核', NULL, NULL, NULL, 1, 2, 'blog:comment:audit', 1, '2026-06-14 03:00:00', '2026-06-14 03:00:00', 0);
INSERT INTO `sys_menu` VALUES (422, 42, '评论删除', NULL, NULL, NULL, 2, 2, 'blog:comment:delete', 1, '2026-06-14 03:00:00', '2026-06-14 03:00:00', 0);
INSERT INTO `sys_menu` VALUES (431, 43, '专栏新增', NULL, NULL, NULL, 1, 2, 'blog:series:add', 1, '2026-06-19 00:00:00', '2026-06-19 00:00:00', 0);
INSERT INTO `sys_menu` VALUES (432, 43, '专栏编辑', NULL, NULL, NULL, 2, 2, 'blog:series:edit', 1, '2026-06-19 00:00:00', '2026-06-19 00:00:00', 0);
INSERT INTO `sys_menu` VALUES (433, 43, '专栏删除', NULL, NULL, NULL, 3, 2, 'blog:series:delete', 1, '2026-06-19 00:00:00', '2026-06-19 00:00:00', 0);
INSERT INTO `sys_menu` VALUES (434, 2, '技术栈管理', '/admin/techstack', 'admin/TechStackManage', 'AppstoreOutlined', 3, 1, 'content:techstack:view', 1, '2026-08-31 23:44:46', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (435, 2, '相册管理', '/admin/album', 'admin/AlbumManage', 'CameraOutlined', 7, 1, 'content:album:view', 1, '2026-08-31 23:44:46', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (436, 4, '友链管理', '/admin/friendlink', 'admin/FriendLinkManage', 'LinkOutlined', 4, 1, 'blog:friendlink:view', 1, '2026-08-31 23:44:46', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (437, 3, '操作日志', '/admin/log', 'admin/OperLogManage', 'ClockCircleOutlined', 4, 1, 'system:log:view', 1, '2026-08-31 23:44:46', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (438, 3, '访客地图', '/admin/visitor-map', 'admin/VisitorMapManage', 'EnvironmentOutlined', 5, 1, 'system:visitor-map:view', 1, '2026-08-31 23:44:46', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (439, 3, 'API文档', '/admin/apidoc', 'admin/ApiDoc', 'FileSearchOutlined', 3, 1, 'system:apidoc:view', 1, '2026-08-31 23:44:46', '2026-08-31 23:44:46', 0);
INSERT INTO `sys_menu` VALUES (440, 3, '通知设置', '/admin/notify-setting', 'admin/NotifySettingManage', 'BellOutlined', 6, 1, 'system:notify:view', 1, '2026-09-04 00:55:32', '2026-09-04 00:55:32', 0);
INSERT INTO `sys_menu` VALUES (441, 3, '在线会话', '/admin/online-session', 'admin/OnlineSessionManage', 'LaptopOutlined', 7, 1, 'system:session:view', 1, '2026-09-04 00:55:32', '2026-09-04 00:55:32', 0);
INSERT INTO `sys_menu` VALUES (442, 2, '开源项目', '/admin/open-source', 'admin/OpenSourceManage', 'GithubOutlined', 8, 1, 'content:opensource:view', 1, '2026-09-04 00:55:32', '2026-09-04 00:55:32', 0);

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作模块',
  `operation` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作描述',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '请求参数',
  `response_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '响应数据',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户代理',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作用户名',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '操作状态：0-失败，1-成功',
  `error_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `duration` bigint(20) NULL DEFAULT NULL COMMENT '执行时长(ms)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (11, '个人信息管理', '更新了个人信息', NULL, NULL, NULL, NULL, NULL, NULL, 2, 'admin', 1, NULL, NULL, '2026-10-17 02:08:19');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', '超级管理员，拥有所有权限', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `sys_role` VALUES (2, '普通用户', 'user', '普通用户，只有查看权限', 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 1, 1);
INSERT INTO `sys_role_menu` VALUES (2, 1, 2);
INSERT INTO `sys_role_menu` VALUES (3, 1, 3);
INSERT INTO `sys_role_menu` VALUES (36, 1, 4);
INSERT INTO `sys_role_menu` VALUES (47, 1, 5);
INSERT INTO `sys_role_menu` VALUES (4, 1, 21);
INSERT INTO `sys_role_menu` VALUES (5, 1, 22);
INSERT INTO `sys_role_menu` VALUES (6, 1, 23);
INSERT INTO `sys_role_menu` VALUES (7, 1, 24);
INSERT INTO `sys_role_menu` VALUES (8, 1, 25);
INSERT INTO `sys_role_menu` VALUES (9, 1, 31);
INSERT INTO `sys_role_menu` VALUES (37, 1, 41);
INSERT INTO `sys_role_menu` VALUES (38, 1, 42);
INSERT INTO `sys_role_menu` VALUES (48, 1, 43);
INSERT INTO `sys_role_menu` VALUES (10, 1, 211);
INSERT INTO `sys_role_menu` VALUES (11, 1, 221);
INSERT INTO `sys_role_menu` VALUES (12, 1, 222);
INSERT INTO `sys_role_menu` VALUES (13, 1, 223);
INSERT INTO `sys_role_menu` VALUES (14, 1, 231);
INSERT INTO `sys_role_menu` VALUES (15, 1, 232);
INSERT INTO `sys_role_menu` VALUES (16, 1, 233);
INSERT INTO `sys_role_menu` VALUES (17, 1, 241);
INSERT INTO `sys_role_menu` VALUES (18, 1, 242);
INSERT INTO `sys_role_menu` VALUES (19, 1, 243);
INSERT INTO `sys_role_menu` VALUES (20, 1, 251);
INSERT INTO `sys_role_menu` VALUES (21, 1, 252);
INSERT INTO `sys_role_menu` VALUES (22, 1, 253);
INSERT INTO `sys_role_menu` VALUES (23, 1, 311);
INSERT INTO `sys_role_menu` VALUES (24, 1, 312);
INSERT INTO `sys_role_menu` VALUES (25, 1, 313);
INSERT INTO `sys_role_menu` VALUES (26, 1, 314);
INSERT INTO `sys_role_menu` VALUES (39, 1, 411);
INSERT INTO `sys_role_menu` VALUES (40, 1, 412);
INSERT INTO `sys_role_menu` VALUES (41, 1, 413);
INSERT INTO `sys_role_menu` VALUES (42, 1, 421);
INSERT INTO `sys_role_menu` VALUES (43, 1, 422);
INSERT INTO `sys_role_menu` VALUES (49, 1, 431);
INSERT INTO `sys_role_menu` VALUES (50, 1, 432);
INSERT INTO `sys_role_menu` VALUES (51, 1, 433);
INSERT INTO `sys_role_menu` VALUES (53, 1, 434);
INSERT INTO `sys_role_menu` VALUES (54, 1, 435);
INSERT INTO `sys_role_menu` VALUES (55, 1, 436);
INSERT INTO `sys_role_menu` VALUES (56, 1, 437);
INSERT INTO `sys_role_menu` VALUES (57, 1, 438);
INSERT INTO `sys_role_menu` VALUES (58, 1, 439);
INSERT INTO `sys_role_menu` VALUES (60, 1, 440);
INSERT INTO `sys_role_menu` VALUES (61, 1, 441);
INSERT INTO `sys_role_menu` VALUES (63, 1, 442);
INSERT INTO `sys_role_menu` VALUES (27, 2, 1);
INSERT INTO `sys_role_menu` VALUES (28, 2, 2);
INSERT INTO `sys_role_menu` VALUES (29, 2, 3);
INSERT INTO `sys_role_menu` VALUES (44, 2, 4);
INSERT INTO `sys_role_menu` VALUES (30, 2, 21);
INSERT INTO `sys_role_menu` VALUES (31, 2, 22);
INSERT INTO `sys_role_menu` VALUES (32, 2, 23);
INSERT INTO `sys_role_menu` VALUES (33, 2, 24);
INSERT INTO `sys_role_menu` VALUES (34, 2, 25);
INSERT INTO `sys_role_menu` VALUES (35, 2, 31);
INSERT INTO `sys_role_menu` VALUES (45, 2, 41);
INSERT INTO `sys_role_menu` VALUES (46, 2, 42);
INSERT INTO `sys_role_menu` VALUES (52, 2, 43);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (2, 'admin', '$2a$10$BBZU8ARhDZ/fk0lFU34P1Ovc4Cp1Vy2j/nvTeRaIeNM1nJw4TtpFG', '管理员', NULL, NULL, NULL, 1, '2026-10-17 01:54:56', '2026-10-17 01:54:56', 0);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (7, 2, 1);
INSERT INTO `sys_user_role` VALUES (8, 2, 2);

-- ----------------------------
-- Table structure for tb_album_group
-- ----------------------------
DROP TABLE IF EXISTS `tb_album_group`;
CREATE TABLE `tb_album_group`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '分组名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组描述',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分组封面图',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '相册分组表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_album_group
-- ----------------------------

-- ----------------------------
-- Table structure for tb_album_photo
-- ----------------------------
DROP TABLE IF EXISTS `tb_album_photo`;
CREATE TABLE `tb_album_photo`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '照片标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '照片描述/故事',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片地址',
  `group_id` bigint(20) NULL DEFAULT NULL COMMENT '所属分组ID（NULL为未分组）',
  `location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拍摄地点',
  `camera` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '相机型号（EXIF解析）',
  `aperture` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '光圈，如 f/2.8（EXIF解析）',
  `shutter` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '快门速度，如 1/250s（EXIF解析）',
  `iso` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '感光度，如 400（EXIF解析）',
  `focal_length` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '焦距，如 35mm（EXIF解析）',
  `shoot_date` date NULL DEFAULT NULL COMMENT '拍摄日期',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-隐藏，1-显示',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '相册照片表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_album_photo
-- ----------------------------

-- ----------------------------
-- Table structure for tb_article
-- ----------------------------
DROP TABLE IF EXISTS `tb_article`;
CREATE TABLE `tb_article`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文章标题',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文章摘要',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '文章内容',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类',
  `tags` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '标签(逗号分隔)',
  `view_count` int(11) NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞次数',
  `is_top` tinyint(4) NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-草稿，1-已发布，2-定时发布',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '定时发布时间（status=2时生效，到达时间后自动发布）',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_article
-- ----------------------------
INSERT INTO `tb_article` VALUES (1, 'Vue 3 Composition API 实战指南', '本文介绍 Vue 3 Composition API 的核心概念和实战技巧，帮助你更好地使用 Vue 3 开发应用。', '烦烦烦', NULL, '前端开发', 'Vue,JavaScript,前端', 136, 15, 1, 1, NULL, 1, '2026-06-14 02:00:00', '2026-10-17 02:03:13', 0);
INSERT INTO `tb_article` VALUES (2, 'Spring Boot 集成 Sa-Token 实现权限认证', '详细介绍如何在 Spring Boot 项目中集成 Sa-Token 实现用户认证和权限管理。', '# Spring Boot 集成 Sa-Token\n\n## Sa-Token 简介\n\nSa-Token 是一个轻量级 Java 权限认证框架...\n\n## 集成步骤\n\n### 1. 添加依赖\n\n```xml\n<dependency>\n    <groupId>cn.dev33</groupId>\n    <artifactId>sa-token-spring-boot3-starter</artifactId>\n    <version>1.38.0</version>\n</dependency>\n```\n\n### 2. 配置\n\n在 `application.yml` 中添加配置...', NULL, '后端开发', 'Java,SpringBoot,认证', 88, 12, 0, 1, NULL, 2, '2026-06-14 02:00:00', '2026-08-30 01:27:32', 0);
INSERT INTO `tb_article` VALUES (3, 'Docker 容器化部署实践', '分享 Docker 在实际项目中的容器化部署经验和最佳实践。', '2313', NULL, 'DevOps', 'Docker,部署,运维', 62, 8, 0, 1, NULL, 3, '2026-06-14 02:00:00', '2026-10-17 02:03:13', 0);

-- ----------------------------
-- Table structure for tb_article_daily_stats
-- ----------------------------
DROP TABLE IF EXISTS `tb_article_daily_stats`;
CREATE TABLE `tb_article_daily_stats`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `total_count` int(11) NOT NULL DEFAULT 0 COMMENT '截至当日累计阅读量快照',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '当日新增阅读量',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_date`(`article_id`, `stat_date`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章每日统计数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_article_daily_stats
-- ----------------------------
INSERT INTO `tb_article_daily_stats` VALUES (16, 1, '2026-10-17', 136, 0, '2026-10-17 02:05:19', '2026-10-17 02:05:19', 0);
INSERT INTO `tb_article_daily_stats` VALUES (17, 2, '2026-10-17', 88, 0, '2026-10-17 02:05:19', '2026-10-17 02:05:19', 0);
INSERT INTO `tb_article_daily_stats` VALUES (18, 3, '2026-10-17', 62, 0, '2026-10-17 02:05:20', '2026-10-17 02:05:20', 0);

-- ----------------------------
-- Table structure for tb_award
-- ----------------------------
DROP TABLE IF EXISTS `tb_award`;
CREATE TABLE `tb_award`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '奖项名称',
  `issuer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '颁发机构',
  `award_date` date NULL DEFAULT NULL COMMENT '获奖日期',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '奖项描述',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '获奖经历表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_award
-- ----------------------------
INSERT INTO `tb_award` VALUES (1, 'PMP项目管理', 'PMP项目管理', '2023-06-01', '', 1, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);

-- ----------------------------
-- Table structure for tb_collect
-- ----------------------------
DROP TABLE IF EXISTS `tb_collect`;
CREATE TABLE `tb_collect`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `visitor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '访客标识(IP+UA hash)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_visitor`(`article_id`, `visitor_id`) USING BTREE,
  UNIQUE INDEX `uk_collect_article_visitor`(`article_id`, `visitor_id`) USING BTREE,
  INDEX `idx_article_id`(`article_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章收藏记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_collect
-- ----------------------------

-- ----------------------------
-- Table structure for tb_comment
-- ----------------------------
DROP TABLE IF EXISTS `tb_comment`;
CREATE TABLE `tb_comment`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `parent_id` bigint(20) NULL DEFAULT 0 COMMENT '父评论ID(0为顶级评论)',
  `reply_to_id` bigint(20) NULL DEFAULT NULL COMMENT '回复的评论ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-待审核，1-已通过',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_article_id`(`article_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评论表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_comment
-- ----------------------------
INSERT INTO `tb_comment` VALUES (1, 1, 0, NULL, '张三', 'zhangsan@example.com', NULL, '写得很好，学到了很多！', NULL, 1, '2026-06-14 02:30:00', 0);
INSERT INTO `tb_comment` VALUES (2, 1, 1, 1, '李四', 'lisi@example.com', NULL, '同感，Composition API 确实比 Options API 更灵活。', NULL, 1, '2026-06-14 02:35:00', 0);
INSERT INTO `tb_comment` VALUES (3, 1, 0, NULL, '王五', 'wangwu@example.com', NULL, '请问有完整的示例代码吗？', NULL, 1, '2026-06-14 02:40:00', 0);
INSERT INTO `tb_comment` VALUES (4, 2, 0, NULL, '赵六', 'zhaoliu@example.com', NULL, 'Sa-Token 确实很好用，比 Spring Security 轻量多了。', NULL, 1, '2026-06-14 02:45:00', 0);
INSERT INTO `tb_comment` VALUES (5, 1, 0, NULL, '王五', '23123123', NULL, '231312321', NULL, 1, '2026-06-14 03:27:22', 1);

-- ----------------------------
-- Table structure for tb_friend_link
-- ----------------------------
DROP TABLE IF EXISTS `tb_friend_link`;
CREATE TABLE `tb_friend_link`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '站点名称',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '站点链接',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '站点图标/头像',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '站点描述',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已驳回',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '友情链接表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_friend_link
-- ----------------------------
INSERT INTO `tb_friend_link` VALUES (2, '21312', 'https://213123', '/uploads/2026/08/285170ced2ab4462b5529ef15d735240.png', '213123', 1, 0, '2026-08-30 01:20:10', '2026-08-30 01:20:23', 0);

-- ----------------------------
-- Table structure for tb_guestbook
-- ----------------------------
DROP TABLE IF EXISTS `tb_guestbook`;
CREATE TABLE `tb_guestbook`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '昵称',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '留言内容',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '#3b82f6' COMMENT '卡片颜色',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `like_count` int(11) NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '留言板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_guestbook
-- ----------------------------

-- ----------------------------
-- Table structure for tb_internship
-- ----------------------------
DROP TABLE IF EXISTS `tb_internship`;
CREATE TABLE `tb_internship`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '公司名称',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职位',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '工作描述',
  `achievements` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '工作成果',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '实习经历表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_internship
-- ----------------------------

-- ----------------------------
-- Table structure for tb_like
-- ----------------------------
DROP TABLE IF EXISTS `tb_like`;
CREATE TABLE `tb_like`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `visitor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '访客标识(IP+UA hash)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_article_visitor`(`article_id`, `visitor_id`) USING BTREE,
  INDEX `idx_article_id`(`article_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文章点赞记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_like
-- ----------------------------

-- ----------------------------
-- Table structure for tb_notify_log
-- ----------------------------
DROP TABLE IF EXISTS `tb_notify_log`;
CREATE TABLE `tb_notify_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '通知类型：comment-新评论, reply-评论回复, friendlink-友链申请, publish-定时发布, security-安全告警',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮件标题',
  `recipient` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '收件邮箱',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '发送状态：0-失败，1-成功',
  `error_msg` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '失败原因',
  `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_type`(`type`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮件通知日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_notify_log
-- ----------------------------

-- ----------------------------
-- Table structure for tb_notify_setting
-- ----------------------------
DROP TABLE IF EXISTS `tb_notify_setting`;
CREATE TABLE `tb_notify_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID（固定为1）',
  `admin_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '站长收件邮箱（空则使用 app.admin-email 配置）',
  `enable_comment_notify` tinyint(4) NOT NULL DEFAULT 1 COMMENT '新评论通知站长：0-关，1-开',
  `enable_reply_notify` tinyint(4) NOT NULL DEFAULT 1 COMMENT '评论回复通知访客：0-关，1-开',
  `enable_friendlink_notify` tinyint(4) NOT NULL DEFAULT 1 COMMENT '友链申请通知站长：0-关，1-开',
  `enable_publish_notify` tinyint(4) NOT NULL DEFAULT 1 COMMENT '定时发布结果通知：0-关，1-开',
  `enable_security_notify` tinyint(4) NOT NULL DEFAULT 1 COMMENT '安全告警通知：0-关，1-开',
  `alert_interval_minutes` int(11) NOT NULL DEFAULT 30 COMMENT '同类安全告警最小间隔（分钟），防止邮件轰炸',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邮件通知设置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_notify_setting
-- ----------------------------
INSERT INTO `tb_notify_setting` VALUES (1, NULL, 1, 1, 1, 1, 1, 30, '2026-10-17 02:06:45');

-- ----------------------------
-- Table structure for tb_open_source
-- ----------------------------
DROP TABLE IF EXISTS `tb_open_source`;
CREATE TABLE `tb_open_source`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '一句话简介',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '详细描述（选填）',
  `repo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '仓库地址（Gitee/GitHub）',
  `demo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '在线体验地址（选填）',
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目图标（选填，缺省用主语言色块+首字母）',
  `language` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '主语言（Java/Vue等，前台映射色点）',
  `tech_stack` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '技术栈标签，逗号分隔',
  `license` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '开源协议：MIT/Apache-2.0等',
  `stars` int(11) NOT NULL DEFAULT 0 COMMENT 'Star 数（手动填或同步覆盖）',
  `forks` int(11) NOT NULL DEFAULT 0 COMMENT 'Fork 数（手动填或同步覆盖）',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-已归档，1-维护中',
  `is_top` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序，越小越前',
  `sync_time` datetime NULL DEFAULT NULL COMMENT '最近同步时间（NULL=从未同步）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '开源项目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_open_source
-- ----------------------------

-- ----------------------------
-- Table structure for tb_profile
-- ----------------------------
DROP TABLE IF EXISTS `tb_profile`;
CREATE TABLE `tb_profile`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '姓名',
  `university` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学校',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专业',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `wechat` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '个人介绍',
  `career_goal` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '职业目标',
  `gitee_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Gitee地址',
  `github_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'GitHub地址',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '个人信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_profile
-- ----------------------------
INSERT INTO `tb_profile` VALUES (1, '赵先生', '野鸡大学', '测试专业', '123456789@qq.com', '123412512', NULL, '一名热爱技术、持续精进的全栈开发工程师。\n**技术能力：** 前端方面，熟练运用**Vue**、**React** 生态进行**Web**应用开发，同时具备**UniApp**跨端开发及**Flutter** 移动端开发能力，能够根据业务场景灵活选择最优技术方案；后端方面，深耕 **Spring Boot** / **Spring Cloud**体系，拥有从零搭建、架构设计到落地交付的完整项目经验。\n', '全栈开发工程师', 'https://gitee.com/', 'https://github.com/', '2026-06-14 01:18:52', '2026-06-15 01:06:49');

-- ----------------------------
-- Table structure for tb_project
-- ----------------------------
DROP TABLE IF EXISTS `tb_project`;
CREATE TABLE `tb_project`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目描述',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目截图（JSON数组）',
  `tech_stack` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '技术栈',
  `responsibilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '职责描述',
  `project_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目链接',
  `start_date` date NULL DEFAULT NULL COMMENT '开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '结束日期',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_project
-- ----------------------------

-- ----------------------------
-- Table structure for tb_series
-- ----------------------------
DROP TABLE IF EXISTS `tb_series`;
CREATE TABLE `tb_series`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '专栏名称',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '封面图',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '专栏描述',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '专栏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_series
-- ----------------------------

-- ----------------------------
-- Table structure for tb_series_article
-- ----------------------------
DROP TABLE IF EXISTS `tb_series_article`;
CREATE TABLE `tb_series_article`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `series_id` bigint(20) NOT NULL COMMENT '专栏ID',
  `article_id` bigint(20) NOT NULL COMMENT '文章ID',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_series_article`(`series_id`, `article_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '专栏文章关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_series_article
-- ----------------------------

-- ----------------------------
-- Table structure for tb_skill
-- ----------------------------
DROP TABLE IF EXISTS `tb_skill`;
CREATE TABLE `tb_skill`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '技能名称',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '分类',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(4) NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '技能表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_skill
-- ----------------------------
INSERT INTO `tb_skill` VALUES (1, 'Vue', '前端', NULL, 1, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (2, 'React', '前端', NULL, 2, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (3, 'TypeScript/JavaScript', '前端', NULL, 3, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (4, 'Flutter', '前端', NULL, 4, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (5, 'Uniapp', '前端', NULL, 5, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (6, 'TailwindCSS', '前端', NULL, 6, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (7, 'Java', '后端', NULL, 7, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (8, 'Spring Boot', '后端', NULL, 8, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (9, 'MyBatis Plus', '后端', NULL, 9, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (10, 'MySQL', '数据库', NULL, 10, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (11, 'Redis', '数据库', NULL, 11, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (12, 'Docker', 'DevOps', NULL, 12, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (13, ' OPC', '工业协议', NULL, 13, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (14, 'Linux', '运维', NULL, 14, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (15, 'Nginx', '运维', NULL, 15, 1, '2026-06-14 01:18:52', '2026-06-14 01:18:52', 0);
INSERT INTO `tb_skill` VALUES (16, 'React', '前端', NULL, 0, 1, '2026-06-20 16:14:41', '2026-06-20 16:14:41', 0);
INSERT INTO `tb_skill` VALUES (17, 'MODBUS', '工业协议', NULL, 0, 1, '2026-06-20 16:18:38', '2026-06-20 16:18:38', 0);
INSERT INTO `tb_skill` VALUES (18, 'PostgreSQL', '数据库', NULL, 0, 1, '2026-06-20 16:23:08', '2026-06-20 16:23:08', 0);
INSERT INTO `tb_skill` VALUES (19, 'TIDB', '数据库', NULL, 0, 1, '2026-06-20 16:25:18', '2026-06-20 16:25:18', 0);
INSERT INTO `tb_skill` VALUES (20, '集群化', '运维', NULL, 0, 1, '2026-06-20 16:28:56', '2026-06-20 16:28:56', 0);
INSERT INTO `tb_skill` VALUES (21, 'Jenkins', 'DevOps', NULL, 0, 1, '2026-06-20 16:29:25', '2026-06-20 16:29:25', 0);
INSERT INTO `tb_skill` VALUES (22, 'Spring Cloud', '后端', NULL, 0, 1, '2026-06-20 16:30:15', '2026-06-20 16:30:15', 0);

-- ----------------------------
-- Table structure for tb_tech_stack
-- ----------------------------
DROP TABLE IF EXISTS `tb_tech_stack`;
CREATE TABLE `tb_tech_stack`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '技术名称',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '图标URL',
  `featured` int(1) NULL DEFAULT 0 COMMENT '是否Featured',
  `sort` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` int(1) NULL DEFAULT 1 COMMENT '状态',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '技术栈管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_tech_stack
-- ----------------------------
INSERT INTO `tb_tech_stack` VALUES (1, 'Vue', '/tech-icons/vue.svg', 1, 1, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (2, 'React', '/tech-icons/react.svg', 1, 2, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (3, 'TypeScript', '/tech-icons/typescript.svg', 1, 3, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (4, 'Java', '/tech-icons/java.svg', 1, 4, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (5, 'Spring Boot', '/tech-icons/spring.svg', 1, 5, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (6, 'MySQL', '/tech-icons/mysql.svg', 0, 6, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (7, 'Redis', '/tech-icons/redis.svg', 0, 7, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (8, 'Docker', '/tech-icons/docker.svg', 0, 8, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (9, 'Git', '/tech-icons/git.svg', 0, 9, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);
INSERT INTO `tb_tech_stack` VALUES (10, 'Linux', '/tech-icons/linux.svg', 0, 10, 1, '2026-06-18 23:45:48', '2026-06-18 23:45:48', 0);

-- ----------------------------
-- Table structure for tb_visitor_location
-- ----------------------------
DROP TABLE IF EXISTS `tb_visitor_location`;
CREATE TABLE `tb_visitor_location`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `visitor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '访客标识（IP+UA的MD5）',
  `ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '访客IP地址',
  `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '国家',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份/州',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `longitude` double NULL DEFAULT NULL COMMENT '经度',
  `latitude` double NULL DEFAULT NULL COMMENT '纬度',
  `visit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_visitor_id`(`visitor_id`) USING BTREE,
  INDEX `idx_visit_time`(`visit_time`) USING BTREE,
  INDEX `idx_city`(`city`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '访客位置记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_visitor_location
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
