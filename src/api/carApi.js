// 合并自 vehicle.js、index.js、taskApi.js
// ===================== vehicle.js =====================
import axios from 'axios';
import { extractTimeFormat } from 'element-plus';
import request from '@/utils/request';

// vehicle.js 常量
const AGV_API_BASE_URL = 'http://192.168.2.57/prod-api';
const CAMERA_API_BASE_URL = 'http://192.168.2.57/easy-api';
const STREAM_MEDIA_URL = 'http://192.168.2.57/webrtc-api';

const agvApiClient = axios.create({
  baseURL: AGV_API_BASE_URL,
  timeout: 15000,
});
const cameraApiClient = axios.create({
  baseURL: CAMERA_API_BASE_URL,
  timeout: 15000,
});
agvApiClient.interceptors.response.use(
  response => {
    if (response.data) {
      if (typeof response.data.code !== 'undefined') {
        if (response.data.code === 200 || response.data.code === 0) {
          return response.data.data;
        } else {
          console.error('API 业务错误:', response.data);
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
      console.log(response.data)
      return response.data;
    }
    return Promise.reject(new Error('无效的响应'));
  },
  error => {
    console.error('AGV API网络请求错误:', error);
    return Promise.reject(error);
  }
);

export const getDeviceList = () => {
  const myHeaders = new Headers();
  myHeaders.append("Authorization", "Basic YWRtaW4xMjM6QWRtaW5AMTIz");
  const requestOptions = {
    method: "GET",
    headers: myHeaders,
    redirect: "follow",
  };
  return fetch(`${CAMERA_API_BASE_URL}/devices?page=1&size=999&status=&id&name`, requestOptions)
    .then((response) => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      return response.text();
    })
    .then((result) => {
      if (result && result.trim().length > 0) {
        try {
          return JSON.parse(result);
        } catch (e) {
          console.error("解析摄像头列表JSON字符串失败:", e);
          return Promise.reject(new Error("Failed to parse camera list JSON"));
        }
      } else {
        console.warn("摄像头列表API返回了空的响应体。");
        return [];
      }
    });
};

export const getTaskDetails = (taskId) => {
  return agvApiClient.get(`/agv/task/${taskId}`);
};

export const getFlawList = (taskId) => {
  return agvApiClient.get('/agv/flaw/list', {
    params: { taskId }
  });
};

export const getFlawDetails = (flawId) => {
  return agvApiClient.get(`/agv/flaw/${flawId}`);
};

// ===================== index.js =====================
const BASE_URL = 'http://192.168.2.57/prod-api';
const apiClient = async (endpoint) => {
  const response = await fetch(`${BASE_URL}${endpoint}`);
  if (!response.ok) {
    throw new Error(`网络响应错误: ${response.status} ${response.statusText}`);
  }
  return response.json();
};

export const checkFs = () => apiClient('/system/check/fs');
export const checkDb = () => apiClient('/system/check/db');
export const checkAgv = () => apiClient('/system/check/agv');
export const checkCam = () => apiClient('/system/check/cam');

// ===================== taskApi.js =====================
// 任务相关
export function listTask(params) {
  return request({
    url: '/agv/task/list',
    method: 'get',
    params
  });
}
export function getTask(id) {
  return request({
    url: `/agv/task/${id}`,
    method: 'get'
  });
}
export function addTask(data) {
  return request({
    url: '/agv/task',
    method: 'post',
    data: data
  })
}
export function updateTask(data) {
  return request({
    url: '/agv/task',
    method: 'put',
    data: data
  })
}
export function delTask(id) {
  return request({
    url: `/agv/task/${id}`,
    method: 'delete'
  })
}
export function preUploadTask(id) {
  return request({
    url: `/agv/task/preupload/${id}`,
    method: 'get'
  });
}
export function uploadTask(id) {
  return request({
    url: `/agv/task/upload/${id}`,
    method: 'post',
    timeout: 600000 // 10分钟
  });
}
// 故障相关
export function listFlaw(params) {
  return request({
    url: '/agv/flaw/list',
    method: 'get',
    params
  });
}
// ========== updateFlaw 同名方法 ==========
// vehicle.js 版本：
export const updateFlaw_vehicle = (flawData) => {
  return agvApiClient.put('/agv/flaw', flawData);
};
// taskApi.js 版本：
export function updateFlaw(data) {
  return request({
    url: '/agv/flaw',
    method: 'put',
    data: data
  });
}
// ========== END updateFlaw ==========
export function batchUpdateFlaw(data) {
  return request({
    url: '/agv/flaw/batch',
    method: 'post',
    data: data
  });
}
// ===================== vehicle.js 其余方法 =====================
export const agvForward = () => agvApiClient.post('/agv/movement/forward');
export const agvStop = () => agvApiClient.post('/agv/movement/stop');
export const agvBackward = () => agvApiClient.post('/agv/movement/backward');
export const getAgvHeartbeat = () => agvApiClient.get('/agv/movement/heartbeat');
export const endTask = (taskId, isAbort = false) => {
  return agvApiClient.post(`/agv/task/end/${taskId}?isAbort=${isAbort}`);
};
export const startTask = (taskId) => {
  return agvApiClient.post(`/agv/task/start/${taskId}`);
};
export const getVideoStreamUrl = (deviceId) => {
  if (!deviceId) return '';
  return `${STREAM_MEDIA_URL}/live/${deviceId}_01.flv`;
};
export function addFlaw(data) {
  return agvApiClient.post('/agv/flaw', data);
}
export const getLiveFlawInfo = (taskId) => {
  return agvApiClient.get(`/agv/flaw/live/${taskId}`);
};
