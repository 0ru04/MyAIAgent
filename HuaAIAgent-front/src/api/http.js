import axios from 'axios'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

export const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 0,
  headers: {
    'Content-Type': 'application/json',
  },
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isCancel(error) || error?.code === 'ERR_CANCELED') {
      return Promise.reject(error)
    }
    if (error?.code === 'ERR_NETWORK') {
      error.message = '无法连接后端服务，请确认服务已启动'
    } else if (error?.response?.status >= 500) {
      error.message = `服务端异常（${error.response.status}）`
    } else if (error?.response?.data?.message) {
      error.message = error.response.data.message
    }
    return Promise.reject(error)
  },
)
