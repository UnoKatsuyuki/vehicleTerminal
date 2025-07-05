// 本地数据源API - 按照carApi.js规范整理
import axios from 'axios';

// 本地数据源常量
const LOCAL_API_BASE_URL = 'http://localhost:8080';

// 创建本地API客户端
const localApiClient = axios.create({
  baseURL: LOCAL_API_BASE_URL,
  timeout: 15000,
});

// 响应拦截器 - 按照carApi.js的规范
localApiClient.interceptors.response.use(
  response => {
    if (response.data) {
      if (typeof response.data.code !== 'undefined') {
        if (response.data.code === 200 || response.data.code === 0) {
          return response.data.data;
        } else {
          console.error('本地API 业务错误:', response.data);
          return Promise.reject(new Error(response.data.msg || `操作失败，业务代码: ${response.data.code}`));
        }
      }
      if (typeof response.data.success !== 'undefined') {
        if (response.data.success) {
          return response.data.data || response.data;
        } else {
          return Promise.reject(new Error(response.data.msg || '操作失败'));
        }
      }
      console.log(response.data);
      return response.data;
    }
    return Promise.reject(new Error('无效的响应'));
  },
  error => {
    console.error('本地API网络请求错误:', error);
    return Promise.reject(error);
  }
);

// ===================== 缺陷管理相关接口 =====================
export function getFlawList(params = {}) {
  return localApiClient.get('/agv/flaw/list', { params });
}

export function getFlawDetail(id) {
  return localApiClient.get(`/agv/flaw/${id}`);
}

export function addFlaw(flawData) {
  return localApiClient.post('/agv/flaw', flawData);
}

export function updateFlaw(flawData) {
  return localApiClient.put('/agv/flaw', flawData);
}

export function deleteFlaw(id) {
  return localApiClient.delete(`/agv/flaw/${id}`);
}

export function confirmFlaw(id) {
  return localApiClient.put(`/agv/flaw/confirm/${id}`);
}

export function uploadFlaw(id) {
  return localApiClient.put(`/agv/flaw/upload/${id}`);
}

export function getLiveFlawsByTaskId(taskId) {
  return localApiClient.get(`/agv/flaw/live/${taskId}`);
}

export function checkAllFlawsConfirmed(taskId) {
  return localApiClient.get(`/agv/flaw/checkConfirmed/${taskId}`);
}

// ===================== 任务管理相关接口 =====================
export function getTaskList(params = {}) {
  return localApiClient.get('/agv/task/list', { params });
}

export function getTaskDetail(id) {
  return localApiClient.get(`/agv/task/${id}`);
}

export function addTask(taskData) {
  return localApiClient.post('/agv/task', taskData);
}

export function updateTask(taskData) {
  return localApiClient.put('/agv/task', taskData);
}

export function deleteTask(id) {
  return localApiClient.delete(`/agv/task/${id}`);
}

export function startTask(id) {
  return localApiClient.put(`/agv/task/start/${id}`);
}

export function endTask(id, isAbort = false) {
  const queryParam = isAbort ? '?isAbort=true' : '';
  return localApiClient.put(`/agv/task/stop/${id}${queryParam}`);
}

export function preUploadTask(id) {
  return localApiClient.put(`/agv/task/preUpload/${id}`);
}

export function uploadTask(id) {
  return localApiClient.put(`/agv/task/upload/${id}`);
}
