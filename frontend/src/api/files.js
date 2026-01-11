import request from '@/utils/request'

// 上传文件
export function uploadFile(formData) {
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取文件信息
export function getFileInfo(fileId) {
  return request({
    url: `/files/${fileId}`,
    method: 'get'
  })
}

// 按分类/业务ID查询文件
export function getFiles(params) {
  return request({
    url: '/files',
    method: 'get',
    params
  })
}

// 删除文件
export function deleteFile(fileId) {
  return request({
    url: `/files/${fileId}`,
    method: 'delete'
  })
}

