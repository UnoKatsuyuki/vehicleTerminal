<template>
  <div class="init-view-wrapper">
    <div class="init-container">
      <h1>AGV智能巡检系统</h1>

      <div class="check-items-container">
        <div v-for="(item, index) in checkItems" :key="index" class="check-item">
          <div class="check-header">
            <div class="check-icon">
              <el-icon class="is-loading" v-if="item.status === 'loading'"><Loading /></el-icon>
              <el-icon class="is-success" v-if="item.status === 'success'"><CircleCheckFilled /></el-icon>
              <el-icon class="is-error" v-if="item.status === 'error'"><CircleCloseFilled /></el-icon>
            </div>
            <span class="check-message">{{ item.message }}</span>
          </div>
          <el-collapse-transition>
            <div v-show="item.isExpanded" class="check-content">
              <p v-if="item.errorDetails">错误详情: {{ item.errorDetails }}</p>
              <p>解决方案: {{ item.solution }}</p>
            </div>
          </el-collapse-transition>
        </div>
      </div>

      <div class="operate-btn-group">
        <!-- 【核心修复】将 icon="Setting" 修改为 :icon="Setting" -->
        <el-button circle :icon="Setting" @click="openSettings" title="设置" />
        <el-button
          type="success"
          size="large"
          :disabled="!allChecksPassed"
          @click="enterSystem"
        >
          进入系统
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="runAllChecks"
          :loading="isChecking"
        >
          {{ isChecking ? '正在检测...' : '重新检测' }}
        </el-button>
      </div>
    </div>

    <el-dialog v-model="showSettingsModal" title="系统设置" width="600px">
      <div style="color: #666; text-align: center; padding: 40px;">
        <p>[此处为系统设置(SettingsView)组件内容]</p>
        <p style="font-size: 14px; margin-top: 10px;">
          可在此配置车辆IP、端口、摄像头地址及账号密码等。
        </p>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="closeSettings">取消</el-button>
          <el-button type="primary" @click="saveSettings">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { CircleCheckFilled, CircleCloseFilled, Loading, Setting } from '@element-plus/icons-vue';
// 导入真实的API函数
import { checkFs, checkDb, checkAgv, checkCam } from '@/api/index.js';

const router = useRouter();
const showSettingsModal = ref(false);

const checkItems = reactive([
  // 将 apiCall 指向真实的API函数
  { name: '文件系统', apiCall: checkFs, message: '正在检查系统文件完整性', solution: '请重新安装本系统。', status: 'loading', errorDetails: '', isExpanded: false },
  { name: '数据库', apiCall: checkDb, message: '正在检测数据库系统连接', solution: '请检查数据库连接设置是否正确。', status: 'loading', errorDetails: '', isExpanded: false },
  { name: '车辆控制系统', apiCall: checkAgv, message: '正在与车辆控制系统通信', solution: '请检查巡检车IP与端口配置是否正确。', status: 'loading', errorDetails: '', isExpanded: false },
  { name: '摄像头', apiCall: checkCam, message: '正在检测摄像头通道状态', solution: '请检查摄像头IP及账号密码是否正确。', status: 'loading', errorDetails: '', isExpanded: false }
]);

const isChecking = computed(() => checkItems.some(item => item.status === 'loading'));
const allChecksPassed = computed(() => checkItems.every(item => item.status === 'success') && !isChecking.value);

const runCheck = async (item) => {
  item.status = 'loading';
  item.isExpanded = false;
  try {
    // 调用真实的API，并直接使用返回的msg
    const response = await item.apiCall();
    item.status = 'success';
    item.message = response.msg;
  } catch (error) {
    item.status = 'error';
    item.message = `${item.name}检测失败`;
    // 从error对象中获取更详细的错误信息
    item.errorDetails = error.msg || error.message || '未知网络错误';
    item.isExpanded = true;
  }
};

const runAllChecks = () => {
  checkItems.forEach(item => {
    // 重置状态
    item.isExpanded = false;
    if (item.name === '文件系统') item.message = "正在检查系统文件完整性";
    if (item.name === '数据库') item.message = "正在检测数据库系统连接";
    if (item.name === '车辆控制系统') item.message = "正在与车辆控制系统通信";
    if (item.name === '摄像头') item.message = "正在检测摄像头通道状态";
    runCheck(item);
  });
};

const openSettings = () => showSettingsModal.value = true;
const closeSettings = () => showSettingsModal.value = false;

const saveSettings = () => {
  ElMessage.success("设置已保存，将重新执行自检。");
  closeSettings();
  setTimeout(runAllChecks, 300);
};

const enterSystem = () => {
  if (allChecksPassed.value) {
    ElMessage.success("所有检查通过，进入系统主页...");
    router.push({ name: 'task-list' });
  }
};

onMounted(() => {
  runAllChecks();
});
</script>

<style scoped>
.init-view-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.init-container {
  width: 450px;
  background: white;
  padding: 30px 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

h1 {
  text-align: center;
  margin-bottom: 40px;
  color: #333;
  font-size: 24px;
}

.check-items-container {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.check-item {
  border-bottom: 1px solid #e4e7ed;
}
.check-item:last-child {
  border-bottom: none;
}

.check-header {
  padding: 12px 16px;
  display: flex;
  align-items: center;
}

.check-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  font-size: 20px;
}
.check-icon .el-icon {
  font-size: 20px;
}
.is-success { color: #67c23a; }
.is-error { color: #f56c6c; }

.check-message {
  color: #606266;
}

.check-content {
  padding: 0 16px 12px 48px;
  background: #fafafa;
  color: #909399;
  font-size: 13px;
  line-height: 1.5;
}

.operate-btn-group {
  margin-top: 40px;
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>
