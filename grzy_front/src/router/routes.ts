import type { RouteRecordRaw } from 'vue-router'

/**
 * 常量路由（不需要权限）
 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      hidden: true,
    },
  },
  {
    path: '/',
    name: 'Front',
    component: () => import('@/layouts/FrontLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/front/Home.vue'),
        meta: {
          title: '首页',
          icon: 'HomeOutlined',
        },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/front/Profile.vue'),
        meta: {
          title: '个人信息',
          icon: 'UserOutlined',
        },
      },
      {
        path: 'project',
        name: 'Project',
        component: () => import('@/views/front/Project.vue'),
        meta: {
          title: '项目展示',
          icon: 'ProjectOutlined',
        },
      },
      {
        path: 'opensource',
        name: 'OpenSource',
        component: () => import('@/views/front/OpenSource.vue'),
        meta: {
          title: '开源项目',
          icon: 'GithubOutlined',
        },
      },
      {
        path: 'internship',
        name: 'Internship',
        component: () => import('@/views/front/Internship.vue'),
        meta: {
          title: '实习经历',
          icon: 'SolutionOutlined',
        },
      },
      {
        path: 'article',
        name: 'Article',
        component: () => import('@/views/front/Article.vue'),
        meta: {
          title: '技术博客',
          icon: 'ReadOutlined',
        },
      },
      {
        path: 'series',
        name: 'SeriesList',
        component: () => import('@/views/front/SeriesList.vue'),
        meta: { title: '专栏系列', icon: 'ReadOutlined' },
      },
      {
        path: 'series/:id',
        name: 'SeriesDetail',
        component: () => import('@/views/front/SeriesDetail.vue'),
        meta: { title: '专栏详情', hidden: true },
      },
      {
        path: 'guestbook',
        name: 'GuestBook',
        component: () => import('@/views/front/GuestBook.vue'),
        meta: {
          title: '留言墙',
          icon: 'MessageOutlined',
        },
      },
      {
        path: 'album',
        name: 'Album',
        component: () => import('@/views/front/Album.vue'),
        meta: {
          title: '相册',
          icon: 'CameraOutlined',
        },
      },
      {
        path: 'links',
        name: 'FriendLinks',
        component: () => import('@/views/front/FriendLinks.vue'),
        meta: {
          title: '友情链接',
          icon: 'LinkOutlined',
        },
      },
      {
        path: 'archive',
        name: 'Archive',
        component: () => import('@/views/front/Archive.vue'),
        meta: {
          title: '文章归档',
          icon: 'CalendarOutlined',
        },
      },
      {
        path: 'visitor-map',
        name: 'VisitorMap',
        component: () => import('@/views/front/VisitorMap.vue'),
        meta: {
          title: '访客足迹',
          icon: 'EnvironmentOutlined',
        },
      },
      {
        path: 'project/:id',
        name: 'ProjectDetail',
        component: () => import('@/views/front/ProjectDetail.vue'),
        meta: {
          title: '项目详情',
          hidden: true,
        },
      },
      {
        path: 'collections',
        name: 'MyCollections',
        component: () => import('@/views/front/MyCollections.vue'),
        meta: {
          title: '我的收藏',
          hidden: true,
        },
      },
      {
        path: 'article/:id',
        name: 'ArticleDetail',
        component: () => import('@/views/front/ArticleDetail.vue'),
        meta: {
          title: '文章详情',
          hidden: true,
        },
      },
    ],
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/common/404.vue'),
    meta: {
      hidden: true,
    },
  },
]

/**
 * 异步路由（需要权限）
 */
export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/admin',
    name: 'Admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    meta: {
      title: '后台管理',
      icon: 'SettingOutlined',
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: {
          title: '仪表盘',
          icon: 'DashboardOutlined',
          permission: 'system:dashboard:view',
        },
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('@/views/admin/ProfileManage.vue'),
        meta: {
          title: '个人信息管理',
          icon: 'UserOutlined',
          permission: 'content:profile:view',
        },
      },
      {
        path: 'skill',
        name: 'SkillManage',
        component: () => import('@/views/admin/SkillManage.vue'),
        meta: {
          title: '技能管理',
          icon: 'ThunderboltOutlined',
          permission: 'content:skill:view',
        },
      },
      {
        path: 'techstack',
        name: 'TechStackManage',
        component: () => import('@/views/admin/TechStackManage.vue'),
        meta: {
          title: '技术栈管理',
          icon: 'AppstoreOutlined',
          permission: 'content:techstack:view',
        },
      },
      {
        path: 'project',
        name: 'ProjectManage',
        component: () => import('@/views/admin/ProjectManage.vue'),
        meta: {
          title: '项目管理',
          icon: 'ProjectOutlined',
          permission: 'content:project:view',
        },
      },
      {
        path: 'internship',
        name: 'InternshipManage',
        component: () => import('@/views/admin/InternshipManage.vue'),
        meta: {
          title: '实习管理',
          icon: 'SolutionOutlined',
          permission: 'content:internship:view',
        },
      },
      {
        path: 'award',
        name: 'AwardManage',
        component: () => import('@/views/admin/AwardManage.vue'),
        meta: {
          title: '获奖管理',
          icon: 'TrophyOutlined',
          permission: 'content:award:view',
        },
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: {
          title: '用户管理',
          icon: 'TeamOutlined',
          permission: 'system:user:view',
        },
      },
      {
        path: 'article',
        name: 'ArticleManage',
        component: () => import('@/views/admin/ArticleManage.vue'),
        meta: {
          title: '文章管理',
          icon: 'FileTextOutlined',
          permission: 'blog:article:view',
        },
      },
      {
        path: 'role',
        name: 'RoleManage',
        component: () => import('@/views/admin/RoleManage.vue'),
        meta: {
          title: '角色管理',
          icon: 'SafetyOutlined',
          permission: 'system:role:view',
        },
      },
      {
        path: 'comment',
        name: 'CommentManage',
        component: () => import('@/views/admin/CommentManage.vue'),
        meta: {
          title: '评论管理',
          icon: 'CommentOutlined',
          permission: 'blog:comment:view',
        },
      },
      {
        path: 'series',
        name: 'SeriesManage',
        component: () => import('@/views/admin/SeriesManage.vue'),
        meta: {
          title: '专栏管理',
          icon: 'ReadOutlined',
          permission: 'blog:series:view',
        },
      },
      {
        path: 'friendlink',
        name: 'FriendLinkManage',
        component: () => import('@/views/admin/FriendLinkManage.vue'),
        meta: {
          title: '友链管理',
          icon: 'LinkOutlined',
          permission: 'blog:friendlink:view',
        },
      },
      {
        path: 'album',
        name: 'AlbumManage',
        component: () => import('@/views/admin/AlbumManage.vue'),
        meta: {
          title: '相册管理',
          icon: 'CameraOutlined',
          permission: 'content:album:view',
        },
      },
      {
        path: 'open-source',
        name: 'OpenSourceManage',
        component: () => import('@/views/admin/OpenSourceManage.vue'),
        meta: {
          title: '开源项目',
          icon: 'GithubOutlined',
          permission: 'content:opensource:view',
        },
      },
      {
        path: 'notify-setting',
        name: 'NotifySettingManage',
        component: () => import('@/views/admin/NotifySettingManage.vue'),
        meta: {
          title: '通知设置',
          icon: 'BellOutlined',
          permission: 'system:notify:view',
        },
      },
      {
        path: 'online-session',
        name: 'OnlineSessionManage',
        component: () => import('@/views/admin/OnlineSessionManage.vue'),
        meta: {
          title: '在线会话',
          icon: 'LaptopOutlined',
          permission: 'system:session:view',
        },
      },
      {
        path: 'apidoc',
        name: 'ApiDoc',
        component: () => import('@/views/admin/ApiDoc.vue'),
        meta: {
          title: 'API文档',
          icon: 'FileSearchOutlined',
          permission: 'system:apidoc:view',
        },
      },
      {
        path: 'log',
        name: 'OperLogManage',
        component: () => import('@/views/admin/OperLogManage.vue'),
        meta: {
          title: '操作日志',
          icon: 'ClockCircleOutlined',
          permission: 'system:log:view',
        },
      },
      {
        path: 'visitor-map',
        name: 'VisitorMapManage',
        component: () => import('@/views/admin/VisitorMapManage.vue'),
        meta: {
          title: '访客地图',
          icon: 'EnvironmentOutlined',
          permission: 'system:visitor-map:view',
        },
      },
    ],
  },
]

/**
 * 通配路由（放在最后）
 */
export const wildcardRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  redirect: '/404',
  meta: {
    hidden: true,
  },
}
