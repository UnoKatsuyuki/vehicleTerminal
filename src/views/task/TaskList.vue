<template>
  <div class="task-list-page">
    <div class="app-container">
      <!-- 1. 顶部面包屑 -->
      <el-page-header @back="goBack" class="page-header">
        <template #content>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>地铁隧道巡线车智能巡检系统</el-breadcrumb-item>
            <el-breadcrumb-item>任务列表</el-breadcrumb-item>
          </el-breadcrumb>
        </template>
        <template #extra>
          <el-button circle icon="Setting" @click="handleSettings" />
        </template>
      </el-page-header>

      <!-- 2. 数据源切换开关 -->
      <div class="data-source-switch">
        <el-card shadow="never" class="switch-card">
          <div class="switch-content">
            <div class="switch-info">
              <el-icon class="switch-icon" :class="{ 'local': isLocalDataSource, 'vehicle': !isLocalDataSource }">
                <component :is="isLocalDataSource ? 'Monitor' : 'Van'" />
              </el-icon>
              <div class="switch-text">
                <div class="switch-title">{{ currentDataSourceInfo.name }}</div>
                <div class="switch-desc">{{ currentDataSourceInfo.description }}</div>
              </div>
            </div>
            <el-switch
              v-model="isLocalDataSource"
              active-text="本地数据源"
              inactive-text="小车数据源"
              :loading="isDataSourceChanging"
              @change="handleDataSourceChange"
              class="data-source-switch"
            />
          </div>
        </el-card>
      </div>

      <!-- 3. 搜索表单 -->
      <div class="search-form-container">
        <el-form :model="queryParams" ref="queryFormRef" :inline="true" label-position="top">
          <el-form-item label="任务编号" prop="taskCode">
            <el-input v-model="queryParams.taskCode" placeholder="请输入任务编号" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="创建人" prop="creator">
            <el-input v-model="queryParams.creator" placeholder="请输入创建人" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="执行人" prop="executor">
            <el-input v-model="queryParams.executor" placeholder="请输入执行人" clearable @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="taskStatus">
            <el-select v-model="queryParams.taskStatus" placeholder="请选择状态" clearable style="width: 180px;">
              <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value" />
            </el-select>
          </el-form-item>
          <el-form-item class="action-buttons">
            <el-button type="primary" @click="handleQuery">搜索</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 3. 操作栏 -->
      <div class="toolbar">
        <el-button type="primary" @click="handleCreate">新增任务</el-button>
        <el-button type="info" @click="testApiConnection" :loading="testingConnection">测试API连接</el-button>
      </div>

      <!-- 4. 数据表格 -->
      <div class="table-wrapper">
        <el-table v-loading="loading" :data="taskList" stripe @row-click="handleTaskClick" row-key="id">
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column label="任务编号" prop="taskCode" width="200">
            <template #default="scope">
              <el-button link type="primary">{{ scope.row.taskCode }}</el-button>
            </template>
          </el-table-column>
          <el-table-column label="任务名称" prop="taskName" :show-overflow-tooltip="true" />
          <el-table-column label="起始地点" prop="startPos" align="center" width="120" />
          <el-table-column label="任务距离(m)" prop="taskTrip" align="center" width="120" />
          <el-table-column label="创建人" prop="creator" align="center" width="120" />
          <el-table-column label="执行人" prop="executor" align="center" width="120" />
          <el-table-column label="完成时间" prop="endTime" align="center" width="180" />
          <el-table-column label="状态" align="center" width="120">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.taskStatus)" disable-transitions>{{ scope.row.taskStatus }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="220">
            <template #default="scope">
              <el-button link type="primary" icon="VideoPlay" @click.stop="handleStartPatrol(scope.row)" v-if="scope.row.taskStatus === '待巡视'">开始巡视</el-button>
              <el-button link type="primary" icon="VideoPlay" @click.stop="handleStartPatrol(scope.row)" v-if="scope.row.taskStatus === '巡视中'">继续巡视</el-button>
              <el-button link type="primary" icon="DocumentChecked" @click.stop="handleReview(scope.row)" v-if="scope.row.taskStatus === '待上传'">复盘</el-button>
              <el-button link type="primary" icon="View" @click.stop="handleReview(scope.row)" v-if="scope.row.taskStatus === '已完成'">查看报告</el-button>
              <el-button link type="primary" icon="Edit" @click.stop="handleEdit(scope.row)" v-if="scope.row.taskStatus === '待巡视'">修改</el-button>
              <el-button link type="danger" icon="Delete" @click.stop="handleDelete(scope.row.id, scope.row.taskCode)" v-if="scope.row.taskStatus === '待巡视'">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 5. 分页 -->
      <el-pagination
        class="pagination"
        v-show="total > 0"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="getList"
        @current-change="getList"
      />

      <!-- 6. 新增/编辑任务对话框 -->
      <el-dialog :title="dialog.title" v-model="dialog.open" width="800px" append-to-body>
        <el-form ref="taskFormRef" :model="form" :rules="rules" label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="任务名称" prop="taskName">
                <el-input v-model="form.taskName" placeholder="请输入任务名称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务编号" prop="taskCode">
                <el-input
                  v-model="form.taskCode"
                  :placeholder="taskCodeSelection === 'auto' ? '' : '请输入自定义编号'"
                  :disabled="!!form.id"
                  class="input-with-select"
                >
                  <template #prepend>
                    <el-select
                      v-model="taskCodeSelection"
                      :disabled="!!form.id"
                      style="width: 110px"
                    >
                      <el-option label="自动生成" value="auto" />
                      <el-option label="自定义" value="custom" />
                    </el-select>
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="起始地点" prop="startPos">
                <el-input v-model="form.startPos" placeholder="请输入起始地点" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="任务距离(m)" prop="taskTrip">
                <el-input-number v-model="form.taskTrip" :min="0" controls-position="right" style="width: 100%;" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="创建人" prop="creator">
                <el-input v-model="form.creator" placeholder="请输入创建人" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="执行人" prop="executor">
                <el-input v-model="form.executor" placeholder="请输入执行人" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="cancel">取 消</el-button>
            <el-button type="primary" @click="submitForm">确 定</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import {
  listTask, addTask, updateTask, delTask, getTask,
  getCurrentDataSourceInfo
} from '@/api/apiManager.js';
import {
  useDataSourceState,
  switchDataSource,
  DataSourceType,
  getCurrentDataSourceConfig
} from '@/utils/dataSourceManager';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Monitor, Van } from '@element-plus/icons-vue';

const router = useRouter();

// --- 数据源相关状态 ---
const { currentDataSource, isDataSourceChanging, dataSourceState } = useDataSourceState();
const currentDataSourceInfo = computed(() => getCurrentDataSourceInfo());
const isLocalDataSource = computed({
  get: () => currentDataSource.value === DataSourceType.LOCAL,
  set: (value) => {
    // 这个setter不会被直接调用，通过handleDataSourceChange处理
  }
});

// --- 响应式状态定义 ---
const loading = ref(true);
const taskList = ref([]);
const total = ref(0);
const testingConnection = ref(false);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  taskCode: '',
  creator: '',
  executor: '',
  taskStatus: ''
});

const statusOptions = ref([
  { value: '', label: '全部' },
  { value: '待巡视', label: '待巡视' },
  { value: '巡视中', label: '巡视中' },
  { value: '待上传', label: '待上传' },
  { value: '已完成', label: '已完成' }
]);

const dialog = reactive({
  open: false,
  title: ''
});

const form = ref({});
const rules = reactive({
  taskName: [{ required: true, message: "任务名称不能为空", trigger: "blur" }],
  taskCode: [{ required: true, message: "任务编号不能为空", trigger: "blur" }],
  creator: [{ required: true, message: "创建人不能为空", trigger: "blur" }],
  executor: [{ required: true, message: "执行人不能为空", trigger: "blur" }]
});

// --- DOM引用 ---
const queryFormRef = ref(null);
const taskFormRef = ref(null);

// --- 新增：任务编号生成模式 ---
const taskCodeSelection = ref('auto');

// --- 任务编号占位符和自动完成 ---
const taskCodePlaceholder = computed(() => {
  const date = new Date();
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  return `TASK-${year}${month}${day}`;
});

// --- 监听任务编号模式变化 ---
watch(taskCodeSelection, (newValue) => {
  // 如果是编辑模式，则不执行任何操作
  if (form.value.id) {
    return;
  }
  if (newValue === 'auto') {
    form.value.taskCode = taskCodePlaceholder.value;
  } else {
    form.value.taskCode = '';
  }
});

// --- 数据源切换方法 ---
const handleDataSourceChange = async (value) => {
  const targetDataSource = value ? DataSourceType.LOCAL : DataSourceType.VEHICLE;

  try {
    const result = await switchDataSource(targetDataSource);
    if (result.success) {
      ElMessage.success(result.message);
      // 重新加载任务列表
      getList();
    } else {
      ElMessage.error(result.message);
    }
  } catch (error) {
    console.error('切换数据源失败:', error);
    ElMessage.error('切换数据源失败，请重试');
  }
};

// --- 测试API连接方法 ---
const testApiConnection = async () => {
  testingConnection.value = true;
  try {
    console.log('=== 开始测试API连接 ===');
    console.log('当前数据源:', currentDataSourceInfo.value);
    console.log('当前数据源配置:', getCurrentDataSourceConfig());

    // 测试1：直接访问后端（不使用代理）
    console.log('=== 测试1：直接访问后端 ===');
    const axios = await import('axios');
    try {
      const directBackendResponse = await axios.default.get('http://localhost:8080/agv/task/list?pageNum=1&pageSize=5');
      console.log('直接访问后端成功:', directBackendResponse.data);
    } catch (directBackendError) {
      console.error('直接访问后端失败:', directBackendError.message);
      console.error('错误状态:', directBackendError.response?.status);
    }

    // 测试2：通过代理访问
    console.log('=== 测试2：通过代理访问 ===');
    const testUrl = '/local-api/agv/task/list?pageNum=1&pageSize=5';
    console.log('测试请求URL:', testUrl);

    try {
      const proxyResponse = await axios.default.get(testUrl);
      console.log('=== 代理请求结果 ===');
      console.log('响应状态:', proxyResponse.status);
      console.log('响应头:', proxyResponse.headers);
      console.log('原始响应数据:', proxyResponse.data);
    } catch (proxyError) {
      console.error('代理请求失败:', proxyError);
      console.error('错误详情:', proxyError.response?.data);
      console.error('错误状态:', proxyError.response?.status);
    }

    // 测试3：通过apiManager的请求
    console.log('=== 测试3：通过apiManager测试 ===');
    const response = await listTask({ pageNum: 1, pageSize: 5 });

    console.log('=== API响应详情 ===');
    console.log('原始响应:', response);
    console.log('响应类型:', typeof response);
    console.log('是否为数组:', Array.isArray(response));
    if (response && typeof response === 'object') {
      console.log('响应对象的键:', Object.keys(response));
      console.log('响应对象的值:', response);
    }

    ElMessage.success('API连接测试完成，请查看控制台输出');
  } catch (error) {
    console.error('API连接测试失败:', error);
    console.error('错误详情:', error.response?.data);
    console.error('错误状态:', error.response?.status);
    console.error('错误配置:', error.config);
    ElMessage.error(`API连接测试失败: ${error.message}`);
  } finally {
    testingConnection.value = false;
  }
};

// --- 方法定义 ---
onMounted(() => {
  // 【新】页面加载时，安全地恢复页码
  const savedPageNum = sessionStorage.getItem('taskListPageNum');
  if (savedPageNum) {
    const pageNum = parseInt(savedPageNum, 10);
    if (!isNaN(pageNum) && pageNum > 0) {
      queryParams.pageNum = pageNum;
    }
    // 立即清除，防止刷新页面时依然停留在旧页码
    sessionStorage.removeItem('taskListPageNum');
  }

  getList();
});

// 【核心修复】获取任务列表
async function getList() {
  loading.value = true;
  try {
    const response = await listTask(queryParams);

    // 增加调试日志，查看从API获取的原始数据
    console.log("从后端接收到的任务列表响应:", response);
    console.log("响应类型:", typeof response);
    console.log("是否为数组:", Array.isArray(response));
    if (response && typeof response === 'object') {
      console.log("响应对象的键:", Object.keys(response));
    }

    // 处理不同的API响应格式
    if (response) {
      // 小车API格式：{ rows: [...], total: number }
      if (response.rows && typeof response.total !== 'undefined') {
        taskList.value = response.rows;
        total.value = response.total;
      }
      // 本地API格式：直接是数组或者 { data: [...], total: number }
      else if (Array.isArray(response)) {
        taskList.value = response;
        total.value = response.length;
      }
      // 本地API格式：{ data: [...], total: number }
      else if (response.data && Array.isArray(response.data)) {
        taskList.value = response.data;
        total.value = response.total || response.data.length;
      }
      // 其他格式，尝试作为数组处理
      else if (typeof response === 'object') {
        console.warn("未知的API响应格式，尝试作为数组处理:", response);
        const dataArray = Object.values(response).find(item => Array.isArray(item));
        if (dataArray) {
          taskList.value = dataArray;
          total.value = dataArray.length;
        } else {
          throw new Error("无法解析API响应格式");
        }
      }
      else {
        throw new Error("API响应格式不正确");
      }
    } else {
      // 响应为空，设置为空数组
      taskList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error("获取任务列表失败:", error);
    ElMessage.error(`获取任务列表时出错: ${error.message}`);
    taskList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 搜索按钮操作
function handleQuery() {
  queryParams.pageNum = 1;
  getList();
}

// 重置按钮操作
function resetQuery() {
  // 【新】重置时清除页码记忆
  sessionStorage.removeItem('taskListPageNum');
  queryFormRef.value.resetFields();
  handleQuery();
}

// --- 【重构】表单重置 ---
function resetForm() {
  form.value = {};
  if (taskFormRef.value) {
    // 清除element-plus表单的校验信息
    taskFormRef.value.resetFields();
  }
}

// --- 【重构】对话框取消按钮 ---
function cancel() {
  dialog.open = false;
  resetForm();
}

// 新增任务按钮操作
function handleCreate() {
  resetForm();
  taskCodeSelection.value = 'auto'; // 恢复默认选项
  form.value.taskCode = taskCodePlaceholder.value; // 设置初始值
  dialog.open = true;
  dialog.title = "新增任务";
}

// 修改按钮操作
function handleEdit(row) {
  resetForm(); // 先重置表单
  getTask(row.id).then(response => {
    // 【修复】getTask经过拦截器处理，返回的response本身就是任务数据对象
    form.value = response;
    taskCodeSelection.value = 'custom'; // 编辑时模式固定为自定义
    dialog.open = true;
    dialog.title = "修改任务";
  });
}

// --- 【最终修复】删除按钮操作 ---
function handleDelete(id, taskCode) {
  // 【调试】打印入参，检查数据是否正确
  console.log(`[ handleDelete ] raw args: id=${id}, taskCode=${taskCode}`);

  ElMessageBox.confirm(
    `是否确认删除任务编号为"${taskCode}"的数据项?`,
    "系统提示",
    {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning",
    }
  ).then(() => {
    return delTask(id);
  }).then(() => {
    ElMessage.success("删除成功");
    getList(); // 重新加载列表
  }).catch(() => {
    // 用户点击了取消
  });
}

// 提交按钮
async function submitForm() {
  await taskFormRef.value.validate();
  if (form.value.id != null) {
    await updateTask(form.value);
    ElMessage.success("修改成功");
  } else {
    await addTask(form.value);
    ElMessage.success("新增成功");
  }
  dialog.open = false;
  getList();
}

// 复盘/查看报告 按钮操作
function handleReview(row) {
  sessionStorage.setItem('taskListPageNum', queryParams.pageNum);
  router.push({ name: 'task-review', params: { id: row.id } });
}

// 开始/继续巡视 按钮操作
async function handleStartPatrol(row) {
  sessionStorage.setItem('taskListPageNum', queryParams.pageNum);
  if (row.taskStatus === '待巡视') {
    try {
      // 手动更新任务状态为"巡视中"
      const updated = { ...row, taskStatus: '巡视中' };
      await updateTask(updated);
      // ElMessage.success('任务状态已手动更新为"巡视中"');
      router.push({ name: 'task-execute', params: { id: row.id } });
    } catch (error) {
      ElMessage.error('手动更新任务状态失败: ' + (error?.message || '未知错误'));
      console.error('手动更新任务状态失败:', error);
    }
  } else {
    router.push({ name: 'task-execute', params: { id: row.id } });
  }
}

// 根据任务状态，决定点击整行时的行为
function handleTaskClick(row) {
  const status = row.taskStatus;
  if (status === '待巡视' || status === '巡视中') {
    handleStartPatrol(row);
  } else if (status === '待上传' || status === '已完成') {
    handleReview(row);
  }
}

// --- 【重构】设置按钮操作 ---
function handleSettings() {
  router.push({ name: 'settings' });
}

// 返回操作
function goBack() {
  router.back();
}

// 任务状态Tag样式
function getStatusTagType(status) {
  switch (status) {
    case '待巡视': return 'info';
    case '巡视中': return 'primary';
    case '待上传': return 'warning';
    case '已完成': return 'success';
    default: return 'info';
  }
}
</script>

<style scoped>
.task-list-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
  box-sizing: border-box;
}
.app-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}
.page-header {
  margin-bottom: 20px;
  padding: 0;
}
.data-source-switch {
  margin-bottom: 20px;
}

.switch-card {
  border: 1px solid #e4e7ed;
}

.switch-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.switch-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-icon {
  font-size: 24px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.switch-icon.local {
  color: #409eff;
  background-color: #ecf5ff;
}

.switch-icon.vehicle {
  color: #67c23a;
  background-color: #f0f9ff;
}

.switch-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.switch-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.switch-desc {
  font-size: 12px;
  color: #909399;
}

.data-source-switch {
  transform: scale(1.1);
}

.search-form-container {
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.el-form--inline .el-form-item {
  margin-right: 15px;
  margin-bottom: 18px;
}
.el-form-item.action-buttons {
  margin-right: 0;
  align-self: flex-end;
}
.toolbar {
  margin-bottom: 20px;
}
.table-wrapper {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}
.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>
