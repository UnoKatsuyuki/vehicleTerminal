import axios from 'axios';

// 使用代理路径
const AGV_API_BASE_URL = 'http://192.168.2.57/prod-api';
const CAMERA_API_BASE_URL = 'http://192.168.2.57/easy-api';
const STREAM_MEDIA_URL = 'http://192.168.2.57/webrtc-api';

// 为AGV后端创建的客户端
const agvApiClient = axios.create({
  baseURL: AGV_API_BASE_URL,
  timeout: 15000,
});

// 为摄像头服务创建的客户端
const cameraApiClient = axios.create({
    baseURL: CAMERA_API_BASE_URL,
    timeout: 15000,
});

// AGV客户端的拦截器
agvApiClient.interceptors.response.use(
  response => {
    if (response.data) {
      // 兼容 code: 200 或 code: 0
      if (typeof response.data.code !== 'undefined') {
        if (response.data.code === 200 || response.data.code === 0) {
          return response.data.data;
        } else {
          console.error('API 业务错误:', response.data);
          return Promise.reject(new Error(response.data.msg || `操作失败，业务代码: ${response.data.code}`));
        }
      }
      // 兼容 success: true
      if (typeof response.data.success !== 'undefined') {
        if (response.data.success) {
          return response.data.data || response.data;
        } else {
          return Promise.reject(new Error(response.data.msg || '操作失败'));
        }
      }
      console.log(response.data)
      // 没有 code 字段，直接返回
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

export const updateFlaw = (flawData) => {
    return agvApiClient.put('/agv/flaw', flawData);
};

export const agvForward = () => agvApiClient.post('/agv/movement/forward');
export const agvStop = () => agvApiClient.post('/agv/movement/stop');
export const agvBackward = () => agvApiClient.post('/agv/movement/backward');
export const getAgvHeartbeat = () => agvApiClient.get('/agv/movement/heartbeat');

// export const controlAgv = (isMoving) => {
//     if (isMoving) {
//         return agvForward();
//     } else {
//         return agvStop();
//     }
// };

export const endTask = (taskId, isAbort = false) => {
    return agvApiClient.post(`/agv/task/end/${taskId}?isAbort=${isAbort}`);
};


// =================================================================
// **新增：将URL拼接逻辑整合到这里**
// =================================================================
/**
 * 根据摄像头ID生成视频流地址
 * @param {string} deviceId 摄像头ID
 * @returns {string} 完整的视频流URL
 */
export const getVideoStreamUrl = (deviceId) => {
    if (!deviceId) return '';
    // 返回通过代理访问的相对路径
    return `${STREAM_MEDIA_URL}/live/${deviceId}_01.flv`;
};
