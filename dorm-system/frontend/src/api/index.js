import http from './http'

// Auth
export const authApi = {
  login: (data) => http.post('/auth/login', data),
  changePassword: (data) => http.post('/auth/change-password', data),
}

// Stats
export const statsApi = {
  overview: () => http.get('/stats/overview'),
}

// Buildings
export const buildingApi = {
  list: () => http.get('/buildings'),
  create: (data) => http.post('/buildings', data),
  update: (id, data) => http.put(`/buildings/${id}`, data),
  delete: (id) => http.delete(`/buildings/${id}`),
}

// Rooms
export const roomApi = {
  list: (params) => http.get('/rooms', { params }),
  detail: (id) => http.get(`/rooms/${id}`),
  available: () => http.get('/rooms/available'),
  create: (data) => http.post('/rooms', data),
  update: (id, data) => http.put(`/rooms/${id}`, data),
}

// Students
export const studentApi = {
  list: (params) => http.get('/students', { params }),
  getOne: (id) => http.get(`/students/${id}`),
  create: (data) => http.post('/students', data),
  update: (id, data) => http.put(`/students/${id}`, data),
  checkout: (id, data) => http.post(`/students/${id}/checkout`, data),
  transfer: (id, data) => http.post(`/students/${id}/transfer`, data),
}

// Repairs
export const repairApi = {
  list: (params) => http.get('/repairs', { params }),
  create: (data) => http.post('/repairs', data),
  update: (id, data) => http.put(`/repairs/${id}`, data),
}

// Visitors
export const visitorApi = {
  list: (params) => http.get('/visitors', { params }),
  create: (data) => http.post('/visitors', data),
  update: (id, data) => http.put(`/visitors/${id}`, data),
}

// Fees
export const feeApi = {
  list: (params) => http.get('/fees', { params }),
  create: (data) => http.post('/fees', data),
  pay: (id) => http.post(`/fees/${id}/pay`),
}

// Notices
export const noticeApi = {
  list: (params) => http.get('/notices', { params }),
  create: (data) => http.post('/notices', data),
  update: (id, data) => http.put(`/notices/${id}`, data),
  delete: (id) => http.delete(`/notices/${id}`),
}

// Users
export const userApi = {
  list: () => http.get('/users'),
  create: (data) => http.post('/users', data),
  update: (id, data) => http.put(`/users/${id}`, data),
}
