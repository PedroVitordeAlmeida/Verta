import axios from 'axios'

const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const apiClient = axios.create({ baseURL })

// Anexa o token JWT salvo no login em toda requisicao.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('verta_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Se o token expirar/for invalido, manda de volta pro login.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('verta_token')
      localStorage.removeItem('verta_usuario')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)
