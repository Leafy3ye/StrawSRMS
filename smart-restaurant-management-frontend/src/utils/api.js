import axios from 'axios'

function getApiBaseUrl() {
  if (process.env.NODE_ENV === 'production') {
    // ✅ 走 Nginx 反向代理，避免 HTTPS 跨域问题
    return '/api/';
  } else {
    // 本地开发依然使用后端直连地址
    return 'http://localhost:8080/';
  }
}

const api = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加 JWT Token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 请求拦截器 - 添加租户 ID 和店铺 ID
api.interceptors.request.use(
  (config) => {
    const userStr = localStorage.getItem('user')
    if (userStr && userStr !== 'undefined' && userStr !== 'null') {
      try {
        const user = JSON.parse(userStr)

        // 检查是否为超级管理员
        if (user?.userType === 'SUPER_ADMIN') {
          // 超级管理员：检查是否有手动设置的租户ID和店铺ID
          if (config.headers['X-Tenant-ID']) {
            // 如果请求中已经设置了租户ID，保持不变
          } else {
            // 超级管理员默认不设置租户ID，可以查看所有数据
          }
        } else {
          // 普通用户：使用用户自己的租户ID和店铺ID
          if (user?.tenantId) {
            config.headers['X-Tenant-ID'] = user.tenantId
          }
          // 添加店铺ID请求头
          if (user?.storeId) {
            config.headers['X-Store-Id'] = user.storeId
          }
        }

        console.log('请求头设置:', {
          'X-Tenant-ID': config.headers['X-Tenant-ID'],
          'X-Store-Id': config.headers['X-Store-Id'],
          'userType': user?.userType
        });
      } catch (error) {
        console.error('解析用户信息失败:', error)
        localStorage.removeItem('user')
      }
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 - 处理 401 登录失效
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

// 创建一个可以指定租户ID的API实例（用于超级管理员）
export const createTenantApi = (tenantId, storeId = null) => {
  return {
    get: (url, config = {}) => {
      const headers = {
        ...config.headers,
        'X-Tenant-ID': tenantId
      }
      if (storeId) {
        headers['X-Store-Id'] = storeId
      }
      return api.get(url, { ...config, headers })
    },
    post: (url, data, config = {}) => {
      const headers = {
        ...config.headers,
        'X-Tenant-ID': tenantId
      }
      if (storeId) {
        headers['X-Store-Id'] = storeId
      }
      return api.post(url, data, { ...config, headers })
    },
    put: (url, data, config = {}) => {
      const headers = {
        ...config.headers,
        'X-Tenant-ID': tenantId
      }
      if (storeId) {
        headers['X-Store-Id'] = storeId
      }
      return api.put(url, data, { ...config, headers })
    },
    delete: (url, config = {}) => {
      const headers = {
        ...config.headers,
        'X-Tenant-ID': tenantId
      }
      if (storeId) {
        headers['X-Store-Id'] = storeId
      }
      return api.delete(url, { ...config, headers })
    }
  }
}

export default api
