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

/**
 * 用户登录
 * @param data 登录表单数据
 */
export const login = (data: LoginRequest): Promise<LoginResponse> => {
  return request.post('/api/v1/auth/login', data)
}

/**
 * 用户登出
 */
export const logout = (): Promise<any> => {
  return request.post('/api/v1/auth/logout')
}

/**
 * 获取当前用户信息
 */
export const getCurrentUser = (): Promise<UserInfo> => {
  return request.get('/api/v1/auth/current')
}
