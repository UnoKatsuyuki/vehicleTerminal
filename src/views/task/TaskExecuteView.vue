<template>
  <div class="app-container">
    <div class="breadcrumb">
      地铁隧道巡线车智能巡检系统 <span>/</span> 任务列表 <span>/</span> 任务巡视
    </div>

    <div class="main-container">
      <div class="content-area">
        <div class="video-area" id="video-container">

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
        <el-scrollbar>
          <!-- =================================================== -->
          <!-- **改动点 1：新增独立的AGV移动控制卡片** -->
          <!-- =================================================== -->
          <div class="card">
            <div class="card-header">
              <span>AGV 移动控制</span>
              <div class="control-lock">
                <span class="lock-label">{{ isLocked ? '已锁定' : '已解锁' }}</span>
                <button class="lock-btn" @click="isLocked = !isLocked">
                  <svg v-if="isLocked" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#f56c6c" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 10 0v4"></path></svg>
                  <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#67c23a" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect><path d="M7 11V7a5 5 0 0 1 5-5 4.93 4.93 0 0 1 2.78.94"></path></svg>
                </button>
              </div>
            </div>
            <div class="card-body agv-console">
              <div class="control-grid" :class="{ locked: isLocked }">
                <button class="control-btn" @click="handleMove('backward')" :disabled="isLocked">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>
                  <span>后退</span>
                </button>
                <button class="control-btn stop" @click="handleMove('stop')" :disabled="isLocked">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><rect x="9" y="9" width="6" height="6"></rect></svg>
                  <span>停止</span>
                </button>
                <button class="control-btn" @click="handleMove('forward')" :disabled="isLocked">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line><polyline points="12 5 19 12 12 19"></polyline></svg>
                  <span>前进</span>
                </button>
              </div>
            </div>
          </div>

          <!-- **原有的"控制台"卡片，现在只负责视频和任务操作** -->
          <div class="card">
            <div class="card-header">
              <span>视频与任务</span>
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
            <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
              <span>车辆状态</span>
              <span :class="heartbeatStatusClass" style="font-size: 13px;">{{ heartbeatStatusText }}</span>
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
                <div class="info-value"><span class="count-animation">{{ typeof distance === 'number' ? distance.toFixed(2) : '0.00' }}</span> 米</div>
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
              <div class="info-item" style="align-items: center;">
                <div class="info-label">🚦 行驶状态</div>
                <div class="info-value" style="display: flex; align-items: center; gap: 8px;">
                  <span :class="['breath-light', agvIsRunning ? 'running' : 'stopped']"></span>
                  <span>{{ agvIsRunning ? '行驶中' : '已停止' }}</span>
                </div>
              </div>
            </div>
          </div>

          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>⚠️ 故障历史</span>
              </div>
            </template>
            <el-table :data="flaws" style="width: 100%" @row-click="viewFlawDetail" :row-class-name="tableRowClassName">
              <el-table-column prop="flawName" label="故障名称" />
              <el-table-column prop="flawType" label="类型" width="80"/>
              <el-table-column prop="flawDistance" label="位置(m)" width="80"/>
              <el-table-column label="图片" width="100">
                <template #default="scope">
                  <el-image v-if="scope.row.flawImageUrl" :src="scope.row.flawImageUrl" fit="cover" style="width:60px;height:40px;" :preview-src-list="[scope.row.flawImageUrl]" preview-teleported/>
                  <span v-else style="color:#bbb;">无</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="70" align="center">
                <template #default="scope">
                  <el-button link type="primary" @click.stop="viewFlawDetail(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-scrollbar>
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

    <el-dialog v-model="completeDialogVisible" title="完成确认" width="400px" :close-on-click-modal="false">
      <div style="font-size:16px; text-align:center; margin: 20px 0;">
        <el-icon style="font-size:32px; color:#67c23a; margin-bottom:10px;"><CircleCheckFilled /></el-icon>
        <div>请确认是否要<strong>完成当前巡检任务</strong>？<br>此操作不可撤销。</div>
      </div>
      <template #footer>
        <el-button size="large" @click="completeDialogVisible = false">取消</el-button>
        <el-button size="large" type="success" @click="onConfirmComplete" :disabled="!canComplete">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="terminateDialogVisible" title="终止确认" width="400px" :close-on-click-modal="false">
      <div style="font-size:16px; text-align:center; margin: 20px 0;">
        <el-icon style="font-size:32px; color:#f56c6c; margin-bottom:10px;"><CircleCloseFilled /></el-icon>
        <div>您确定要<strong>终止当前巡检任务</strong>吗？<br><span style="color:#f56c6c;">此操作不可恢复！</span></div>
      </div>
      <template #footer>
        <el-button size="large" @click="terminateDialogVisible = false">取消</el-button>
        <el-button size="large" type="danger" @click="onConfirmTerminate" :disabled="!canTerminate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import {
  getDeviceList, getTaskDetails, getFlawList, getFlawDetails,
  updateFlaw_vehicle, agvForward, agvStop, agvBackward, getAgvHeartbeat,
  getVideoStreamUrl, endTask, addFlaw, updateFlaw,
  getLiveFlawInfo
} from '@/api/apiManager.js';
import { updateTask } from '@/api/apiManager.js';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const currentTaskId = ref(route.params.id); // 动态获取任务ID
const player = ref(null);
const totalDistance = ref(0);
const distance = ref(0);
const taskNumber = ref('加载中');
const systemTime = ref('加载中');
const taskStatus = ref('未知');
const flaws = ref([]);
const isModalVisible = ref(false);
const selectedFlaw = ref(null);
const selectedCameraId = ref(null); // 默认不选中任何摄像头
const cameras = ref([]); // 默认摄像头列表为空，将由API填充
const isLocked = ref(true); // 控制台默认锁定
const heartbeatStatus = ref('unknown'); // 'ok', 'error', 'unknown'
const completeDialogVisible = ref(false);
const terminateDialogVisible = ref(false);
const agvIsRunning = ref(false);

// 定时器变量
let taskInterval = null;
let agvInterval = null;
let flawInterval = null;
let heartbeatInterval = null;

const progressPercentage = computed(() => {
  const total = Number(totalDistance.value);
  const current = Number(distance.value);
  if (!total || isNaN(total) || total <= 0) return 0;
  if (!current || isNaN(current) || current < 0) return 0;
  const percent = (current / total) * 100;
  return Math.max(0, Math.min(percent, 100));
});
const formattedSystemTime = computed(() => new Date(systemTime.value).toLocaleString('zh-CN'));
const confirmedFlawCount = computed(() => flaws.value.filter(f => f.confirmed).length);
const unconfirmedFlawCount = computed(() => flaws.value.filter(f => !f.confirmed).length);
const currentCamera = computed(() => cameras.value.find(c => c.id === selectedCameraId.value) || {});

const heartbeatStatusText = computed(() => {
  if (heartbeatStatus.value === 'ok') return '连接正常';
  if (heartbeatStatus.value === 'error') return '连接异常';
  return '未知';
});

const heartbeatStatusClass = computed(() => {
  if (heartbeatStatus.value === 'ok') return 'status-ok';
  if (heartbeatStatus.value === 'error') return 'status-error';
  return 'status-unknown';
});

const canComplete = computed(() => taskStatus.value === '巡视中');
const canTerminate = computed(() => taskStatus.value === '巡视中');

const pollHeartbeat = async () => {
  try {
    await getAgvHeartbeat();
    heartbeatStatus.value = 'ok';
  } catch (error) {
    heartbeatStatus.value = 'error';
  }
};

const initPlayer = (container, deviceId) => {
  if (!container || !deviceId) return;
  const streamUrl = getVideoStreamUrl(deviceId);
  console.log('视频流URL:', streamUrl);
  console.log("container",container);
  console.log("deviceId",deviceId);

  if (player.value) {
    // player.value.destroy();
    // player.value = null;
    console.log(`切换视频流到: ${streamUrl}`);
    player.value.play(streamUrl).catch(error => {
      console.error('视频切换失败:', error);
    });
    return; // 完成切换，退出函数
  }

  const easyplayer = new EasyPlayerPro(container, {
    libPath: "/js/",
    isLive: true,
    bufferTime: 0.2,
    stretch: false,
    MSE: false,
    WCS: false,
    hasAudio: true,
    watermark: {text: {content: 'easyplayer-pro'}, right: 10, top: 10},
  });

  player.value = easyplayer;

  setTimeout(() => {
    if (streamUrl && player.value) {
      player.value.play(streamUrl).then(() => {
        console.log('视频播放成功');
      }).catch((error) => {
        console.error('视频播放失败:', error);
        ElMessage.error('视频播放失败！');
      });
    }
  }, 100);
};

const refreshMonitor = () => {
    console.log("手动刷新监控...");
    if (player.value) {
        player.value.destroy();
        player.value = null;
    }
    const cam = currentCamera.value;
    console.log("当前摄像头信息:", cam);
    console.log("摄像头ID:", cam.id);
    console.log("摄像头URL:", cam.url);
    if (cam && cam.url) { initPlayer(document.getElementById('video-container'),cam.id); }
    else { console.error("当前摄像头没有有效的URL，无法刷新。"); }
};

const handleMove = async (direction) => {
  if (isLocked.value) return;
  try {
    if (direction === 'forward') await agvForward();
    else if (direction === 'stop') await agvStop();
    else if (direction === 'backward') await agvBackward();
    console.log(`发送指令: ${direction}`);
  } catch (error) {
    console.error(`发送指令 ${direction} 失败:`, error);
  }
};

function handleVisibilityChange() {
  if (document.hidden) {
    clearInterval(taskInterval);
    taskInterval = null;
    clearInterval(agvInterval);
    agvInterval = null;
    clearInterval(flawInterval);
    flawInterval = null;
    clearInterval(heartbeatInterval);
    heartbeatInterval = null;
  } else {
    if (!taskInterval) taskInterval = setInterval(pollTaskDetails, 3000);
    if (!agvInterval) agvInterval = setInterval(pollAgvStatus, 2000);
    if (!flawInterval) flawInterval = setInterval(pollFlawList, 10000);
    if (!heartbeatInterval) heartbeatInterval = setInterval(pollHeartbeat, 5000);
  }
}

// 恢复 pollTaskDetails
const pollTaskDetails = async () => {
  if (!currentTaskId.value) return;
  try {
    const taskData = await getTaskDetails(currentTaskId.value);
    console.log('获取到的任务详情:', taskData);
    if (taskData) {
      // 处理不同数据源的字段差异
      taskNumber.value = taskData.taskCode || taskData.taskNumber || '未知';
      totalDistance.value = Number(taskData.taskTrip) || Number(taskData.totalDistance) || 0;
      taskStatus.value = taskData.taskStatus || taskData.status || '未知';
    }
  } catch (error) {
    console.error("轮询任务详情失败:", error);
  }
};

// 恢复 pollAgvStatus
const pollAgvStatus = async () => {
  try {
    const status = await getAgvHeartbeat();
    if (status) {
      systemTime.value = status.sysTime || '加载中';
      agvIsRunning.value = status.isRunning || false;
      distance.value = typeof status.currentPosition === 'number' ? status.currentPosition : 0;
    }
  } catch (error) {
    console.error('获取AGV状态失败:', error);
  }
};

const pollFlawList = async () => {
  if (!currentTaskId.value) return;
  try {
    const liveFlaws = await getLiveFlawInfo(currentTaskId.value);
    console.log('获取到的故障信息:', liveFlaws);

    // 处理不同数据源的响应格式
    if (Array.isArray(liveFlaws)) {
      // 直接是数组格式
      flaws.value = liveFlaws;
    } else if (liveFlaws && Array.isArray(liveFlaws.rows)) {
      // TableDataInfo格式 { rows: [...], total: number }
      flaws.value = liveFlaws.rows;
    } else if (liveFlaws && Array.isArray(liveFlaws.data)) {
      // 本地API格式 { data: [...], total: number }
      flaws.value = liveFlaws.data;
    } else {
      flaws.value = [];
    }

    console.log('处理后的故障列表:', flaws.value);
  } catch (error) {
    console.error('实时获取故障信息失败:', error);
    flaws.value = [];
  }

  // 自动弹出第一个未shown的故障详情弹窗，并截图
  const unshownFlaw = flaws.value.find(f => !f.shown);
  if (unshownFlaw && !isModalVisible.value) {
    selectedFlaw.value = unshownFlaw;
    selectedFlaw.value.flawImageUrl = await captureScreenshot();
    isModalVisible.value = true;
  }
};

// 自动截图函数，适配EasyPlayerPro
const captureScreenshot = async () => {
  // player.value 是 EasyPlayerPro 实例
  if (player.value && typeof player.value.screenshot === 'function') {
    try {
      // 返回base64图片
      return player.value.screenshot('flaw', 'png', 0.8, 'base64');
    } catch (e) {
      console.warn('EasyPlayerPro 截图失败', e);
    }
  }
  return '';
};

const viewFlawDetail = async (flaw) => {
  try {
    const flawDetails = await getFlawDetails(flaw.id);
    console.log('获取到的故障详情:', flawDetails);
    if(flawDetails){
        selectedFlaw.value = flawDetails;
        isModalVisible.value = true;
    }
  } catch (error) {
    console.error("获取缺陷详情失败:", error);
    // 如果获取详情失败，直接使用当前故障信息
    selectedFlaw.value = flaw;
    isModalVisible.value = true;
  }
};

const markFlawAsShown = async () => {
    if (!selectedFlaw.value) return;
    if (selectedFlaw.value.shown === false) {
        try {
            selectedFlaw.value.shown = true;

            // 根据数据源选择不同的更新方法
            const { getCurrentDataSourceInfo } = await import('@/api/apiManager.js');
            const dataSourceInfo = getCurrentDataSourceInfo();

            if (dataSourceInfo.isLocal) {
              // 本地数据源使用updateFlaw
              await updateFlaw(selectedFlaw.value);
            } else {
              // 小车数据源使用updateFlaw_vehicle
              await updateFlaw_vehicle(selectedFlaw.value);
            }

            console.log(`缺陷 ${selectedFlaw.value.id} 已标记为"已提示"`);
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
    console.log('准备保存的故障信息:', selectedFlaw.value);

    // 根据数据源选择不同的更新方法
    const { getCurrentDataSourceInfo } = await import('@/api/apiManager.js');
    const dataSourceInfo = getCurrentDataSourceInfo();

    if (dataSourceInfo.isLocal) {
      // 本地数据源使用updateFlaw
      await updateFlaw(selectedFlaw.value);
    } else {
      // 小车数据源使用addFlaw
      await addFlaw(selectedFlaw.value);
    }

    selectedFlaw.value.shown = true;
    ElMessage.success('缺陷已保存并上传！');
    closeFlawModal();
  } catch (error) {
    ElMessage.error('上传失败，请重试');
    console.error('上传缺陷失败:', error);
  }
};

const handleCompleteTask = () => {
  completeDialogVisible.value = true;
};

const handleTerminateTask = () => {
  terminateDialogVisible.value = true;
};

const onConfirmComplete = async () => {
  completeDialogVisible.value = false;
  try {
    await endTask(currentTaskId.value, false);

    // 根据数据源选择不同的更新方式
    const { getCurrentDataSourceInfo } = await import('@/api/apiManager.js');
    const dataSourceInfo = getCurrentDataSourceInfo();

    if (dataSourceInfo.isLocal) {
      // 本地数据源：获取最新任务详情，更新状态为待上传
      const taskData = await getTaskDetails(currentTaskId.value);
      if (taskData) {
        const updated = { ...taskData, taskStatus: '待上传' };
        await updateTask(updated);
        taskStatus.value = '待上传';
      }
    } else {
      // 小车数据源：直接更新状态
      taskStatus.value = '待上传';
    }

    ElMessage.success('任务已完成!');
    setTimeout(() => {
      router.back();
    }, 800);
  } catch (error) {
    ElMessage.error('完成任务失败: ' + (error?.message || '未知错误'));
    console.error('完成任务失败:', error);
  }
};

const onConfirmTerminate = async () => {
  terminateDialogVisible.value = false;
  try {
    await endTask(currentTaskId.value, true);
    ElMessage.success('任务已终止!');
    setTimeout(() => {
      router.back();
    }, 800);
  } catch (error) {
    ElMessage.error('终止任务失败: ' + (error?.message || '未知错误'));
    console.error('终止任务失败:', error);
  }
};

const tableRowClassName = ({ row }) => {
  if (row.confirmed === true) return 'status-confirmed';
  if (row.confirmed === false) return 'status-ignored';
  return 'status-pending';
};

let lastDistance = null;
onMounted(() => {
  lastDistance = distance.value;
});
watch(distance, (newDistance, oldDistance) => {
  if (lastDistance === null) {
    lastDistance = newDistance;
    return;
  }
  if (!isModalVisible.value && newDistance > lastDistance) {
    const nextFlaw = flaws.value.find(f =>
      !f.shown &&
      f.flawDistance > lastDistance &&
      f.flawDistance <= newDistance
    );
    if (nextFlaw) {
      selectedFlaw.value = nextFlaw;
      const img = captureScreenshot();
      console.log('截图base64:', img);
      selectedFlaw.value.flawImageUrl = img;
      isModalVisible.value = true;
    }
  }
  lastDistance = newDistance;
});

onMounted(async () => {
  await Promise.allSettled([
    getDeviceList().then(deviceData => {
      let deviceList = [];
      if (deviceData && Array.isArray(deviceData.items)) {
        deviceList = deviceData.items;
      }
      if (deviceList.length > 0) {
        cameras.value = deviceList.map(device => ({
          id: device.id,
          name: device.name || `摄像头 ${device.id}`,
          url: getVideoStreamUrl(device.id)
        }));
        if (cameras.value.length > 0) {
          selectedCameraId.value = cameras.value[0].id;
        }
      }
    }).catch(error => {
      console.error("获取摄像头列表失败:", error);
    }),
    pollTaskDetails(),
    pollFlawList(),
    pollHeartbeat(),
    pollAgvStatus()
  ]);

  taskInterval = setInterval(pollTaskDetails, 3000);
  agvInterval = setInterval(pollAgvStatus, 2000);
  flawInterval = setInterval(pollFlawList, 10000);
  heartbeatInterval = setInterval(pollHeartbeat, 5000);
  document.addEventListener('visibilitychange', handleVisibilityChange);
});

onUnmounted(() => {
  if (player.value) { player.value.destroy(); }
  clearInterval(taskInterval);
  clearInterval(agvInterval);
  clearInterval(flawInterval);
  clearInterval(heartbeatInterval);
  document.removeEventListener('visibilitychange', handleVisibilityChange);
});

watch(selectedCameraId, (newId) => {
    const newCam = cameras.value.find(c => c.id === newId);
    console.log("selectedCameraId",selectedCameraId.value);
    if (newCam) { initPlayer(document.getElementById('video-container'), selectedCameraId.value); }
});
</script>

<style scoped>
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

.control-lock { display: flex; align-items: center; gap: 8px; }
.lock-label { font-size: 12px; color: #999; font-weight: normal; }
.lock-btn { background: none; border: none; padding: 0; cursor: pointer; display: flex; align-items: center; }

.agv-console .heartbeat-panel {
  display: block;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 20px;
}
.agv-console .status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.agv-console .status-item .label {
  font-size: 14px;
  color: #606266;
}
.agv-console .status-item .value {
  font-size: 14px;
  font-weight: bold;
}
.status-ok { color: #67c23a; }
.status-error { color: #f56c6c; }
.status-unknown { color: #909399; }

.agv-console .control-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  transition: opacity 0.3s ease;
}
.agv-console .control-grid.locked {
  opacity: 0.5;
  pointer-events: none;
}
.agv-console .control-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 15px 0;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}
.agv-console .control-btn:hover:not(:disabled) {
  background-color: #ecf5ff;
  color: #409eff;
  border-color: #c6e2ff;
}
.agv-console .control-btn.stop:hover:not(:disabled) {
  background-color: #fef0f0;
  color: #f56c6c;
  border-color: #fde2e2;
}
.agv-console .control-btn span {
  font-size: 14px;
}
.agv-console .control-btn svg {
  width: 24px;
  height: 24px;
}

.breath-light {
  display: inline-block;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #bbb;
  margin-right: 4px;
  vertical-align: middle;
  box-shadow: 0 0 8px #bbb;
  animation: breath-stopped 1.5s infinite;
}
.breath-light.running {
  background: #67c23a;
  box-shadow: 0 0 16px #67c23a;
  animation: breath-running 1.5s infinite;
}
.breath-light.stopped {
  background: #bbb;
  box-shadow: 0 0 8px #bbb;
  animation: breath-stopped 1.5s infinite;
}
@keyframes breath-running {
  0% { box-shadow: 0 0 8px #67c23a; }
  50% { box-shadow: 0 0 24px #67c23a; }
  100% { box-shadow: 0 0 8px #67c23a; }
}
@keyframes breath-stopped {
  0% { box-shadow: 0 0 8px #bbb; }
  50% { box-shadow: 0 0 16px #bbb; }
  100% { box-shadow: 0 0 8px #bbb; }
}

.status-confirmed { color: #67c23a; font-weight: bold; }
.status-ignored { color: #909399; font-weight: bold; }
.status-pending { color: #e6a23c; font-weight: bold; }
.el-table .status-confirmed { --el-table-tr-bg-color: var(--el-color-success-light-9); }
.el-table .status-ignored { --el-table-tr-bg-color: var(--el-color-info-light-9); }
.el-table .status-pending { --el-table-tr-bg-color: var(--el-color-warning-light-9); }

</style>
