import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import store from '@/store'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果是text/html类型的响应（如支付宝支付表单），直接返回
    const contentType = response.headers['content-type'] || ''
    if (contentType.includes('text/html') || response.config.responseType === 'text') {
      return response
    }
    
    const res = response.data
    
    // 检查是否为标准的Result响应格式
    if (res && typeof res === 'object' && 'code' in res) {
      // 如果code不等于200，表示请求失败
    if (res.code !== undefined && res.code !== 200) {
      const errorMessage = res.message || '请求失败'
      const skipErrorHandler = response.config?.skipErrorHandler
      
      // 如果设置了 skipErrorHandler，不显示错误消息
      if (!skipErrorHandler) {
        ElMessage.error(errorMessage)
      }
      
      if (res.code === 401 && !skipErrorHandler) {
        removeToken()
        store.dispatch('user/logout')
        window.location.href = '/login'
      }
      
      const error = new Error(errorMessage)
      error.code = res.code
      error.response = response
      error.messageShown = !skipErrorHandler
      return Promise.reject(error)
    }
    
      // code === 200，请求成功，返回response（包含完整的Result对象）
      // response.data 会包含 { code: 200, message: "success", data: "...", success: true }
      return response
    }
    
    // 如果不是标准Result格式，直接返回（可能是其他格式的响应）
    return response
  },
  error => {
    // 检查是否应该跳过错误处理（静默失败）
    const skipErrorHandler = error.config?.skipErrorHandler
    
    if (error.response) {
      const { status, data } = error.response
      
      // 如果设置了 skipErrorHandler，不显示错误消息（但仍返回 rejected promise）
      if (!skipErrorHandler) {
        switch (status) {
          case 401:
            ElMessage.error('未授权，请重新登录')
            removeToken()
            store.dispatch('user/logout')
            window.location.href = '/login'
            break
          case 403:
            ElMessage.error('拒绝访问')
            break
          case 404:
            ElMessage.error('请求的资源不存在')
            break
          case 500:
            ElMessage.error('服务器错误')
            break
          default:
            ElMessage.error(data?.message || '请求失败')
        }
      }
    } else {
      // 网络错误也检查 skipErrorHandler
      if (!skipErrorHandler) {
        ElMessage.error('网络错误，请检查网络连接')
      }
    }
    
    return Promise.reject(error)
  }
)

export default service

