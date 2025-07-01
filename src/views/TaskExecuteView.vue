<template>
  <div class="app-container">
    <div class="breadcrumb">
      地铁隧道巡线车智能巡检系统 <span>/</span> 任务列表 <span>/</span> 任务巡视
    </div>

    <div class="main-container">
      <div class="content-area">
        <div class="video-area" id="video-container">
          <!-- 优化：音频控制面板现在集成在右下角 -->
          <div class="audio-panel">
            <button class="audio-btn" @click="toggleMute">
              <!-- 根据静音状态显示不同图标 -->
              <svg v-if="isMuted" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><line x1="23" y1="9" x2="17" y2="15"></line><line x1="17" y1="9" x2="23" y2="15"></line></svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon><path d="M19.07 4.93a10 10 0 0 1 0 14.14M15.54 8.46a5 5 0 0 1 0 7.07"></path></svg>
            </button>
            <input type="range" min="0" max="100" v-model="volumeLevel" class="volume-slider" @input="setVolume" />
          </div>
        </div>

        <div class="scale-bar-area">
          <div class="scale-bar-wrapper">
            <div class="scale-bar-text start">0m</div>
            <div class="scale-bar-text end">{{ totalDistance }}m</div>
            <div class="scale-bar">
              <div class="scale-bar-progress" :style="{ width: progressPercentage + '%' }"></div>
            </div>
            <div
              v-for="flaw in flaws"
              :key="flaw.id"
              class="scale-bar-item scale-bar-flaw"
              :class="{ unconfirmed: !flaw.confirmed }"
              :style="{ left: (flaw.flawDistance / totalDistance) * 100 + '%' }"
              :title="flaw.flawName"
              @click="viewFlawDetail(flaw)"
            >📍</div>
            <div
              class="scale-bar-item scale-bar-agv"
              :style="{ left: progressPercentage + '%' }"
              title="当前位置"
            >🚛</div>
          </div>
        </div>
      </div>

      <div class="sidebar">
        <!-- 其他卡片保持不变 -->
        <div class="card">
          <div class="card-header">
            控制台
          </div>
          <div class="card-body">
            <div class="control-buttons">
              <button class="btn btn-primary" @click="refreshMonitor">刷新监控</button>
              <select class="cam-selector" v-model="selectedCameraId">
                <option v-if="cameras.length === 0" disabled>加载中...</option>
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

    <!-- Modal Dialog保持不变 -->
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
// 导入更新后的API函数
import { getDeviceList, getTaskDetails, getFlawList, getFlawDetails, updateFlaw, controlAgv, completeTask, terminateTask } from '@/api/vehicle.js';

const player = ref(null);
const currentTaskId = ref('2');
const taskNumber = ref('加载中...');
const totalDistance = ref(0);
const distance = ref(0);
const systemTime = ref(new Date().toISOString());
const isAgvActive = ref(false);
const flaws = ref([]);
const isModalVisible = ref(false);
const selectedFlaw = ref(null);
const selectedCameraId = ref(null); // 默认不选中任何摄像头
const cameras = ref([]); // 默认摄像头列表为空，将由API填充
const isMuted = ref(true); // 音频默认静音
const volumeLevel = ref(50); // 默认音量

let taskPollInterval = null;
let flawPollInterval = null;

const progressPercentage = computed(() => totalDistance.value === 0 ? 0 : Math.min((distance.value / totalDistance.value) * 100, 100));
const formattedSystemTime = computed(() => new Date(systemTime.value).toLocaleString('zh-CN'));
const confirmedFlawCount = computed(() => flaws.value.filter(f => f.confirmed).length);
const unconfirmedFlawCount = computed(() => flaws.value.filter(f => !f.confirmed).length);
const currentCamera = computed(() => cameras.value.find(c => c.id === selectedCameraId.value) || {});

const initPlayer = (videoUrl) => {
  if (typeof window.EasyPlayer === 'undefined' || !window.EasyPlayer) {
    console.error("EasyPlayer.js 脚本尚未加载或加载失败!");
    return;
  }
  if (player.value) { player.value.destroy(); player.value = null; }
  if (!videoUrl) { console.warn("视频地址为空，无法初始化播放器。"); return; }
  console.log(`正在初始化播放器，地址: ${videoUrl}`);
  player.value = new window.EasyPlayer.Player({
    el: '#video-container', url: videoUrl, autoplay: true, live: true,
    decode_type: 'auto', show_audio_bar: false,
  });
};

// **音频控制方法**
const toggleMute = () => {
  isMuted.value = !isMuted.value;
  if (player.value) {
    isMuted.value ? player.value.mute() : player.value.unmute();
  }
};

const setVolume = () => {
  if (player.value) {
    player.value.setVolume(volumeLevel.value / 100);
    // 如果之前是静音，拖动音量条则自动取消静音
    if (isMuted.value && volumeLevel.value > 0) {
        isMuted.value = false;
    } else if (!isMuted.value && volumeLevel.value == 0) {
        isMuted.value = true;
    }
  }
};

const refreshMonitor = () => {
    console.log("手动刷新监控...");
    const cam = currentCamera.value;
    if (cam && cam.url) { initPlayer(cam.url); }
    else { console.error("当前摄像头没有有效的URL，无法刷新。"); }
};

const pollTaskDetails = async () => {
  if (!currentTaskId.value) return;
  try {
    const taskData = await getTaskDetails(currentTaskId.value);
    if (taskData) {
      taskNumber.value = taskData.taskNumber;
      totalDistance.value = taskData.totalDistance;
      distance.value = taskData.currentDistance;
      systemTime.value = taskData.updateTime;
      isAgvActive.value = taskData.status === '1';
    }
  } catch (error) {
    console.error("轮询任务详情失败:", error);
  }
};

const pollFlawList = async () => {
  if (!currentTaskId.value) return;
  try {
    const newFlaws = await getFlawList(currentTaskId.value);
    if (newFlaws && Array.isArray(newFlaws)) {
        flaws.value = newFlaws;
        if (!isModalVisible.value) {
            const unshownFlaw = newFlaws.find(f => !f.shown);
            if (unshownFlaw) {
                console.log(`发现新的未提示故障: ${unshownFlaw.flawName}`);
                viewFlawDetail(unshownFlaw);
            }
        }
    }
  } catch (error) {
    console.error("轮询缺陷列表失败:", error);
  }
};

const viewFlawDetail = async (flaw) => {
  try {
    const flawDetails = await getFlawDetails(flaw.id);
    if(flawDetails){
        selectedFlaw.value = flawDetails;
        isModalVisible.value = true;
    }
  } catch (error) {
    console.error("获取缺陷详情失败:", error);
    console.error('获取缺陷详情失败!');
  }
};

const markFlawAsShown = async () => {
    if (!selectedFlaw.value) return;
    if (selectedFlaw.value.shown === false) {
        try {
            selectedFlaw.value.shown = true;
            await updateFlaw(selectedFlaw.value);
            console.log(`缺陷 ${selectedFlaw.value.id} 已标记为“已提示”`);
        } catch (error) {
            console.error("标记缺陷为已读失败:", error);
        }
    }
};

const closeFlawModal = async () => {
  await markFlawAsShown();
  isModalVisible.value = false;
  selectedFlaw.value = null;
};

const handleUpdateFlaw = async () => {
    if (!selectedFlaw.value) return;
    try {
        selectedFlaw.value.shown = true;
        await updateFlaw(selectedFlaw.value);
        console.log('缺陷信息更新成功!');
        closeFlawModal();
        pollFlawList();
    } catch(error) {
        console.error("更新缺陷失败:", error);
        console.error('更新缺陷失败!');
    }
};

const handleCompleteTask = async () => {
    const confirmed = window.prompt("您确定要完成当前巡检任务吗? 请输入 'yes' 确认。");
    if (confirmed === 'yes') {
        try {
            await completeTask(currentTaskId.value);
            console.log('任务已完成!');
        } catch (error) {
            console.error("完成任务失败:", error);
        }
    }
};

const handleTerminateTask = async () => {
    const confirmed = window.prompt("您确定要终止当前巡检任务吗? 此操作不可恢复! 请输入 'yes' 确认。");
    if (confirmed === 'yes') {
        try {
            await terminateTask(currentTaskId.value);
            console.log('任务已终止!');
        } catch (error) {
            console.error("终止任务失败:", error);
        }
    }
};

onMounted(async () => {
  try {
    const deviceData = await getDeviceList();
    // **改动点：更稳健地解析摄像头数据**
    // 检查 deviceData 和 deviceData.Devices
    const deviceList = deviceData?.Devices || (Array.isArray(deviceData) ? deviceData : []);

    if (deviceList.length > 0) {
        cameras.value = deviceList.map(device => ({
            id: device.ID,
            name: device.Name,
            // **改动点：使用代理路径而不是硬编码IP**
            url: `/live/${device.ID}_01.flv`
        }));

        if (cameras.value.length > 0) {
            selectedCameraId.value = cameras.value[0].id;
            // Watcher会自动初始化播放器
        }
    } else {
        console.warn("从API获取到的摄像头列表为空。");
    }
  } catch (error) {
    console.error("获取摄像头列表失败:", error);
  }

  await pollTaskDetails();
  await pollFlawList();
  taskPollInterval = setInterval(pollTaskDetails, 3000);
  flawPollInterval = setInterval(pollFlawList, 10000);
});

onUnmounted(() => {
  if (player.value) { player.value.destroy(); }
  clearInterval(taskPollInterval);
  clearInterval(flawPollInterval);
});

watch(isAgvActive, async (newValue, oldValue) => {
  if (newValue !== oldValue) {
    try {
      await controlAgv(newValue);
      console.log(`发送AGV控制命令: ${newValue ? '前进' : '停止'}`);
    } catch (error) {
      console.error("发送AGV控制命令失败:", error);
      console.error('控制车辆失败!');
      isAgvActive.value = oldValue;
    }
  }
});

watch(selectedCameraId, (newId) => {
    const newCam = cameras.value.find(c => c.id === newId);
    if (newCam) { initPlayer(newCam.url); }
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

.audio-panel {
    position: absolute;
    bottom: 15px;
    right: 15px;
    background: rgba(25, 25, 25, 0.75);
    border-radius: 8px;
    padding: 10px 15px;
    display: flex;
    align-items: center;
    gap: 12px;
    z-index: 10;
    border: 1px solid rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
}

.audio-btn {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    padding: 0;
    display: flex;
    align-items: center;
}

.volume-slider {
    -webkit-appearance: none;
    appearance: none;
    width: 120px;
    height: 4px;
    background: #555;
    outline: none;
    border-radius: 2px;
    transition: opacity 0.2s;
}

.volume-slider::-webkit-slider-thumb {
    -webkit-appearance: none;
    appearance: none;
    width: 16px;
    height: 16px;
    background: #fff;
    cursor: pointer;
    border-radius: 50%;
}

.volume-slider::-moz-range-thumb {
    width: 16px;
    height: 16px;
    background: #fff;
    cursor: pointer;
    border-radius: 50%;
    border: none;
}

</style>
