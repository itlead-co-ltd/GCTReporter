import request from '@/utils/request'

/**
 * 用户数据结构
 */
export interface User {
  id: number
  username: string
  role: string
  enabled: boolean
  createdAt: string
  updatedAt: string
}

/**
 * 获取用户列表
 * @param keyword 搜索关键字（可选）
 */
export const getUsers = (keyword?: string) => {
  return request.get<User[]>('/api/v1/users', {
    params: keyword ? { keyword } : {}
  })
}

/**
 * 根据ID获取用户详情
 * @param id 用户ID
 */
export const getUserById = (id: number) => {
  return request.get<User>(`/api/v1/users/${id}`)
}
