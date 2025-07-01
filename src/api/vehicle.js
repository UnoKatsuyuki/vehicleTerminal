import axios from 'axios';

// 车辆Web服务的基地址
const API_BASE_URL = 'http://192.168.2.57/prod-api';

// 创建一个Axios实例，用于与车辆后端通信
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000, // 请求超时时间设置为10秒
});

// Axios响应拦截器，用于简化数据提取和统一错误处理
apiClient.interceptors.response.use(
  response => {
    // 检查响应体和业务代码是否成功
    if (response.data && response.data.code === 200) {
      // 如果成功，只返回核心的 `data` 部分
      return response.data.data;
    }
    // 如果业务代码不是200或响应体结构不正确，则构造一个错误对象并拒绝Promise
    return Promise.reject(new Error(response.data.msg || '业务错误，但未提供错误信息'));
  },
  error => {
    // 处理网络层面的错误 (如超时、网络中断等)
    console.error('API网络请求错误:', error);
    return Promise.reject(error);
  }
);

/**
 * 获取系统配置，包含摄像头地址等信息
 * @returns {Promise<Object>} 系统配置对象
 */
export const getSystemConfig = () => {
  // 调用真实接口: GET /agv/config
  return apiClient.get('/agv/config');
};

/**
 * 获取指定ID的任务详情。此函数也将用于轮询以获取车辆的最新状态和当前距离。
 * @param {string | number} taskId 任务ID
 * @returns {Promise<Object>} 包含任务详情的对象 (AgvTask)
 */
export const getTaskDetails = (taskId) => {
  // 调用真实接口: GET /agv/task/{id}
  return apiClient.get(`/agv/task/${taskId}`);
};

/**
 * 获取指定任务的缺陷列表。此函数将用于轮询以发现新故障。
 * @param {string | number} taskId 任务ID
 * @returns {Promise<Array>} 包含缺陷对象的数组 (AgvFlaw[])
 */
export const getFlawList = (taskId) => {
  // 调用真实接口: GET /agv/flaw/list
  return apiClient.get('/agv/flaw/list', {
    params: { taskId } // 将taskId作为查询参数
  });
};

/**
 * 获取指定ID的缺陷详情
 * @param {string | number} flawId 缺陷ID
 * @returns {Promise<Object>} 包含缺陷详情的对象 (AgvFlaw)
 */
export const getFlawDetails = (flawId) => {
    // 调用真实接口: GET /agv/flaw/{id}
    return apiClient.get(`/agv/flaw/${flawId}`);
};

/**
 * 更新一个缺陷的信息 (例如，确认缺陷、添加备注等)
 * @param {Object} flawData 包含要更新的缺陷数据的对象 (AgvFlaw)
 * @returns {Promise<any>} API的响应
 */
export const updateFlaw = (flawData) => {
    // 调用真实接口: PUT /agv/flaw
    return apiClient.put('/agv/flaw', flawData);
};

/**
 * 控制AGV移动
 * @param {boolean} isMoving true表示前进, false表示停止
 * @returns {Promise<any>} API的响应
 */
export const controlAgv = (isMoving) => {
    // 根据UI的简单开关，映射到后端的 start/stop 动作
    const action = isMoving ? 'start' : 'stop';
    // 调用真实接口: POST /agv/control/{action}
    return apiClient.post(`/agv/control/${action}`);
};

/**
 * 标记任务为“已完成”
 * @param {string | number} taskId 任务ID
 * @returns {Promise<any>} API的响应
 */
export const completeTask = (taskId) => {
    // 调用真实接口: POST /agv/task/complete/{taskId}
    return apiClient.post(`/agv/task/complete/${taskId}`);
};

/**
 * 终止一个正在执行的任务
 * @param {string | number} taskId 任务ID
 * @returns {Promise<any>} API的响应
 */
export const terminateTask = (taskId) => {
    // 调用真实接口: POST /agv/task/terminate/{taskId}
    return apiClient.post(`/agv/task/terminate/${taskId}`);
};
