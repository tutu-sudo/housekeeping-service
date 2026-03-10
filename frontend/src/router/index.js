import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'
import store from '@/store'

// 路由懒加载
const HomeView = () => import('@/views/customer/HomeView.vue')
const ServiceListView = () => import('@/views/customer/ServiceListView.vue')
const HousekeeperListView = () => import('@/views/customer/HousekeeperListView.vue')
const HousekeeperDetailView = () => import('@/views/customer/HousekeeperDetailView.vue')
const AppointmentView = () => import('@/views/customer/AppointmentView.vue')
const ProfileView = () => import('@/views/customer/ProfileView.vue')

const UserProfileView = () => import('@/views/staff/UserProfileView.vue')
const MyScheduleView = () => import('@/views/staff/MyScheduleView.vue')

const AdminDashboardView = () => import('@/views/admin/AdminDashboardView.vue')
const OrderManagementView = () => import('@/views/admin/OrderManagementView.vue')
const VerificationView = () => import('@/views/admin/VerificationView.vue')
const ServiceTypeManagementView = () => import('@/views/admin/ServiceTypeManagementView.vue')
const ServiceItemManagementView = () => import('@/views/admin/ServiceItemManagementView.vue')
const ServiceContentManagementView = () => import('@/views/admin/ServiceContentManagementView.vue')
const ScheduleManagementView = () => import('@/views/admin/ScheduleManagementView.vue')
const StaffCertificateAuditView = () => import('@/views/admin/StaffCertificateAuditView.vue')
const StaffStatusMonitorView = () => import('@/views/admin/StaffStatusMonitorView.vue')
const ReviewManagementView = () => import('@/views/admin/ReviewManagementView.vue')
const PaymentManagementView = () => import('@/views/admin/PaymentManagementView.vue')
const CompanyManagementView = () => import('@/views/admin/CompanyManagementView.vue')
const StatisticsView = () => import('@/views/admin/StatisticsView.vue')

const LoginView = () => import('@/views/auth/LoginView.vue')
const RegisterView = () => import('@/views/auth/RegisterView.vue')
const ForgotPasswordView = () => import('@/views/auth/ForgotPasswordView.vue')

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: RegisterView,
    meta: { requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPasswordView,
    meta: { requiresAuth: false }
  },
  // 客户页面路由
  {
    path: '/home',
    name: 'Home',
    component: HomeView,
    meta: { requiresAuth: false }
  },
  {
    path: '/housekeepers',
    name: 'ServiceList',
    component: ServiceListView,
    meta: { requiresAuth: false, title: '产品服务' }
  },
  {
    path: '/housekeeper-info',
    name: 'HousekeeperInfo',
    component: HousekeeperListView,
    meta: { requiresAuth: false, title: '家政服务资料' }
  },
  {
    path: '/housekeeper/:id',
    name: 'HousekeeperDetail',
    component: HousekeeperDetailView,
    meta: { requiresAuth: false }
  },
  {
    path: '/service/detail',
    name: 'ServiceDetail',
    component: () => import('@/views/customer/ServiceDetailView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/service/annual-package',
    name: 'AnnualPackage',
    component: () => import('@/views/customer/AnnualPackageView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/appointment',
    name: 'Appointment',
    component: AppointmentView,
    meta: { requiresAuth: true, role: ['customer'] }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: ProfileView,
    meta: { requiresAuth: true, role: ['customer'] }
  },
  {
    path: '/appointment/:id',
    name: 'AppointmentDetail',
    component: () => import('@/views/customer/AppointmentDetailView.vue'),
    meta: { requiresAuth: true, role: ['customer'] }
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('@/views/customer/PaymentView.vue'),
    meta: { requiresAuth: true, role: ['customer'] }
  },
  {
    path: '/payment/success',
    name: 'PaymentSuccess',
    component: () => import('@/views/customer/PaymentSuccessView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/payment/fail',
    name: 'PaymentFail',
    component: () => import('@/views/customer/PaymentFailView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/review',
    name: 'Review',
    component: () => import('@/views/customer/ReviewView.vue'),
    meta: { requiresAuth: true, role: ['customer'] }
  },
  // 服务人员页面路由
  {
    path: '/staff/profile',
    name: 'UserProfile',
    component: UserProfileView,
    meta: { requiresAuth: true, role: ['staff'] }
  },
  {
    path: '/staff/schedule',
    name: 'MySchedule',
    component: MyScheduleView,
    meta: { requiresAuth: true, role: ['staff'] }
  },
  // 管理后台路由
  {
    path: '/admin',
    redirect: '/admin/dashboard'
  },
  {
    path: '/admin/dashboard',
    name: 'AdminDashboard',
    component: AdminDashboardView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/orders',
    name: 'OrderManagement',
    component: OrderManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/payments',
    name: 'PaymentManagement',
    component: PaymentManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/services/types',
    name: 'ServiceTypeManagement',
    component: ServiceTypeManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/services/items',
    name: 'ServiceItemManagement',
    component: ServiceItemManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/services/content',
    name: 'ServiceContentManagement',
    component: ServiceContentManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/verification',
    name: 'Verification',
    component: VerificationView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/staff/certificates',
    name: 'StaffCertificateAudit',
    component: StaffCertificateAuditView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/staff/status',
    name: 'StaffStatusMonitor',
    component: StaffStatusMonitorView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/schedules',
    name: 'ScheduleManagement',
    component: ScheduleManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/reviews',
    name: 'ReviewManagement',
    component: ReviewManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/companies',
    name: 'CompanyManagement',
    component: CompanyManagementView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/admin/statistics',
    name: 'Statistics',
    component: StatisticsView,
    meta: { requiresAuth: true, role: ['admin'] }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  const userRole = store.getters['user/userRole']

  // 需要登录的页面
  if (to.meta.requiresAuth) {
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }

    // 需要特定角色的页面
    if (to.meta.role && to.meta.role.length > 0) {
      if (!userRole || !to.meta.role.includes(userRole)) {
        next({ path: '/home' })
        return
      }
    }
  }

  // 已登录用户访问登录/注册页，重定向到首页
  if ((to.path === '/login' || to.path === '/register') && token) {
    next({ path: '/home' })
    return
  }

  next()
})

export default router

