// API管理器 - 根据数据源自动选择API
import { getCurrentDataSourceConfig, DataSourceType } from '@/utils/dataSourceManager';
import * as carApi from './carApi.js';
import * as jincangApi from './jincangApi.js';

// 获取当前数据源配置
const getCurrentConfig = () => {
  return getCurrentDataSourceConfig();
};

// 根据当前数据源选择API
const getApi = () => {
  const config = getCurrentConfig();
  console.log('当前数据源配置:', config);
  console.log('数据源名称:', config?.name);
  const api = config && config.name === '本地数据源' ? jincangApi : carApi;
  console.log('选择的API:', api === jincangApi ? '本地API' : '小车API');
  return api;
};

// 任务管理相关API - 使用jincangApi.js中的方法名
export const listTask = (params) => {
  const api = getApi();
  // 根据数据源选择正确的方法名
  if (api === jincangApi) {
    return api.getTaskList(params);
  } else {
    return api.listTask(params);
  }
};

export const getTask = (id) => {
  const api = getApi();
  if (api === jincangApi) {
    return api.getTaskDetail(id);
  } else {
    return api.getTask(id);
  }
};

export const addTask = (data) => {
  const api = getApi();
  return api.addTask(data);
};

export const updateTask = (data) => {
  const api = getApi();
  return api.updateTask(data);
};

export const delTask = (id) => {
  const api = getApi();
  if (api === jincangApi) {
    return api.deleteTask(id);
  } else {
    return api.delTask(id);
  }
};

export const preUploadTask = (id) => {
  const api = getApi();
  return api.preUploadTask(id);
};

export const uploadTask = (id) => {
  const api = getApi();
  return api.uploadTask(id);
};

export const startTask = (id) => {
  const api = getApi();
  return api.startTask(id);
};

export const endTask = (id, isAbort = false) => {
  const api = getApi();
  return api.endTask(id, isAbort);
};

// 故障管理相关API - 使用jincangApi.js中的方法名
export const listFlaw = (params) => {
  const api = getApi();
  if (api === jincangApi) {
    return api.getFlawList(params);
  } else {
    return api.listFlaw(params);
  }
};

export const getFlaw = (id) => {
  const api = getApi();
  if (api === jincangApi) {
    return api.getFlawDetail(id);
  } else {
    return api.getFlaw(id);
  }
};

export const addFlaw = (data) => {
  const api = getApi();
  return api.addFlaw(data);
};

export const updateFlaw = (data) => {
  const api = getApi();
  return api.updateFlaw(data);
};

export const delFlaw = (id) => {
  const api = getApi();
  if (api === jincangApi) {
    return api.deleteFlaw(id);
  } else {
    return api.delFlaw(id);
  }
};

export const confirmFlaw = (id) => {
  const api = getApi();
  return api.confirmFlaw(id);
};

export const uploadFlaw = (id) => {
  const api = getApi();
  return api.uploadFlaw(id);
};

// 实时数据接口 - 根据数据源选择
export const getLiveFlawInfo = (taskId) => {
  const api = getApi();
  if (api === jincangApi) {
    // 本地数据源使用getFlawList获取故障信息，添加默认分页参数
    return api.getFlawList({
      taskId,
      pageNum: 1,
      pageSize: 1000
    });
  } else {
    // 小车数据源使用实时接口
    return carApi.getLiveFlawInfo(taskId);
  }
};

export const checkAllFlawsConfirmed = (taskId) => {
  const api = getApi();
  return api.checkAllFlawsConfirmed(taskId);
};

// 以下方法jincangApi.js中没有，使用carApi.js
export const getDeviceList = () => {
  return carApi.getDeviceList();
};

export const getVideoStreamUrl = (deviceId) => {
  return carApi.getVideoStreamUrl(deviceId);
};

export const agvForward = () => {
  return carApi.agvForward();
};

export const agvStop = () => {
  return carApi.agvStop();
};

export const agvBackward = () => {
  return carApi.agvBackward();
};

export const getAgvHeartbeat = () => {
  return carApi.getAgvHeartbeat();
};

// 任务详情接口 - 根据数据源选择
export const getTaskDetails = (taskId) => {
  const api = getApi();
  if (api === jincangApi) {
    // 本地数据源使用getTaskDetail
    return api.getTaskDetail(taskId);
  } else {
    // 小车数据源使用getTaskDetails
    return carApi.getTaskDetails(taskId);
  }
};

// 故障列表接口 - 根据数据源选择
export const getFlawList = (taskId) => {
  const api = getApi();
  if (api === jincangApi) {
    // 本地数据源使用getFlawList，添加默认分页参数
    return api.getFlawList({
      taskId,
      pageNum: 1,
      pageSize: 1000
    });
  } else {
    // 小车数据源使用getFlawList
    return carApi.getFlawList(taskId);
  }
};

// 故障详情接口 - 根据数据源选择
export const getFlawDetails = (flawId) => {
  const api = getApi();
  if (api === jincangApi) {
    // 本地数据源使用getFlawDetail
    return api.getFlawDetail(flawId);
  } else {
    // 小车数据源使用getFlawDetails
    return carApi.getFlawDetails(flawId);
  }
};

export const updateFlaw_vehicle = (flawData) => {
  return carApi.updateFlaw_vehicle(flawData);
};

// 系统检查接口 - 只使用carApi.js
export const checkFs = () => {
  return carApi.checkFs();
};

export const checkDb = () => {
  return carApi.checkDb();
};

export const checkAgv = () => {
  return carApi.checkAgv();
};

export const checkCam = () => {
  return carApi.checkCam();
};

// 获取当前数据源信息
export const getCurrentDataSourceInfo = () => {
  const config = getCurrentConfig();
  return {
    name: config.name,
    description: config.description,
    isLocal: config.name === '本地数据源'
  };
};
