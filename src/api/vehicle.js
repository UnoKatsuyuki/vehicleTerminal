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
    // 构造请求头
    const myHeaders = new Headers();
    myHeaders.append("Authorization", "Basic YWRtaW4xMjM6QWRtaW5AMTIz");

    const requestOptions = {
        method: "GET",
        headers: myHeaders,
        redirect: "follow",
    };

    // 使用 fetch 发起请求，路径为代理路径
    return fetch(`${CAMERA_API_BASE_URL}/devices?page=1&size=999&status=&id&name`, requestOptions)
        .then((response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            // 1. 将返回体作为纯文本处理
            return response.text();
        })
        .then((result) => {
            // 2. 检查文本是否为空
            if (result && result.trim().length > 0) {
                try {
                    // 3. 手动将文本解析成JSON
                    return JSON.parse(result);
                } catch (e) {
                    console.error("解析摄像头列表JSON字符串失败:", e);
                    // 如果解析失败，抛出错误
                    return Promise.reject(new Error("Failed to parse camera list JSON"));
                }
            } else {
                // 如果返回的文本为空，返回一个空数组
                console.warn("摄像头列表API返回了空的响应体。");
                return [];
            }
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
