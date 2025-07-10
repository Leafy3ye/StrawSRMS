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

// 请求拦截器 - 添加租户 ID
api.interceptors.request.use(
  (config) => {
    const userStr = localStorage.getItem('user')
    if (userStr && userStr !== 'undefined' && userStr !== 'null') {
      try {
        const user = JSON.parse(userStr)
        if (user?.tenantId) {
          config.headers['X-Tenant-ID'] = user.tenantId
        }
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

export default api
