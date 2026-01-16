import request, { type ApiResponse } from '@/utils/request'
import type { AxiosResponse } from 'axios'

/**
 * 用户角色枚举
 */
export type UserRole = 'ADMIN' | 'DESIGNER' | 'VIEWER'

/**
 * 用户接口
 */
export interface User {
  id: number
  username: string
  role: UserRole
  enabled: boolean
  createdAt: string
  updatedAt: string
}

/**
 * 创建用户请求
 */
export interface CreateUserRequest {
  username: string
  password: string
  role: UserRole
  enabled?: boolean
}

/**
 * 更新用户请求
 */
export interface UpdateUserRequest {
  password?: string
  role?: UserRole
  enabled?: boolean
}

/**
 * 用户API服务
 */
export const userApi = {
  /**
   * 获取所有用户
   */
  getAllUsers: () => {
    return request.get<any, AxiosResponse<ApiResponse<User[]>>>('/api/v1/users').then(res => res.data)
  },

  /**
   * 根据ID获取用户
   */
  getUserById: (id: number) => {
    return request.get<any, AxiosResponse<ApiResponse<User>>>(`/api/v1/users/${id}`).then(res => res.data)
  },

  /**
   * 创建用户
   */
  createUser: (data: CreateUserRequest) => {
    return request.post<any, AxiosResponse<ApiResponse<User>>>('/api/v1/users', data).then(res => res.data)
  },

  /**
   * 更新用户
   */
  updateUser: (id: number, data: UpdateUserRequest) => {
    return request.put<any, AxiosResponse<ApiResponse<User>>>(`/api/v1/users/${id}`, data).then(res => res.data)
  },

  /**
   * 删除用户
   */
  deleteUser: (id: number) => {
    return request.delete<any, AxiosResponse<ApiResponse<void>>>(`/api/v1/users/${id}`).then(res => res.data)
  },

  /**
   * 启用/禁用用户
   */
  toggleUserStatus: (id: number, enabled: boolean) => {
    return request.patch<any, AxiosResponse<ApiResponse<User>>>(`/api/v1/users/${id}/status`, null, {
      params: { enabled }
    }).then(res => res.data)
  }
}
