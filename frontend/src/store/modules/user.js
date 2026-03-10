import { login, register, refreshToken } from '@/api/auth'
import { getUserInfo as apiGetUserProfile } from '@/api/user'
import { getToken, setToken, getUserInfo, setUserInfo, clearAuth } from '@/utils/auth'

const state = {
  token: getToken() || '',
  userInfo: getUserInfo() || null,
  isLoggedIn: !!getToken()
}

const getters = {
  token: state => state.token,
  userInfo: state => state.userInfo,
  isLoggedIn: state => state.isLoggedIn,
  userRole: state => state.userInfo?.role || null,
  userId: state => state.userInfo?.id || null
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    setToken(token)
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    setUserInfo(userInfo)
    state.isLoggedIn = true
  },
  CLEAR_AUTH(state) {
    state.token = ''
    state.userInfo = null
    state.isLoggedIn = false
    clearAuth()
  }
}

const actions = {
  // 登录
  async login({ commit }, loginForm) {
    try {
      const response = await login(loginForm)
      
      let data = response.data
      
      if (data && data.data) {
        data = data.data
      }
      
      if (!data) {
        data = response
      }
      
      const token = data.token || data.accessToken || data.access_token || data.jwtToken || data.jwt
      
      if (token) {
        commit('SET_TOKEN', token)
        
        // 获取用户信息，支持多种数据结构
        const rawUserInfo = data.userInfo || data.user || data.userData || data
        
        // 获取用户类型，支持多种字段名和位置
        const getUserType = () => {
          // 优先从 userInfo 中获取
          if (rawUserInfo.userType !== undefined) return rawUserInfo.userType
          if (rawUserInfo.user_type !== undefined) return rawUserInfo.user_type
          if (rawUserInfo.userTypeId !== undefined) return rawUserInfo.userTypeId
          
          // 从 data 中获取
          if (data.userType !== undefined) return data.userType
          if (data.user_type !== undefined) return data.user_type
          if (data.userTypeId !== undefined) return data.userTypeId
          
          return null
        }
        
        // 获取角色，支持多种字段名
        const getRole = () => {
          const userType = getUserType()
          if (userType === 1) return 'customer'
          if (userType === 2) return 'staff'
          if (userType === 3) return 'admin'
          
          // 如果 userType 不存在，尝试从 role 字段获取
          const role = rawUserInfo.role || rawUserInfo.userRole || data.role || data.userRole
          if (role) {
            // 标准化角色名称
            if (role === 'customer' || role === 'Customer' || role === 'CUSTOMER') return 'customer'
            if (role === 'staff' || role === 'Staff' || role === 'STAFF') return 'staff'
            if (role === 'admin' || role === 'Admin' || role === 'ADMIN') return 'admin'
            return role.toLowerCase()
          }
          
          // 默认返回 customer
          return 'customer'
        }
        
        // 对于服务人员，优先使用staff.name作为显示名称
        const getDisplayName = () => {
          // 如果后端返回了staff信息，使用staff.name
          if (rawUserInfo.staff?.name) return rawUserInfo.staff.name
          if (data.staff?.name) return data.staff.name
          if (rawUserInfo.name) return rawUserInfo.name
          if (data.name) return data.name
          // 否则使用username
          return rawUserInfo.username || data.username || loginForm.username
        }
        
        const userId = rawUserInfo.id || rawUserInfo.userId || data.id || data.userId
        
        // 调试：打印登录时获取的用户信息
        if (import.meta.env.DEV) {
          console.log('登录响应 - rawUserInfo:', rawUserInfo)
          console.log('登录响应 - data:', data)
          console.log('登录响应 - 解析出的userId:', userId)
          console.log('登录响应 - username:', rawUserInfo.username || data.username || loginForm.username)
        }
        
        const userInfo = {
          id: userId,
          username: rawUserInfo.username || data.username || loginForm.username,
          name: getDisplayName(), // 显示名称（服务人员使用staff.name，其他使用username）
          role: getRole(),
          avatar: rawUserInfo.avatar || rawUserInfo.avatarUrl || data.avatar || data.avatarUrl,
          phone: rawUserInfo.phone || rawUserInfo.phoneNumber || data.phone || data.phoneNumber,
          email: rawUserInfo.email || data.email,
          ...rawUserInfo
        }

        // 尝试从新的 /api/users/{userId} 接口获取最新的用户资料（头像/手机号/邮箱等）
        try {
          if (userId) {
            const profileRes = await apiGetUserProfile(userId)
            const profileData = profileRes.data?.data || profileRes.data || {}
            Object.assign(userInfo, {
              avatar: profileData.avatar ?? userInfo.avatar,
              phone: profileData.phone ?? userInfo.phone,
              email: profileData.email ?? userInfo.email,
              username: profileData.username ?? userInfo.username
            })
          }
        } catch (e) {
          // 如果拉取失败，不影响登录流程，继续使用登录接口返回的数据
        }
        
        if (import.meta.env.DEV) {
          console.log('登录响应 - 最终userInfo:', userInfo)
        }
        
        commit('SET_USER_INFO', userInfo)
        return Promise.resolve(response)
      } else {
        return Promise.reject(new Error('登录失败：后端未返回token'))
      }
    } catch (error) {
      return Promise.reject(error)
    }
  },

  // 注册
  async register({ commit }, registerForm) {
    try {
      const response = await register(registerForm)
      
      let data = response.data
      
      if (data && data.data) {
        data = data.data
      }
      
      if (!data) {
        data = response
      }
      
      const token = data.token || data.accessToken || data.access_token || data.jwtToken || data.jwt
      
      if (token) {
        commit('SET_TOKEN', token)
        
        // 获取用户信息，支持多种数据结构
        const rawUserInfo = data.userInfo || data.user || data.userData || data
        
        // 获取用户类型，支持多种字段名和位置
        const getUserType = () => {
          if (rawUserInfo.userType !== undefined) return rawUserInfo.userType
          if (rawUserInfo.user_type !== undefined) return rawUserInfo.user_type
          if (rawUserInfo.userTypeId !== undefined) return rawUserInfo.userTypeId
          if (data.userType !== undefined) return data.userType
          if (data.user_type !== undefined) return data.user_type
          if (data.userTypeId !== undefined) return data.userTypeId
          return null
        }
        
        // 获取角色
        const getRole = () => {
          const userType = getUserType()
          if (userType === 1) return 'customer'
          if (userType === 2) return 'staff'
          if (userType === 3) return 'admin'
          
          const role = rawUserInfo.role || rawUserInfo.userRole || data.role || data.userRole
          if (role) {
            if (role === 'customer' || role === 'Customer' || role === 'CUSTOMER') return 'customer'
            if (role === 'staff' || role === 'Staff' || role === 'STAFF') return 'staff'
            if (role === 'admin' || role === 'Admin' || role === 'ADMIN') return 'admin'
            return role.toLowerCase()
          }
          
          // 从注册表单中获取 userType
          if (registerForm.userType !== undefined) {
            if (registerForm.userType === 1) return 'customer'
            if (registerForm.userType === 2) return 'staff'
            if (registerForm.userType === 3) return 'admin'
          }
          
          return 'customer'
        }
        
        const userInfo = {
          id: rawUserInfo.id || rawUserInfo.userId || data.id || data.userId,
          username: rawUserInfo.username || data.username || registerForm.username,
          role: getRole(),
          avatar: rawUserInfo.avatar || rawUserInfo.avatarUrl || data.avatar || data.avatarUrl,
          phone: registerForm.phone,
          email: registerForm.email,
          ...rawUserInfo
        }
        
        commit('SET_USER_INFO', userInfo)
        return Promise.resolve(response)
      } else {
        return Promise.reject(new Error('注册失败：后端未返回token'))
      }
    } catch (error) {
      return Promise.reject(error)
    }
  },

  // 刷新token
  async refreshToken({ commit }) {
    try {
      const response = await refreshToken()
      if (response.data && response.data.token) {
        commit('SET_TOKEN', response.data.token)
        return Promise.resolve(response)
      }
    } catch (error) {
      commit('CLEAR_AUTH')
      return Promise.reject(error)
    }
  },

  // 登出
  logout({ commit }) {
    commit('CLEAR_AUTH')
  },

  // 更新用户信息（例如个人中心修改资料/头像）
  // 这里只接收部分字段，需要与当前的 userInfo 做合并，避免丢失 id / role 等关键字段
  updateUserInfo({ commit, state }, payload) {
    const current = state.userInfo || {}
    const merged = {
      ...current,
      ...payload
    }
    commit('SET_USER_INFO', merged)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}

