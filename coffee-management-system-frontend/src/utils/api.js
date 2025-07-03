import axios from 'axios'

function getApiBaseUrl() {
  if (process.env.NODE_ENV === 'production') {
    // 改为你的阿里云服务器公网IP
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

// 响应拦截器
api.interceptors.response.use(
  response => response,
  error => {
    console.error('API请求错误:', error)
    return Promise.reject(error)
  }
)

export default api