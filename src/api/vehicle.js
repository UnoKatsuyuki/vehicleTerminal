import axios from 'axios';

// 使用代理路径，不再需要硬编码IP地址
const AGV_API_BASE_URL = '/prod-api';
const CAMERA_API_BASE_URL = '/easy-api';

// 为AGV后端创建的客户端
const agvApiClient = axios.create({
  baseURL: AGV_API_BASE_URL,
  timeout: 15000, // 将超时时间延长到15秒，以应对可能的网络延迟
});

// 为摄像头服务创建的客户端
const cameraApiClient = axios.create({
    baseURL: CAMERA_API_BASE_URL,
    timeout: 15000, // 将超时时间延长到15秒
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

/**
 * 获取摄像头设备列表
 * @returns {Promise<Array>} 包含摄像头对象的数组
 */
export const getDeviceList = () => {
    // 调用真实接口: GET /devices
    return cameraApiClient.get('/devices?page=1&size=999&status=&id&name', {
        headers: {
            'Authorization': 'Basic YWRtaW4xMjM6QWRtaW5AMTIz'
        },
        // 关键：告诉axios不要自动解析JSON，返回原始文本
        transformResponse: [(data) => data]
    }).then(response => {
        // 现在 response.data 就是一个纯粹的字符串
        // 我们需要检查它是否是一个有效的字符串
        if (typeof response.data === 'string' && response.data.trim().length > 0) {
            try {
                // 手动解析这个JSON字符串
                return JSON.parse(response.data);
            } catch (e) {
                console.error("解析摄像头列表JSON字符串失败:", e);
                return Promise.reject(new Error("Failed to parse camera list JSON"));
            }
        }
        // 如果返回的不是字符串或者为空，则返回一个空数组
        console.warn("摄像头列表API没有返回有效的字符串数据。");
        return [];
    });
};

// 获取任务详情
export const getTaskDetails = (taskId) => {
  return agvApiClient.get(`/agv/task/${taskId}`);
};

// 获取缺陷列表
export const getFlawList = (taskId) => {
  return agvApiClient.get('/agv/flaw/list', {
    params: { taskId }
  });
};

// 获取缺陷详情
export const getFlawDetails = (flawId) => {
    return agvApiClient.get(`/agv/flaw/${flawId}`);
};

// 更新缺陷
export const updateFlaw = (flawData) => {
    return agvApiClient.put('/agv/flaw', flawData);
};

// =================================================================
// **使用正确的移动控制接口**
// =================================================================

/**
 * 控制AGV向前移动
 */
export const agvForward = () => {
    return agvApiClient.post('/agv/movement/forward');
};

/**
 * 停止AGV
 */
export const agvStop = () => {
    return agvApiClient.post('/agv/movement/stop');
};

/**
 * 这是一个包装函数，用于UI中的开关组件。
 * @param {boolean} isMoving true表示前进, false表示停止
 * @returns {Promise<any>} API的响应
 */
export const controlAgv = (isMoving) => {
    if (isMoving) {
        return agvForward();
    } else {
        return agvStop();
    }
};

// 完成任务
export const completeTask = (taskId) => {
    return agvApiClient.post(`/agv/task/complete/${taskId}`);
};

// 终止任务
export const terminateTask = (taskId) => {
    return agvApiClient.post(`/agv/task/terminate/${taskId}`);
};
