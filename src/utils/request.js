import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

// 创建 axios 实例，用于请求主服务 /prod-api
const service = axios.create({
  baseURL: '/prod-api', // API 的基础 URL
  timeout: 10000, // 请求超时时间
});

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 在这里可以添加认证token等
    // const token = getToken();
    // if (token) {
    //   config.headers['Authorization'] = 'Bearer ' + token;
    // }
    return config;
  },
  error => {
    console.log(error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  /**
   * 统一处理后端返回的数据格式
   * - AjaxResult: { code, msg, data }
   * - TableDataInfo: { code, msg, rows, total }
   */
  response => {
    const res = response.data;
    // code 不为 200 则判定为错误（支持字符串和数字类型）
    if (res.code !== 200 && res.code !== '200') {
      ElMessage({
        message: res.msg || 'Error',
        type: 'error',
        duration: 5 * 1000
      });

      // 这里可以根据不同的code做不同的处理，例如 50008: 非法token; 50012: 其他客户端登录; 50014: Token 过期;
      if (res.code === 50008 || res.code === 50012 || res.code === 50014) {
        // 重新登录
        ElMessageBox.confirm('会话已失效，您可以取消停留在该页面，或者重新登录', '确认登出', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 这里处理登出逻辑，例如跳转到登录页
          // location.reload();
        });
      }
      return Promise.reject(new Error(res.msg || 'Error'));
    } else {
      // code 为 200
      // 如果响应中包含 'rows' 和 'total'，我们假定它是分页数据 (TableDataInfo)
      // 并返回整个响应体，因为调用方需要访问 'total' 和 'rows'。
      if (Object.prototype.hasOwnProperty.call(res, 'rows') && Object.prototype.hasOwnProperty.call(res, 'total')) {
        return res;
      }

      // 否则，我们假定它是一个标准的 AjaxResult，并返回 'data' 字段。
      return res.data;
    }
  },
  error => {
    console.log('err' + error); // for debug
    ElMessage({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    });
    return Promise.reject(error);
  }
);

export default service;
