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
    if (response.data && response.data.code === 200) {
      return response.data.data;
    }
    return Promise.reject(new Error(response.data.msg || '业务错误'));
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

export const agvForward = () => {
    return agvApiClient.post('/agv/movement/forward');
};

export const agvStop = () => {
    return agvApiClient.post('/agv/movement/stop');
};

export const controlAgv = (isMoving) => {
    if (isMoving) {
        return agvForward();
    } else {
        return agvStop();
    }
};

export const completeTask = (taskId) => {
    return agvApiClient.post(`/agv/task/complete/${taskId}`);
};

export const terminateTask = (taskId) => {
    return agvApiClient.post(`/agv/task/terminate/${taskId}`);
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
