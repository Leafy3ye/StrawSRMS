import axios from 'axios'

function getApiBaseUrl() {
  if (process.env.NODE_ENV === 'production') {
    // 改为阿里云服务器公网IP
    return 'http://47.99.156.121:8080/';
  } else {
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

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 JWT Token
    const token = localStorage.getItem('token');
    
    if (token) {
      // 设置 Authorization 头而不是 X-Tenant-ID
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token 过期或无效，清除本地存储并跳转到登录页
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 请求拦截器 - 自动添加租户ID到请求头
api.interceptors.request.use(
  config => {
    // 从localStorage获取用户信息
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        if (user.tenantId) {
          config.headers['X-Tenant-ID'] = user.tenantId;
        }
      } catch (error) {
        console.error('解析用户信息失败:', error);
      }
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

export default api