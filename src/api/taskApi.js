import request from '@/utils/request';

// --- 5. 巡视任务管理相关接口 ---

/**
 * 获取任务列表 (分页)
 * @param {object} params - 查询参数，包含过滤条件和分页信息 PageParam
 */
export function listTask(params) {
  return request({
    url: '/agv/task/list',
    method: 'get',
    params
  });
}

/**
 * 获取单个任务详情
 * @param {number | string} id - 任务ID
 */
export function getTask(id) {
  return request({
    url: `/agv/task/${id}`,
    method: 'get'
  });
}

/**
 * 新增任务
 * @param {object} data - 任务对象 AgvTask
 */
export function addTask(data) {
  return request({
    url: '/agv/task',
    method: 'post',
    data: data
  })
}

/**
 * 更新任务
 * @param {object} data - 任务对象 AgvTask
 */
export function updateTask(data) {
  return request({
    url: '/agv/task',
    method: 'put',
    data: data
  })
}

/**
 * 删除任务
 * @param {number | string} id - 任务ID
 */
export function delTask(id) {
  return request({
    url: `/agv/task/${id}`,
    method: 'delete'
  })
}

/**
 * 查询待上传的数据
 * @param {number | string} id - 任务ID
 */
export function preUploadTask(id) {
  return request({
    url: `/agv/task/preupload/${id}`,
    method: 'get'
  });
}

/**
 * 上传任务数据
 * @param {number | string} id - 任务ID
 */
export function uploadTask(id) {
  return request({
    url: `/agv/task/upload/${id}`,
    method: 'post',
    timeout: 600000 // 10分钟
  });
}


// --- 2. 故障缺陷管理相关接口 ---

/**
 * 【新增】根据任务ID获取缺陷列表
 * @param {object} params - 查询参数，例如 { taskId: 'xxx', pageSize: 999 }
 */
export function listFlaw(params) {
  return request({
    url: '/agv/flaw/list',
    method: 'get',
    params
  });
}


/**
 * 批量更新缺陷
 * @param {Array<object>} data - 包含多个缺陷更新对象的数组
 */
export function batchUpdateFlaw(data) {
  return request({
    url: '/agv/flaw/batch',
    method: 'put',
    data: data
  });
}
