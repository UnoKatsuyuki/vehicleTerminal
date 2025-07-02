<template>
  <div class="task-detail-view" v-loading="loading">
    <!-- 1. 顶部面包屑和返回按钮 -->
    <el-page-header @back="goBack">
      <template #content>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item>地铁隧道巡线车智能巡检系统</el-breadcrumb-item>
          <el-breadcrumb-item>任务列表</el-breadcrumb-item>
          <el-breadcrumb-item>任务详情与复盘</el-breadcrumb-item>
        </el-breadcrumb>
      </template>
      <template #extra>
        <div class="header-actions">
          <el-button type="primary" @click="handleCompleteReview" :disabled="!isAllFlawsHandled">
            完成复盘并上传
          </el-button>
        </div>
      </template>
    </el-page-header>

    <div class="main-container">
      <!-- 2. 左侧主内容区 -->
      <div class="content-area">
        <!-- 2.1 缺陷图片展示区 -->
        <div class="image-area">
          <el-image :src="currentFlawImage" fit="contain">
            <template #error>
              <div class="image-slot">
                <el-icon><Picture /></el-icon>
                <span>请从右侧或下方选择一个缺陷以预览图片</span>
              </div>
            </template>
          </el-image>
        </div>
        <!-- 2.2 缺陷分布标尺 -->
        <div class="scale-bar-area">
          <el-slider
            v-model="sliderValue"
            :max="parseFloat(taskInfo.taskTrip) || 100"
            :show-tooltip="false"
            disabled
          />
          <div
            v-for="flaw in flawList"
            :key="`marker-${flaw.id}`"
            class="flaw-marker"
            :style="{ left: flawPosition(flaw.flawDistance) }"
            :class="getFlawStatus(flaw).className"
            @click="handleSelectFlaw(flaw)"
          >
            <el-tooltip :content="flaw.flawName" placement="top">
              <span>📍</span>
            </el-tooltip>
          </div>
        </div>
      </div>

      <!-- 3. 右侧信息边栏 -->
      <div class="sidebar">
        <el-scrollbar>
          <!-- 3.1 任务信息卡片 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>📄 任务信息</span>
              </div>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="任务编号">{{ taskInfo.taskCode }}</el-descriptions-item>
              <el-descriptions-item label="开始时间">{{ taskInfo.execTime }}</el-descriptions-item>
              <el-descriptions-item label="结束时间">{{ taskInfo.endTime }}</el-descriptions-item>
              <el-descriptions-item label="行驶距离">{{ taskInfo.taskTrip }} 米</el-descriptions-item>
              <el-descriptions-item label="故障总计">{{ flawStats.total }}</el-descriptions-item>
              <el-descriptions-item label="已确定故障">
                <span class="status-confirmed">{{ flawStats.confirmed }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="疑似故障">
                <span class="status-pending">{{ flawStats.pending }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
          <!-- 3.2 故障历史卡片 -->
          <el-card shadow="never" class="info-card">
            <template #header>
              <div class="card-header">
                <span>⚠️ 故障历史</span>
              </div>
            </template>
            <el-table :data="flawList" style="width: 100%" @row-click="handleSelectFlaw" :row-class-name="tableRowClassName">
              <el-table-column prop="flawName" label="故障名称" />
              <el-table-column prop="flawType" label="类型" width="80"/>
              <el-table-column prop="flawDistance" label="位置(m)" width="80"/>
              <el-table-column label="操作" width="70" align="center">
                <template #default="scope">
                  <el-button link type="primary" @click.stop="handleSelectFlaw(scope.row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-scrollbar>
      </div>
    </div>

    <!-- 4. 故障详情与确认弹窗 -->
    <el-dialog v-model="dialogVisible" title="故障详情与确认" width="70%" top="5vh">
      <div class="modal-body" v-if="selectedFlaw">
        <div class="modal-image-container">
          <el-image :src="selectedFlaw.flawImageUrl" fit="contain" :preview-src-list="[selectedFlaw.flawImageUrl]" preview-teleported/>
        </div>
        <div class="modal-info-container">
          <el-descriptions :column="1" border title="故障信息">
            <el-descriptions-item label="故障名称">{{ selectedFlaw.flawName }}</el-descriptions-item>
            <el-descriptions-item label="故障类型">{{ selectedFlaw.flawType }}</el-descriptions-item>
            <el-descriptions-item label="故障描述">{{ selectedFlaw.flawDesc || '无' }}</el-descriptions-item>
            <el-descriptions-item label="故障位置">{{ selectedFlaw.flawDistance }} 米</el-descriptions-item>
          </el-descriptions>
          <el-form class="modal-form" label-position="top" :model="selectedFlaw">
            <el-form-item label="是否属实">
              <el-radio-group v-model="selectedFlaw.confirmed" :disabled="formDisabled">
                <el-radio :value="true">是</el-radio>
                <el-radio :value="false">否 (误报)</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="补充说明">
              <el-input v-model="selectedFlaw.remark" type="textarea" :rows="5" placeholder="请输入补充说明" :disabled="formDisabled"/>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleConfirm" :disabled="formDisabled">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getTask, listFlaw } from '@/api/taskApi.js';
import { ElMessage } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';

// 【核心修复】移除所有写死的IP地址
// const API_BASE_URL = 'http://192.168.2.57';

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const taskId = ref(null);
const taskInfo = ref({});
const flawList = ref([]);
const sliderValue = ref(0);

const changedFlaws = ref(new Map());
const selectedFlaw = ref(null);
const dialogVisible = ref(false);

const currentFlawImage = computed(() => selectedFlaw.value ? selectedFlaw.value.flawImageUrl : '');
const formDisabled = computed(() => {
  if (!selectedFlaw.value) return true;
  return selectedFlaw.value.initialConfirmed !== null;
});

const isAllFlawsHandled = computed(() => {
  if (flawList.value.length === 0) return true;
  return flawList.value.every(f => f.confirmed !== null);
});

onMounted(() => {
  taskId.value = route.params.id;
  if (taskId.value) {
    fetchData();
  }
});

async function fetchData() {
  loading.value = true;
  try {
    const [taskData, flawData] = await Promise.all([
      getTask(taskId.value),
      listFlaw({ taskId: taskId.value, pageSize: 999 })
    ]);

    taskInfo.value = taskData;
    flawList.value = flawData.rows
      .filter(f => f.taskId == taskId.value)
      .map(f => {
        // 【恢复】使用代理路径访问图片
        const imageUrl = f.flawImageUrl ? `/prod-api/file${f.flawImageUrl}` : '';
        return {
          ...f,
          flawImageUrl: imageUrl,
          initialConfirmed: f.confirmed
        };
      });

  } catch (error) {
    console.error('获取复盘数据失败:', error);
    ElMessage.error('获取复盘数据失败，请检查网络或联系管理员');
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.back();
}

const flawPosition = (distance) => {
  const totalTrip = parseFloat(taskInfo.value.taskTrip);
  if (!totalTrip) return '0%';
  const percentage = (distance / totalTrip) * 100;
  return `${percentage}%`;
}

function getFlawStatus(flaw) {
  if (flaw.confirmed === true) return { text: '已确认', className: 'status-confirmed' };
  if (flaw.confirmed === false) return { text: '已忽略', className: 'status-ignored' };
  return { text: '待处理', className: 'status-pending' };
}

const flawStats = computed(() => {
  const stats = { total: flawList.value.length, confirmed: 0, pending: 0, ignored: 0 };
  flawList.value.forEach(f => {
    if (f.confirmed === true) stats.confirmed++;
    else if (f.confirmed === false) stats.ignored++;
    else stats.pending++;
  });
  return stats;
});

const tableRowClassName = ({ row }) => {
  return getFlawStatus(row).className;
};

function handleSelectFlaw(flaw) {
  // 恢复干净的逻辑：当从列表中选择一个缺陷时，确保传递给弹窗的是一个深拷贝对象
  selectedFlaw.value = JSON.parse(JSON.stringify(flaw));
  dialogVisible.value = true;
}

function handleConfirm() {
  if (!selectedFlaw.value) return;

  const index = flawList.value.findIndex(f => f.id === selectedFlaw.value.id);
  if (index !== -1) {
    const updatedFlaw = {
      ...flawList.value[index],
      confirmed: selectedFlaw.value.confirmed,
      remark: selectedFlaw.value.remark,
    };
    flawList.value.splice(index, 1, updatedFlaw);
    changedFlaws.value.set(selectedFlaw.value.id, {
      id: selectedFlaw.value.id,
      confirmed: selectedFlaw.value.confirmed,
      remark: selectedFlaw.value.remark,
    });
    ElMessage.success('修改已暂存');
  }
  dialogVisible.value = false;
}

function handleCompleteReview() {
  const updates = Array.from(changedFlaws.value.values());
  sessionStorage.setItem('flawUpdates', JSON.stringify(updates));
  router.push({ name: 'task-upload', params: { id: taskId.value } });
}
</script>

<style scoped>
.task-detail-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f5f7fa;
}
.el-page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #dcdfe6;
}
.header-actions {
  margin-right: 24px;
}
.main-container {
  flex-grow: 1;
  display: flex;
  overflow: hidden;
  padding: 16px;
  gap: 16px;
}
.content-area {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  border-radius: 4px;
  overflow: hidden;
}
.image-area {
  flex-grow: 1;
  background-color: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-area .el-image {
  max-width: 100%;
  max-height: 100%;
}
.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}
.image-slot .el-icon {
  font-size: 30px;
  margin-bottom: 8px;
}
.scale-bar-area {
  height: 80px;
  padding: 0 40px;
  display: flex;
  align-items: center;
  position: relative;
  border-top: 1px solid #e4e7ed;
}
.flaw-marker {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
}
.sidebar {
  width: 400px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}
.info-card {
  border: none;
}
.info-card .card-header {
  font-weight: bold;
}
.status-confirmed { color: #67c23a; font-weight: bold; }
.status-ignored { color: #909399; font-weight: bold; }
.status-pending { color: #e6a23c; font-weight: bold; }

.el-table .status-confirmed { --el-table-tr-bg-color: var(--el-color-success-light-9); }
.el-table .status-ignored { --el-table-tr-bg-color: var(--el-color-info-light-9); }
.el-table .status-pending { --el-table-tr-bg-color: var(--el-color-warning-light-9); }

.modal-body {
  display: flex;
  gap: 24px;
  height: 65vh;
}
.modal-image-container {
  flex-grow: 1;
  background-color: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal-image-container .el-image {
  max-width: 100%;
  max-height: 100%;
}
.modal-info-container {
  width: 350px;
  flex-shrink: 0;
}
.modal-form {
  margin-top: 24px;
}
</style>
