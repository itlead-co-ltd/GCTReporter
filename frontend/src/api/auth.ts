import request from '@/utils/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  role: string
  userId: number
}

export interface UserInfo {
  id: number
  username: string
  role: string
  enabled: boolean
}

export interface ChangePasswordRequest {
  username: string
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

/**
 * 用户登录
 * @param data 登录表单数据
 */
export const login = (data: LoginRequest) => {
  return request.post('/api/v1/auth/login', data) as unknown as Promise<LoginResponse>
}

/**
 * 用户登出
 */
export const logout = () => {
  return request.post('/api/v1/auth/logout') as unknown as Promise<any>
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = () => {
  return request.get('/api/v1/auth/current') as unknown as Promise<UserInfo>
}

/**
 * 修改密码
 * @param data 修改密码请求数据
 */
export const changePassword = (data: ChangePasswordRequest) => {
  return request.post('/api/v1/auth/change-password', data) as unknown as Promise<any>
}
