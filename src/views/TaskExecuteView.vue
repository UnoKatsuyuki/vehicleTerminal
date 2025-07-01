<template>
  <div class="app-container">
    <div class="breadcrumb">
      地铁隧道巡线车智能巡检系统 <span>/</span> 任务列表 <span>/</span> 任务巡视
    </div>

    <div class="main-container">
      <div class="content-area">
        <div class="video-area" id="video-container">
          <div style="text-align: center;">
            实时视频流显示区域<br />
            <small style="color: #ccc;">{{ currentCamera.name }}</small>
          </div>
          <div class="audio-stream" style="background: rgba(0, 0, 0, 0.5); padding: 10px; border-radius: 4px;">
            音频控制面板
          </div>
        </div>

        <div class="scale-bar-area">
          <div class="scale-bar-wrapper">
            <div class="scale-bar-text start">0m</div>
            <div class="scale-bar-text end">{{ totalDistance }}m</div>
            <div class="scale-bar">
              <div class="scale-bar-progress" :style="{ width: progressPercentage + '%' }"></div>
            </div>
            <!-- Dynamically generate flaw markers -->
            <div
              v-for="flaw in flaws"
              :key="flaw.id"
              class="scale-bar-item scale-bar-flaw"
              :class="{ unconfirmed: !flaw.confirmed }"
              :style="{ left: (flaw.flawDistance / totalDistance) * 100 + '%' }"
              :title="flaw.flawName"
              @click="viewFlawDetail(flaw)"
            >📍</div>
            <!-- AGV Marker -->
            <div
              class="scale-bar-item scale-bar-agv"
              :style="{ left: progressPercentage + '%' }"
              title="当前位置"
            >🚛</div>
          </div>
        </div>
      </div>

      <div class="sidebar">
        <div class="card">
          <div class="card-header">
            控制台
          </div>
          <div class="card-body">
            <div class="control-buttons">
              <button class="btn btn-primary" @click="refreshMonitor">刷新监控</button>
              <select class="cam-selector" v-model="selectedCameraId">
                <option v-for="cam in cameras" :key="cam.id" :value="cam.id">{{ cam.name }}</option>
              </select>
              <button class="btn btn-success" @click="handleCompleteTask">完成巡检</button>
              <button class="btn btn-danger" @click="handleTerminateTask">终止巡检</button>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            车辆状态
            <label class="switch">
              <input type="checkbox" v-model="isAgvActive" />
              <span class="slider"></span>
            </label>
          </div>
          <div class="card-body">
            <div class="info-item">
              <div class="info-label">📄 巡视任务编号</div>
              <div class="info-value">{{ taskNumber }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">⏰ 车辆系统时间</div>
              <div class="info-value">{{ formattedSystemTime }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">📍 已行驶距离</div>
              <div class="info-value"><span class="count-animation">{{ distance.toFixed(2) }}</span> 米</div>
            </div>
            <div class="info-item">
              <div class="info-label">⚠️ 故障总计</div>
              <div class="info-value">{{ flaws.length }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">✅ 已确定故障</div>
              <div class="info-value confirmed-flaw">{{ confirmedFlawCount }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">❓ 疑似故障</div>
              <div class="info-value unconfirmed-flaw">{{ unconfirmedFlawCount }}</div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">故障历史</div>
          <div class="card-body">
            <table class="flaw-table">
              <thead>
                <tr>
                  <th>故障名称</th>
                  <th>故障类型</th>
                  <th>故障位置</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="flaws.length === 0">
                  <td colspan="3" style="text-align: center; color: #999;">暂无故障记录</td>
                </tr>
                <tr v-for="flaw in flaws" :key="flaw.id" :class="{ confirmed: flaw.confirmed, unconfirmed: !flaw.confirmed }" @click="viewFlawDetail(flaw)">
                  <td><a href="#" class="link">{{ flaw.flawName }}</a></td>
                  <td>{{ flaw.flawType }}</td>
                  <td>{{ flaw.flawDistance }}m</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Dialog for Flaw Details -->
    <div class="modal" :class="{ show: isModalVisible }">
      <div class="modal-content">
        <div class="modal-header">
          故障详情
        </div>
        <div class="modal-body" v-if="selectedFlaw">
          <div class="modal-image">
            <img v-if="selectedFlaw.flawImageUrl" :src="selectedFlaw.flawImageUrl" alt="故障图片" style="max-width: 100%; max-height: 100%; object-fit: contain;">
            <span v-else>暂无图片</span>
          </div>
          <div class="modal-info">
            <div class="card">
              <div class="card-header">故障信息</div>
              <div class="card-body">
                <div class="info-item"><div class="info-label">故障名称</div><div class="info-value">{{ selectedFlaw.flawName }}</div></div>
                <div class="info-item"><div class="info-label">故障类型</div><div class="info-value">{{ selectedFlaw.flawType }}</div></div>
                <div class="info-item"><div class="info-label">故障描述</div><div class="info-value">{{ selectedFlaw.flawDesc }}</div></div>
                <div class="info-item"><div class="info-label">故障位置</div><div class="info-value">{{ selectedFlaw.flawDistance }}m</div></div>
                <div class="info-item">
                  <div class="info-label">是否属实</div>
                  <div class="info-value">
                    <label><input type="radio" name="confirmed" :value="true" v-model="selectedFlaw.confirmed" /> 是</label>
                    <label style="margin-left: 20px;"><input type="radio" name="confirmed" :value="false" v-model="selectedFlaw.confirmed" /> 否</label>
                  </div>
                </div>
                <div class="info-item">
                  <div class="info-label">补充说明</div>
                  <div class="info-value">
                    <textarea v-model="selectedFlaw.remark" style="width: 100%; height: 80px; padding: 8px; border: 1px solid #ddd; border-radius: 4px; resize: vertical;" placeholder="请输入内容"></textarea>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div style="text-align: right; margin-top: 20px;">
          <button class="btn" @click="closeFlawModal">取 消</button>
          <button class="btn btn-primary" style="margin-left: 10px;" @click="handleUpdateFlaw">确 定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
// 导入我们最终确定的API函数
import {
  getSystemConfig,
  getTaskDetails,
  getFlawList,
  getFlawDetails,
  updateFlaw,
  controlAgv,
  completeTask,
  terminateTask
} from '@/api/vehicle.js';

// --- 核心状态 ---
const currentTaskId = ref('2'); // 假设当前任务ID为2，后续可以从路由获取
const taskNumber = ref('加载中...');
const totalDistance = ref(0);
const distance = ref(0);
const systemTime = ref(new Date().toISOString());
const isAgvActive = ref(false); // 默认车辆为停止状态
const flaws = ref([]);
const player = ref(null);

// --- UI状态 ---
const isModalVisible = ref(false);
const selectedFlaw = ref(null);
const selectedCameraId = ref('cam1');
const cameras = ref([ // 默认摄像头列表，将被API数据覆盖
    { id: 'cam1', name: '摄像头1', url: '' },
    { id: 'cam2', name: '摄像头2', url: '' },
    { id: 'cam3', name: '摄像头3', url: '' },
    { id: 'cam4', name: '摄像头4', url: '' },
]);

// --- 定时器 ---
let taskPollInterval = null;
let flawPollInterval = null;

// --- 计算属性 ---
const progressPercentage = computed(() => {
  if (totalDistance.value === 0) return 0;
  return Math.min((distance.value / totalDistance.value) * 100, 100);
});

const formattedSystemTime = computed(() => new Date(systemTime.value).toLocaleString('zh-CN'));
const confirmedFlawCount = computed(() => flaws.value.filter(f => f.confirmed).length);
const unconfirmedFlawCount = computed(() => flaws.value.filter(f => !f.confirmed).length);
const currentCamera = computed(() => cameras.value.find(c => c.id === selectedCameraId.value) || {});

// --- 方法 ---

// 轮询任务详情，以更新车辆位置和状态
const pollTaskDetails = async () => {
  if (!currentTaskId.value) return;
  try {
    const taskData = await getTaskDetails(currentTaskId.value);
    taskNumber.value = taskData.taskNumber;
    totalDistance.value = taskData.totalDistance;
    distance.value = taskData.currentDistance;
    systemTime.value = taskData.updateTime; // 假设任务对象包含更新时间
    isAgvActive.value = taskData.status === '1'; // 假设'1'为巡视中
  } catch (error) {
    console.error("轮询任务详情失败:", error);
  }
};

const pollFlawList = async () => {
  if (!currentTaskId.value) return;
  try {
    const newFlaws = await getFlawList(currentTaskId.value);
    flaws.value = newFlaws; // 更新UI列表

    // 如果当前没有弹窗，则检查是否有新的未提示故障
    if (!isModalVisible.value) {
      const unshownFlaw = newFlaws.find(f => !f.shown);
      if (unshownFlaw) {
        console.log(`发现新的未提示故障: ${unshownFlaw.flawName}`);
        viewFlawDetail(unshownFlaw); // 自动弹出详情
      }
    }
  } catch (error) {
    console.error("轮询缺陷列表失败:", error);
  }
};

const viewFlawDetail = async (flaw) => {
  try {
    const flawDetails = await getFlawDetails(flaw.id);
    selectedFlaw.value = flawDetails;
    isModalVisible.value = true;
  } catch (error) {
    console.error("获取缺陷详情失败:", error);
    alert('获取缺陷详情失败!');
  }
};

const markFlawAsShown = async () => {
    if (!selectedFlaw.value) return;
    // 仅当该缺陷之前是未提示状态时，才去更新它
    if (selectedFlaw.value.shown === false) {
        try {
            selectedFlaw.value.shown = true; // 在本地立即更新
            await updateFlaw(selectedFlaw.value); // 发送API请求，将'shown'状态持久化到后端
            console.log(`缺陷 ${selectedFlaw.value.id} 已标记为“已提示”`);
        } catch (error) {
            console.error("标记缺陷为已读失败:", error);
            // 即使失败了，也要关闭弹窗，避免卡死
        }
    }
};


const closeFlawModal = async () => {
  await markFlawAsShown(); // 在关闭前，先标记为已读
  isModalVisible.value = false;
  selectedFlaw.value = null;
};

const handleUpdateFlaw = async () => {
    if (!selectedFlaw.value) return;
    try {
        selectedFlaw.value.shown = true; // 确认时，也要确保标记为已提示
        await updateFlaw(selectedFlaw.value);
        alert('缺陷信息更新成功!');
        closeFlawModal();
        pollFlawList(); // 重新获取列表以刷新状态
    } catch(error) {
        console.error("更新缺陷失败:", error);
        alert('更新缺陷失败!');
    }
};

const handleCompleteTask = async () => {
    if (confirm('您确定要完成当前巡检任务吗?')) {
        try {
            await completeTask(currentTaskId.value);
            alert('任务已完成!');
        } catch (error) {
            console.error("完成任务失败:", error);
            alert('完成任务失败!');
        }
    }
};

const handleTerminateTask = async () => {
    if (confirm('您确定要终止当前巡检任务吗? 此操作不可恢复!')) {
        try {
            await terminateTask(currentTaskId.value);
            alert('任务已终止!');
        } catch (error) {
            console.error("终止任务失败:", error);
            alert('终止任务失败!');
        }
    }
};

const refreshMonitor = () => {
    console.log("刷新监控...");
    // 视频播放器相关的刷新逻辑将在这里实现
};

// --- 生命周期钩子 ---
onMounted(async () => {
  // 1. 获取系统配置（摄像头等）
  try {
    const config = await getSystemConfig();
    cameras.value = [
        { id: 'cam1', name: '摄像头1', url: config.cam1 },
        { id: 'cam2', name: '摄像头2', url: config.cam2 },
        { id: 'cam3', name: '摄像头3', url: config.cam3 },
        { id: 'cam4', name: '摄像头4', url: config.cam4 },
    ];
    // **获取配置后，立即初始化第一个摄像头的播放器**
    initPlayer(cameras.value[0]?.url);
  } catch (error) {
    console.error("获取系统配置失败:", error);
  }

  // 2. 立即执行一次轮询以快速加载初始数据
  await pollTaskDetails();
  await pollFlawList();

  // 3. 启动定时轮询
  taskPollInterval = setInterval(pollTaskDetails, 3000);
  flawPollInterval = setInterval(pollFlawList, 10000);
});

onUnmounted(() => {
  // 组件销毁时，销毁播放器并清除定时器
  if (player.value) {
    player.value.destroy();
  }
  clearInterval(taskPollInterval);
  clearInterval(flawPollInterval);
});

// --- 视频播放器相关方法 ---
const initPlayer = (videoUrl) => {
  // 销毁旧的播放器实例
  if (player.value) {
    player.value.destroy();
    player.value = null;
  }

  // 如果没有视频地址，则不初始化
  if (!videoUrl) {
    console.warn("视频地址为空，无法初始化播放器。");
    return;
  }

  console.log(`正在初始化播放器，地址: ${videoUrl}`);

  // 创建新的EasyPlayer实例
  player.value = new window.EasyPlayer.Player({
    el: '#video-container', // 挂载点
    url: videoUrl,          // 视频流地址
    autoplay: true,         // 自动播放
    live: true,             // 直播模式
    decode_type: 'auto',    // 自动选择解码方式
    show_audio_bar: false,  // 不显示音频条
  });
};

const refreshMonitor = () => {
    console.log("手动刷新监控...");
    const cam = currentCamera.value;
    if (cam && cam.url) {
        initPlayer(cam.url);
    } else {
        console.error("当前摄像头没有有效的URL，无法刷新。");
    }
};

// --- 监视器 ---
watch(isAgvActive, async (newValue, oldValue) => {
  // 只有在值真的发生变化时才发送API请求
  if (newValue !== oldValue) {
    try {
      await controlAgv(newValue);
      console.log(`发送AGV控制命令: ${newValue ? '前进' : '停止'}`);
    } catch (error) {
      console.error("发送AGV控制命令失败:", error);
      alert('控制车辆失败!');
      // 如果API调用失败，将开关恢复到之前的状态
      isAgvActive.value = oldValue;
    }
  }
});

// **新增监视器：监视摄像头选择的变化**
watch(selectedCameraId, (newId) => {
    const newCam = cameras.value.find(c => c.id === newId);
    if (newCam) {
        // 当用户切换摄像头时，使用新的URL重新初始化播放器
        initPlayer(newCam.url);
    }
});
</script>

<style scoped>
/* 您的CSS样式保持不变 */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: Arial, sans-serif;
    background: #f5f5f5;
    height: 100vh;
    overflow: hidden;
}

.app-container {
    height: 100vh;
    display: flex;
    flex-direction: column;
    background: white;
}

.breadcrumb {
    padding: 20px;
    color: #666;
    font-size: 14px;
    border-bottom: 1px solid #eee;
}

.breadcrumb span {
    margin: 0 5px;
}

.main-container {
    flex: 1;
    display: flex;
    height: calc(100vh - 60px);
}

.content-area {
    flex: 1;
    display: flex;
    flex-direction: column;
}

.video-area {
    flex: 1;
    background: #000;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
}

.audio-stream {
    position: absolute;
    bottom: 10px;
    right: 10px;
    width: 200px;
}

.scale-bar-area {
    height: 120px;
    background: #fafafa;
    border-top: 1px solid #eee;
    padding: 20px;
    display: flex;
    flex-direction: column;
    justify-content: center;
}

.scale-bar-wrapper {
    position: relative;
    height: 60px;
}

.scale-bar {
    width: 100%;
    height: 8px;
    background: #e4e7ed;
    border-radius: 4px;
    position: relative;
    margin: 26px 0;
}

.scale-bar-progress {
    height: 100%;
    background: #409eff;
    border-radius: 4px;
    transition: width 1s ease;
}

.scale-bar-text {
    position: absolute;
    font-size: 12px;
    color: #666;
}

.scale-bar-text.start {
    left: 0;
    top: 0;
}

.scale-bar-text.end {
    right: 0;
    top: 0;
}

.scale-bar-item {
    position: absolute;
    top: 18px;
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    cursor: pointer;
    transform: translateX(-50%);
}

.scale-bar-flaw {
    background: #f56c6c;
    color: white;
}

.scale-bar-flaw.unconfirmed {
    background: #e6a23c;
    color: white;
}

.scale-bar-agv {
    background: #67c23a;
    color: white;
    font-size: 14px;
    animation: pulse 2s infinite;
}

@keyframes pulse {
    0% { transform: translateX(-50%) scale(1); }
    50% { transform: translateX(-50%) scale(1.1); }
    100% { transform: translateX(-50%) scale(1); }
}

.sidebar {
    width: 400px;
    background: white;
    border-left: 1px solid #eee;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
}

.card {
    border: 1px solid #eee;
    border-radius: 8px;
    margin: 10px;
    background: white;
}

.card-header {
    padding: 15px 20px;
    background: #fafafa;
    border-bottom: 1px solid #eee;
    font-weight: bold;
    font-size: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.card-body {
    padding: 20px;
}

.control-buttons {
    display: flex;
    justify-content: center;
    gap: 10px;
    flex-wrap: wrap;
}

.btn {
    padding: 8px 16px;
    border: 1px solid #ddd;
    border-radius: 4px;
    background: white;
    cursor: pointer;
    font-size: 14px;
}

.btn-primary {
    background: #409eff;
    border-color: #409eff;
    color: white;
}

.btn-success {
    background: #67c23a;
    border-color: #67c23a;
    color: white;
}

.btn-danger {
    background: #f56c6c;
    border-color: #f56c6c;
    color: white;
}

.switch {
    position: relative;
    display: inline-block;
    width: 60px;
    height: 34px;
}

.switch input {
    opacity: 0;
    width: 0;
    height: 0;
}

.slider {
    position: absolute;
    cursor: pointer;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: #ccc;
    transition: .4s;
    border-radius: 34px;
}

.slider:before {
    position: absolute;
    content: "";
    height: 26px;
    width: 26px;
    left: 4px;
    bottom: 4px;
    background-color: white;
    transition: .4s;
    border-radius: 50%;
}

input:checked + .slider {
    background-color: #67c23a;
}

input:checked + .slider:before {
    transform: translateX(26px);
}

.info-item {
    display: flex;
    margin-bottom: 15px;
    align-items: center;
}

.info-label {
    width: 140px;
    color: #666;
    font-size: 14px;
}

.info-value {
    flex: 1;
    color: #333;
    font-size: 14px;
}

.confirmed-flaw {
    color: #f56c6c;
    font-weight: bold;
}

.unconfirmed-flaw {
    color: #e6a23c;
    font-weight: bold;
}

.flaw-table {
    width: 100%;
    border-collapse: collapse;
}

.flaw-table th,
.flaw-table td {
    padding: 8px 12px;
    border: 1px solid #eee;
    text-align: left;
    font-size: 12px;
}

.flaw-table th {
    background: #fafafa;
    font-weight: bold;
}

.flaw-table tbody tr {
    cursor: pointer;
}
.flaw-table tbody tr:hover {
    background-color: #f5f7fa;
}

.flaw-table tbody tr.confirmed {
    background: #fef0f0;
}

.flaw-table tbody tr.unconfirmed {
    background: #fdf6ec;
}

.link {
    color: #409eff;
    text-decoration: none;
    cursor: pointer;
}

.link:hover {
    text-decoration: underline;
}

.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.5);
    display: none;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal.show {
    display: flex;
}

.modal-content {
    background: white;
    width: 1150px;
    border-radius: 8px;
    padding: 20px;
    max-height: 80vh;
    overflow-y: auto;
}

.modal-body {
    display: flex;
    gap: 20px;
}

.modal-image {
    width: 800px;
    height: 600px;
    background: #eee;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999;
    font-size: 18px;
    border-radius: 8px;
    border: 1px solid #ddd;
}

.modal-info {
    width: 300px;
}

.cam-selector {
    width: 120px;
    padding: 6px 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
}
</style>
