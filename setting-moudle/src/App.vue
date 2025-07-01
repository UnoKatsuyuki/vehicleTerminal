<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';

// --- 设置 ---
// 现在所有 API 请求都将指向 Vite 开发服务器的 /prod-api 路径，
// 然后由 Vite 转发到真实的后端地址。
// 注意：在生产构建中，您可能需要一个不同的策略（例如，使用Nginx反向代理）。
const API_BASE_URL = '/prod-api';

// --- 状态管理 (State Management) ---
const isLoading = ref(false);
const errorMessage = ref('');
const successMessage = ref('');
const showError = ref(false);
const showSuccess = ref(false);
const accordionValue = ref([true, false, false, false]); // 默认展开第一个

// 使用 reactive 创建响应式表单对象
const form = reactive({
    id: null,
    host: '',
    drivePort: null,
    analysisPort: null,
    cloudUrl: '',
    cameras: [
        { id: 1, name: '前方主视角', address: '', user: '', password: '', showPassword: false },
        { id: 2, name: '左侧视角', address: '', user: '', password: '', showPassword: false },
        { id: 3, name: '右侧视角', address: '', user: '', password: '', showPassword: false },
        { id: 4, name: '后方视角', address: '', user: '', password: '', showPassword: false },
    ]
});

// --- 方法 (Methods) ---

// 将从API获取的数据应用到表单
const applyDataToForm = (data) => {
    form.id = data.id;
    form.host = data.host;
    form.drivePort = data.drivePort;
    form.analysisPort = data.analysisPort;
    form.cloudUrl = data.cloudUrl;
    form.cameras.forEach((cam, index) => {
        cam.address = data[`cam${index + 1}`] || '';
        cam.user = data[`username${index + 1}`] || '';
        cam.password = data[`password${index + 1}`] || '';
    });
};

// 显示通知
const showNotification = (message, type = 'success') => {
    if (type === 'success') {
        successMessage.value = message;
        showSuccess.value = true;
        setTimeout(() => showSuccess.value = false, 3000);
    } else {
        errorMessage.value = message;
        showError.value = true;
        setTimeout(() => showError.value = false, 3000);
    }
};

// 获取配置
const fetchConfig = async () => {
    isLoading.value = true;
    try {
        const response = await axios.get(`${API_BASE_URL}/agv/config`);
        if (response.data.code === 200 && response.data.data) {
            applyDataToForm(response.data.data);
            showNotification('配置已成功加载');
        } else {
            throw new Error(response.data.msg || '获取配置失败');
        }
    } catch (err) {
        const message = err.response?.data?.msg || err.message || '加载配置时发生未知错误';
        showNotification(message, 'error');
    } finally {
        isLoading.value = false;
    }
};

// 保存设置
const handleSave = async () => {
    isLoading.value = true;
    // 数据转换：从表单状态转为API格式
    const dataToSend = {
        id: form.id, host: form.host, drivePort: form.drivePort,
        analysisPort: form.analysisPort, cloudUrl: form.cloudUrl
    };
    form.cameras.forEach((cam, index) => {
        dataToSend[`cam${index + 1}`] = cam.address;
        dataToSend[`username${index + 1}`] = cam.user;
        dataToSend[`password${index + 1}`] = cam.password;
    });

    console.log("准备保存的数据:", dataToSend);

    try {
        const response = await axios.put(`${API_BASE_URL}/agv/config`, dataToSend, {
            headers: { 'Content-Type': 'application/json' }
        });
        if (response.data.code === 200) {
            showNotification('设置已成功保存！');
        } else {
            throw new Error(response.data.msg || '保存失败');
        }
    } catch (err) {
        const message = err.response?.data?.msg || err.message || '保存时发生未知错误';
        showNotification(message, 'error');
    } finally {
        isLoading.value = false;
    }
};

// 取消操作
const handleCancel = () => {
    console.log("操作已取消，重新加载配置...");
    fetchConfig();
};

// --- 生命周期钩子 (Lifecycle Hook) ---
onMounted(() => {
    fetchConfig();
});
</script>

<template>
    <div class="app-container">
        <!-- 顶部通知栏 -->
        <div class="notification-bar">
            <va-alert v-model="showSuccess" color="success" class="mb-4" closeable>
                <va-icon name="check_circle" class="mr-2"></va-icon>
                {{ successMessage }}
            </va-alert>
            <va-alert v-model="showError" color="danger" class="mb-4" closeable>
                <va-icon name="warning" class="mr-2"></va-icon>
                {{ errorMessage }}
            </va-alert>
        </div>

        <h1 class="va-h1" style="text-align: center; margin-bottom: 2rem;">系统设置</h1>

        <!-- 车辆控制设置 -->
        <va-card>
            <va-card-title class="flex items-center">
                <va-icon name="directions_car" color="primary" class="mr-2"></va-icon>
                车辆控制设置
            </va-card-title>
            <va-card-content>
                <va-form tag="div" class="flex flex-col gap-4">
                    <va-input v-model="form.host" label="车辆IP地址 *" placeholder="192.168.1.100"
                        :rules="[value => (value && value.length > 0) || 'IP地址不能为空']" :disabled="isLoading">
                        <template #prepend-inner>
                            <va-icon name="settings_ethernet" color="secondary" />
                        </template>
                    </va-input>
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <va-input v-model.number="form.drivePort" type="number" label="车辆控制端口 *"
                            :rules="[value => (value > 0) || '端口号必须大于0']" :disabled="isLoading"></va-input>
                        <va-input v-model.number="form.analysisPort" type="number" label="分析程序端口 *"
                            :rules="[value => (value > 0) || '端口号必须大于0']" :disabled="isLoading"></va-input>
                    </div>
                </va-form>
            </va-card-content>
        </va-card>

        <!-- 云端平台设置 -->
        <va-card>
            <va-card-title class="flex items-center">
                <va-icon name="cloud_queue" color="info" class="mr-2"></va-icon>
                云端平台设置
            </va-card-title>
            <va-card-content>
                <va-input v-model="form.cloudUrl" label="云端平台地址 *" placeholder="https://api.inspection.com"
                    :rules="[value => (value && value.length > 0) || '云端地址不能为空']" :disabled="isLoading">
                    <template #prepend-inner>
                        <va-icon name="link" color="secondary" />
                    </template>
                </va-input>
            </va-card-content>
        </va-card>

        <!-- 摄像头设置 -->
        <va-card>
            <va-card-title class="flex items-center">
                <va-icon name="videocam" color="success" class="mr-2"></va-icon>
                摄像头设置
            </va-card-title>
            <va-card-content>
                <va-accordion v-model="accordionValue" multiple>
                    <va-collapse v-for="(camera, index) in form.cameras" :key="camera.id"
                        :header="`摄像头 ${camera.id} - ${camera.name}`">
                        <div class="p-4 flex flex-col gap-4">
                            <va-input v-model="camera.address" :label="`摄像头 ${camera.id} 地址`" placeholder="rtsp://..."
                                :disabled="isLoading"></va-input>
                            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <va-input v-model="camera.user" :label="`摄像头 ${camera.id} 账号`" :disabled="isLoading">
                                </va-input>
                                <va-input v-model="camera.password" :type="camera.showPassword ? 'text' : 'password'"
                                    :label="`摄像头 ${camera.id} 密码`" :disabled="isLoading">
                                    <template #append-inner>
                                        <va-icon :name="camera.showPassword ? 'visibility_off' : 'visibility'"
                                            class="cursor-pointer"
                                            @click="camera.showPassword = !camera.showPassword" />
                                    </template>
                                </va-input>
                            </div>
                        </div>
                    </va-collapse>
                </va-accordion>
            </va-card-content>
        </va-card>

        <!-- 操作按钮 -->
        <div class="flex justify-end gap-4 mt-8">
            <va-button preset="secondary" @click="handleCancel" :disabled="isLoading">取消</va-button>
            <va-button @click="handleSave" :loading="isLoading">保存设置</va-button>
        </div>
    </div>
</template>

<style>
body {
    background-color: #f5f8f9;
    /* Vuestic UI 的背景色 */
    font-family: 'Source Sans Pro', sans-serif;
}

.app-container {
    padding: 2rem;
    max-width: 900px;
    margin: auto;
}

.va-card+.va-card {
    margin-top: 2rem;
}

.va-input-wrapper__field {
    background-color: white !important;
}

.notification-bar {
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 1000;
}
</style> 