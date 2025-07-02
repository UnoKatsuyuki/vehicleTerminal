// 从接口文档中获取的基础 URL
const BASE_URL = 'http://192.168.2.57/prod-api';

/**
 * 封装的 fetch 请求函数
 * @param {string} endpoint - 请求的 API 路径 (例如 /system/check/fs)
 * @returns {Promise<any>} - 返回解析后的 JSON 数据
 */
const apiClient = async (endpoint) => {
    const response = await fetch(`${BASE_URL}${endpoint}`);

    // 如果 HTTP 状态码不是 2xx (例如 404, 500), 抛出错误
    if (!response.ok) {
        throw new Error(`网络响应错误: ${response.status} ${response.statusText}`);
    }

    // 解析 JSON 响应体并返回
    return response.json();
};

/**
 * 4. 系统检查相关接口 (真实 API 调用)
 */

// 检查文件系统可用性
export const checkFs = () => {
    return apiClient('/system/check/fs');
};

// 检查数据库连接
export const checkDb = () => {
    return apiClient('/system/check/db');
};

// 检查AGV连接
export const checkAgv = () => {
    return apiClient('/system/check/agv');
};

// 检查摄像头连接
export const checkCam = () => {
    return apiClient('/system/check/cam');
};
