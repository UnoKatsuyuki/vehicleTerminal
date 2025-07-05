// 本地数据源API - 按照carApi.js规范整理
import axios from 'axios';

// 本地数据源常量
const LOCAL_API_BASE_URL = 'http://localhost:8080';

// 创建本地API客户端
const localApiClient = axios.create({
  baseURL: LOCAL_API_BASE_URL,
  timeout: 15000,
});

// 响应拦截器
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
      return response.data;
    }
    return Promise.reject(new Error('无效的响应'));
  },
  error => {
    console.error('本地API网络请求错误:', error);
    return Promise.reject(error);
  }
);

// ===================== 任务管理相关接口 =====================
export function listTask(params) {
  return localApiClient.get('/agv/task/list', { params });
}

export function getTask(id) {
  return localApiClient.get(`/agv/task/${id}`);
}

export function addTask(data) {
  return localApiClient.post('/agv/task', data);
}

export function updateTask(data) {
  return localApiClient.put('/agv/task', data);
}

export function delTask(id) {
  return localApiClient.delete(`/agv/task/${id}`);
}

export function preUploadTask(id) {
  return localApiClient.get(`/agv/task/preUpload/${id}`);
}

export function uploadTask(id) {
  return localApiClient.put(`/agv/task/upload/${id}`, null, {
    timeout: 600000 // 10分钟
  });
}

export function startTask(id) {
  return localApiClient.put(`/agv/task/start/${id}`);
}

export function endTask(id, isAbort = false) {
  return localApiClient.put(`/agv/task/stop/${id}?isAbort=${isAbort}`);
}

// ===================== 故障管理相关接口 =====================
export function listFlaw(params) {
  return localApiClient.get('/agv/flaw/list', { params });
}

export function getFlaw(id) {
  return localApiClient.get(`/agv/flaw/${id}`);
}

export function addFlaw(data) {
  return localApiClient.post('/agv/flaw', data);
}

export function updateFlaw(data) {
  return localApiClient.put('/agv/flaw', data);
}

export function delFlaw(id) {
  return localApiClient.delete(`/agv/flaw/${id}`);
}

export function confirmFlaw(id) {
  return localApiClient.put(`/agv/flaw/confirm/${id}`);
}

export function uploadFlaw(id) {
  return localApiClient.put(`/agv/flaw/upload/${id}`);
}

export function batchUpdateFlaw(data) {
  return localApiClient.post('/agv/flaw/batch', data);
}

// ===================== 实时数据接口 =====================
export function getLiveFlawInfo(taskId) {
  return localApiClient.get(`/agv/flaw/live/${taskId}`);
}

export function checkAllFlawsConfirmed(taskId) {
  return localApiClient.get(`/agv/flaw/checkConfirmed/${taskId}`);
}

// ===================== 系统检查接口 =====================
export const checkFs = () => localApiClient.get('/system/check/fs');
export const checkDb = () => localApiClient.get('/system/check/db');
export const checkAgv = () => localApiClient.get('/system/check/agv');
export const checkCam = () => localApiClient.get('/system/check/cam');
