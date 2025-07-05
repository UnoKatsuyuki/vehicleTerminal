// 数据源管理器
import { ref, reactive } from 'vue';

// 数据源类型枚举
export const DataSourceType = {
  LOCAL: 'local',
  VEHICLE: 'vehicle'
};

// 数据源配置
const dataSourceConfigs = {
  [DataSourceType.LOCAL]: {
    name: '本地数据源',
    description: '使用本地数据库和后端接口',
    baseUrl: '/local-api',
    apiBaseUrl: 'http://localhost:8080',
    isMock: false
  },
  [DataSourceType.VEHICLE]: {
    name: '小车数据源',
    description: '连接真实的小车设备获取数据',
    baseUrl: '/prod-api',
    apiBaseUrl: 'http://192.168.2.57/prod-api',
    cameraApiBaseUrl: 'http://192.168.2.57/easy-api',
    streamMediaUrl: 'http://192.168.2.57/webrtc-api',
    isMock: false
  }
};

// 全局状态管理
const currentDataSource = ref(DataSourceType.VEHICLE); // 默认使用小车数据源
const isDataSourceChanging = ref(false);

// 数据源状态
const dataSourceState = reactive({
  isConnected: false,
  lastConnectionTime: null,
  connectionError: null,
  vehicleConfig: null
});

// 获取当前数据源配置
export const getCurrentDataSourceConfig = () => {
  return dataSourceConfigs[currentDataSource.value];
};

// 获取所有可用的数据源配置
export const getAllDataSourceConfigs = () => {
  return dataSourceConfigs;
};

// 切换数据源
export const switchDataSource = async (newDataSourceType) => {
  if (currentDataSource.value === newDataSourceType) {
    return { success: true, message: '数据源已经是目标类型' };
  }

  if (!dataSourceConfigs[newDataSourceType]) {
    return { success: false, message: '不支持的数据源类型' };
  }

  isDataSourceChanging.value = true;

  try {
    // 保存当前数据源类型到本地存储
    localStorage.setItem('currentDataSource', newDataSourceType);

    // 更新当前数据源
    currentDataSource.value = newDataSourceType;

    // 重置连接状态
    dataSourceState.isConnected = false;
    dataSourceState.connectionError = null;

    // 如果是小车数据源，尝试连接
    if (newDataSourceType === DataSourceType.VEHICLE) {
      await testVehicleConnection();
    } else {
      // 本地数据源直接设置为已连接
      dataSourceState.isConnected = true;
      dataSourceState.lastConnectionTime = new Date();
    }

    return {
      success: true,
      message: `已切换到${dataSourceConfigs[newDataSourceType].name}`
    };
  } catch (error) {
    console.error('切换数据源失败:', error);
    return {
      success: false,
      message: `切换数据源失败: ${error.message}`
    };
  } finally {
    isDataSourceChanging.value = false;
  }
};

// 测试小车连接
export const testVehicleConnection = async () => {
  try {
    const config = getCurrentDataSourceConfig();
    const response = await fetch(`${config.apiBaseUrl}/system/check/agv`, {
      method: 'GET',
      timeout: 5000
    });

    if (response.ok) {
      dataSourceState.isConnected = true;
      dataSourceState.lastConnectionTime = new Date();
      dataSourceState.connectionError = null;
      return true;
    } else {
      throw new Error(`连接失败: ${response.status}`);
    }
  } catch (error) {
    dataSourceState.isConnected = false;
    dataSourceState.connectionError = error.message;
    throw error;
  }
};

// 获取数据源状态
export const getDataSourceState = () => {
  return {
    currentDataSource: currentDataSource.value,
    config: getCurrentDataSourceConfig(),
    isChanging: isDataSourceChanging.value,
    state: { ...dataSourceState }
  };
};

// 初始化数据源
export const initializeDataSource = () => {
  // 从本地存储恢复数据源类型，如果没有保存过则使用默认的小车数据源
  const savedDataSource = localStorage.getItem('currentDataSource');
  if (savedDataSource && dataSourceConfigs[savedDataSource]) {
    currentDataSource.value = savedDataSource;
  } else {
    // 如果没有保存过，默认使用小车数据源
    currentDataSource.value = DataSourceType.VEHICLE;
    localStorage.setItem('currentDataSource', DataSourceType.VEHICLE);
  }

  // 如果是小车数据源，尝试连接但不自动切换
  if (currentDataSource.value === DataSourceType.VEHICLE) {
    testVehicleConnection().catch(error => {
      console.warn('小车连接失败，但保持小车数据源设置:', error);
      // 不自动切换，保持小车数据源设置，只是连接状态为false
    });
  } else {
    // 本地数据源直接设置为已连接
    dataSourceState.isConnected = true;
    dataSourceState.lastConnectionTime = new Date();
  }
};

// 导出状态
export const useDataSourceState = () => {
  return {
    currentDataSource,
    isDataSourceChanging,
    dataSourceState
  };
};

// 自动初始化
initializeDataSource();
