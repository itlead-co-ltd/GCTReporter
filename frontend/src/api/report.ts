import request from '@/utils/request'

export interface Report {
  id: number
  name: string
  description: string
  sqlContent: string
  creatorId: number
  createdAt: string
  updatedAt: string
}

export interface CheckNameResponse {
  exists: boolean
}

/**
 * 检查报表名称是否已存在
 * @param name 报表名称
 */
export const checkReportName = (name: string): Promise<CheckNameResponse> => {
  return request.get('/api/v1/reports/check-name', {
    params: { name }
  })
}
