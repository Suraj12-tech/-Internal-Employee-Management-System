import axios from 'axios'

// One shared Axios instance for the whole app.
// baseURL means every call can just be api.get('/employees') instead of the full URL.
const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

// This runs BEFORE every request: it automatically attaches the JWT token
// (if we have one saved) so we don't have to repeat this in every page.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// If the backend ever responds with 401 (token expired/invalid), automatically
// log the user out and send them back to the login page.
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api
