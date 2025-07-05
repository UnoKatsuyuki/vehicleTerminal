<template>
  <div class="task-upload-view">
    <el-page-header @back="goBack" :icon="ArrowLeft">
      <template #content>
        <span class="text-large font-600 mr-3"> 数据上传 </span>
      </template>
    </el-page-header>

    <div class="upload-container">
      <el-card class="upload-card">
        <template #header>
          <div class="card-header">
            <span>任务上传: {{ taskInfo.taskName || '...' }}</span>
          </div>
        </template>

        <!-- 步骤条 -->
        <el-steps :active="activeStep" finish-status="success" align-center>
          <el-step title="准备上传"></el-step>
          <el-step title="正在上传"></el-step>
          <el-step title="上传完成"></el-step>
        </el-steps>

        <!-- 上传中 视图 -->
        <div v-if="activeStep === 1" class="upload-progress-area">
          <h3>{{ uploadStatusText }}</h3>
          <p class="current-file-info">
            {{ currentFile?.type }}: {{ currentFile?.info }}
          </p>
          <el-progress :percentage="overallProgress" :stroke-width="15" striped striped-flow />

          <div class="file-list">
            <el-scrollbar>
              <ul>
                <li v-for="file in uploadList" :key="file.info">
                  <el-icon><component :is="getIcon(file.status)" :class="file.status" /></el-icon>
                  <span>{{ file.type }}: {{ file.info }}</span>
                </li>
              </ul>
            </el-scrollbar>
          </div>
        </div>

        <!-- 上传完成 视图 -->
        <div v-if="activeStep === 2" class="upload-complete-area">
          <el-result
            icon="success"
            title="数据上传成功"
            :sub-title="`任务 [${taskInfo.taskCode}] 的所有数据已成功上传至云端服务器。`"
          >
            <template #extra>
              <el-button type="primary" @click="goBackToList">返回任务列表</el-button>
            </template>
          </el-result>
        </div>

        <!-- 准备上传 视图 -->
        <div v-if="activeStep === 0" class="prepare-upload-area">
          <el-alert
            title="准备开始上传"
            type="info"
            description="即将开始上传任务数据，请保持网络连接稳定，期间请勿关闭页面。"
            show-icon
            :closable="false"
            center
          />
          <el-button type="primary" size="large" @click="startUploadProcess" :loading="isPreparing">开始上传</el-button>
        </div>

      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getTask, preUploadTask, uploadTask, updateFlaw } from '@/api/apiManager.js';
import { ElMessage } from 'element-plus';
import { ArrowLeft, CircleCheck, Loading, CircleClose } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const taskId = ref(null);
const taskInfo = ref({});
const uploadList = ref([]);
const isPreparing = ref(false);

const activeStep = ref(0);
const overallProgress = ref(0);
const currentFileIndex = ref(-1);
const uploadStatusText = ref('');

const currentFile = computed(() => {
  return currentFileIndex.value >= 0 ? uploadList.value[currentFileIndex.value] : null;
});

onMounted(() => {
  taskId.value = route.params.id;
  if (taskId.value) {
    fetchTaskInfo();
  }
});

async function fetchTaskInfo() {
  try {
    taskInfo.value = await getTask(taskId.value);
  } catch (error) {
    console.error("获取任务信息失败:", error);
    ElMessage.error("获取任务信息失败，请重试。");
  }
}

async function startUploadProcess() {
  isPreparing.value = true;
  try {
    const updatesJson = sessionStorage.getItem('flawUpdates');
    if (updatesJson) {
      const updates = JSON.parse(updatesJson);
      if (updates.length > 0) {
        uploadStatusText.value = '正在保存复盘结果...';
        try {
          const updatePromises = updates.map(flaw => updateFlaw(flaw));
          await Promise.all(updatePromises);

          ElMessage.success('复盘结果保存成功');
        } catch (error) {
          console.error('保存复盘结果失败:', error);
          ElMessage.error("保存复盘结果失败，请重试。");
        }
      }
      sessionStorage.removeItem('flawUpdates');
    }

    uploadStatusText.value = '正在获取文件列表...';
    const filesToUpload = await preUploadTask(taskId.value);
    uploadList.value = filesToUpload.map(file => ({...file, status: 'pending'}));

    await uploadTask(taskId.value);
    activeStep.value = 1;
    uploadStatusText.value = '正在上传文件...';
    simulateFileUpload();

  } catch (error) {
    console.error("上传流程失败:", error);
    ElMessage.error("上传流程失败，请重试。");
  } finally {
    isPreparing.value = false;
  }
}

function simulateFileUpload() {
  if (currentFileIndex.value >= uploadList.value.length - 1) {
    overallProgress.value = 100;
    setTimeout(() => {
      activeStep.value = 2;
    }, 500);
    return;
  }

  currentFileIndex.value++;
  const file = uploadList.value[currentFileIndex.value];
  file.status = 'uploading';

  const uploadTime = Math.random() * 800 + 200;
  setTimeout(() => {
    file.status = 'success';
    overallProgress.value = Math.round(((currentFileIndex.value + 1) / uploadList.value.length) * 100);
    simulateFileUpload();
  }, uploadTime);
}

const getIcon = (status) => {
  switch (status) {
    case 'success':
      return CircleCheck;
    case 'uploading':
      return Loading;
    default:
      return CircleClose;
  }
};

function goBack() {
  router.back();
}
function goBackToList() {
  router.back();
}
</script>

<style scoped>
.task-upload-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.el-page-header {
  background-color: #fff;
  padding: 16px 24px;
  border-bottom: 1px solid #dcdfe6;
  flex-shrink: 0;
}
.upload-container {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  padding: 24px;
}
.upload-card {
  width: 800px;
  min-height: 500px;
}
.card-header {
  font-weight: bold;
}
.el-steps {
  margin: 40px 0;
}
.prepare-upload-area, .upload-complete-area {
  text-align: center;
  padding: 40px;
}
.prepare-upload-area .el-button {
  margin-top: 24px;
}
.upload-progress-area {
  padding: 20px;
  text-align: center;
}
.upload-progress-area h3 {
  margin-bottom: 8px;
}
.current-file-info {
  color: #606266;
  margin-bottom: 20px;
  height: 20px;
}
.file-list {
  margin-top: 30px;
  height: 200px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
}
.file-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
.file-list li {
  display: flex;
  align-items: center;
  padding: 8px;
  font-size: 14px;
}
.file-list .el-icon {
  margin-right: 8px;
  font-size: 16px;
}
.el-icon.success { color: #67c23a; }
.el-icon.uploading { color: #409eff; }
.el-icon.pending { color: #909399; }
</style>
