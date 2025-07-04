// src/api/index.js 或 src/services/api.js

// 后端基础URL，请根据你的实际部署环境修改
const BASE_URL = 'http://localhost:8080';

/**
 * 通用请求函数，处理后端返回的 AjaxResult 结构。
 * 会自动解析响应，如果 code 为 200 则返回 data 字段，否则抛出错误。
 * @param {string} url 请求的完整URL
 * @param {object} options fetch API 的选项，如 method, headers, body 等
 * @returns {Promise<any>} 后端返回的 data 字段内容
 * @throws {Error} 如果HTTP请求失败或后端返回的 code 不为 200
 */
async function request(url, options = {}) {
    try {
        const response = await fetch(url, options);

        // 检查HTTP响应状态码
        if (!response.ok) {
            let errorMsg = `网络请求失败，HTTP状态码: ${response.status}`;
            try {
                const errorData = await response.json();
                // 如果后端返回了 AjaxResult 格式的错误信息
                if (errorData && errorData.msg) {
                    errorMsg = errorData.msg;
                }
            } catch (parseError) {
                // 如果无法解析JSON，则使用默认错误信息
                console.error('解析后端错误响应失败:', parseError);
            }
            throw new Error(errorMsg);
        }

        const result = await response.json(); // 解析 AjaxResult 或 TableDataInfo

        // 检查后端业务逻辑码 (AjaxResult.code)
        if (result.code === 200) {
            // 对于列表查询 (TableDataInfo)，返回整个对象，包含 total 和 rows
            if (result.rows !== undefined && result.total !== undefined) {
                return result; // 返回 TableDataInfo 结构
            }
            return result.data; // 对于其他操作，返回 data 字段
        } else {
            // 后端业务逻辑错误
            throw new Error(result.msg || '后端业务处理失败');
        }
    } catch (error) {
        console.error('API请求过程中发生错误:', error);
        throw error; // 重新抛出错误，供调用者处理
    }
}

// =====================================================================
// 缺陷管理相关接口 (FlawController)
// 基础路径: /agv/flaw
// =====================================================================

const FLAW_API_BASE = `${BASE_URL}/agv/flaw`;

/**
 * 获取缺陷列表。
 * @param {object} params - 过滤和分页参数。
 * @param {number} [params.pageNum=1] - 页码。
 * @param {number} [params.pageSize=10] - 每页大小。
 * @param {string} [params.defectCode] - 缺陷编号 (模糊查询)。
 * @param {number} [params.taskId] - 所属任务ID。
 * @param {string} [params.flawType] - 缺陷类型。
 * @param {string} [params.flawName] - 缺陷名称 (模糊查询)。
 * @param {boolean} [params.confirmed] - 是否确认属实。
 * @param {boolean} [params.uploaded] - 是否已上传。
 * @param {string} [params.level] - 缺陷等级。
 * @returns {Promise<{total: number, rows: Array<object>}>} - 包含总数和缺陷列表的对象。
 */
export async function getFlawList(params = {}) {
    const queryParams = new URLSearchParams(params).toString();
    return request(`${FLAW_API_BASE}/list?${queryParams}`, {
        method: 'GET'
    });
}

/**
 * 根据ID获取缺陷详情。
 * @param {number} id - 缺陷ID。
 * @returns {Promise<object>} - AgvFlaw 对象。
 */
export async function getFlawDetail(id) {
    return request(`${FLAW_API_BASE}/${id}`, {
        method: 'GET'
    });
}

/**
 * 添加新缺陷。
 * @param {object} flawData - FlawDTO 对象。
 * @param {string} flawData.defectCode - 缺陷编号 (必填)。
 * @param {number} flawData.taskId - 所属任务ID (必填)。
 * @param {string} flawData.flawType - 缺陷类型 (必填)。
 * @param {string} flawData.flawName - 缺陷名称 (必填)。
 * @param {string} [flawData.flawDesc] - 缺陷描述。
 * @param {number} [flawData.round] - 巡视轮次。
 * @param {number} [flawData.flawDistance] - 距离原点的距离。
 * @param {string} [flawData.flawImage] - 缺陷图片路径。
 * @param {string} [flawData.flawImageUrl] - 缺陷图片URL。
 * @param {string} [flawData.flawRtsp] - 缺陷视频流地址。
 * @param {string} [flawData.level] - 缺陷等级。
 * @param {number} [flawData.countNum] - 缺陷数量。
 * @param {string} [flawData.remark] - 补充说明。
 * @returns {Promise<object>} - 新增的 AgvFlaw 对象。
 */
export async function addFlaw(flawData) {
    return request(FLAW_API_BASE, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(flawData)
    });
}

/**
 * 更新缺陷信息。
 * @param {object} flawData - 包含ID和要更新字段的 FlawDTO 对象。
 * @param {number} flawData.id - 缺陷ID (必填)。
 * @param {string} [flawData.defectCode] - 缺陷编号。
 * @param {number} [flawData.taskId] - 所属任务ID。
 * @param {string} [flawData.flawType] - 缺陷类型。
 * @param {string} [flawData.flawName] - 缺陷名称。
 * // ... 其他 FlawDTO 字段
 * @returns {Promise<object>} - 更新后的 AgvFlaw 对象。
 */
export async function updateFlaw(flawData) {
    return request(FLAW_API_BASE, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(flawData)
    });
}

/**
 * 逻辑删除缺陷。
 * @param {number} id - 缺陷ID。
 * @returns {Promise<object>} - 被删除的 AgvFlaw 对象。
 */
export async function deleteFlaw(id) {
    return request(`${FLAW_API_BASE}/${id}`, {
        method: 'DELETE'
    });
}

/**
 * 确认缺陷。
 * @param {number} id - 缺陷ID。
 * @returns {Promise<object>} - 确认后的 AgvFlaw 对象。
 */
export async function confirmFlaw(id) {
    return request(`${FLAW_API_BASE}/confirm/${id}`, {
        method: 'PUT'
    });
}

/**
 * 标记缺陷为已上传。
 * @param {number} id - 缺陷ID。
 * @returns {Promise<object>} - 标记后的 AgvFlaw 对象。
 */
export async function uploadFlaw(id) {
    return request(`${FLAW_API_BASE}/upload/${id}`, {
        method: 'PUT'
    });
}

/**
 * 轮询获取任务实时缺陷信息。
 * @param {number} taskId - 任务ID。
 * @returns {Promise<Array<object>>} - 实时缺陷 AgvFlaw 对象列表。
 */
export async function getLiveFlawsByTaskId(taskId) {
    return request(`${FLAW_API_BASE}/live/${taskId}`, {
        method: 'GET'
    });
}

/**
 * 检查任务下所有缺陷是否已全部确认。
 * @param {number} taskId - 任务ID。
 * @returns {Promise<boolean>} - 如果所有缺陷都已确认则为 true，否则为 false。
 */
export async function checkAllFlawsConfirmed(taskId) {
    return request(`${FLAW_API_BASE}/checkConfirmed/${taskId}`, {
        method: 'GET'
    });
}


// =====================================================================
// 任务管理相关接口 (TaskController)
// 基础路径: /agv/task
// =====================================================================

const TASK_API_BASE = `${BASE_URL}/agv/task`;

/**
 * 获取任务列表。
 * @param {object} params - 过滤和分页参数。
 * @param {number} [params.pageNum=1] - 页码。
 * @param {number} [params.pageSize=10] - 每页大小。
 * @param {string} [params.taskCode] - 任务编号 (模糊查询)。
 * @param {string} [params.creator] - 创建人。
 * @param {string} [params.executor] - 执行人。
 * @param {string} [params.taskStatus] - 任务状态。
 * @returns {Promise<{total: number, rows: Array<object>}>} - 包含总数和任务列表的对象。
 */
export async function getTaskList(params = {}) {
    const queryParams = new URLSearchParams(params).toString();
    return request(`${TASK_API_BASE}/list?${queryParams}`, {
        method: 'GET'
    });
}

/**
 * 根据ID获取任务详情。
 * @param {number} id - 任务ID。
 * @returns {Promise<object>} - AgvTask 对象。
 */
export async function getTaskDetail(id) {
    return request(`${TASK_API_BASE}/${id}`, {
        method: 'GET'
    });
}

/**
 * 添加新任务。
 * @param {object} taskData - TaskDTO 对象。
 * @param {string} taskData.taskCode - 任务编号 (必填)。
 * @param {string} taskData.taskName - 任务名称 (必填)。
 * @param {string} taskData.startPos - 起始地点 (必填)。
 * @param {string} taskData.taskTrip - 任务距离 (必填)。
 * @param {string} taskData.creator - 创建人 (必填)。
 * @param {string} taskData.executor - 执行人 (必填)。
 * @returns {Promise<object>} - 新增的 AgvTask 对象。
 */
export async function addTask(taskData) {
    return request(TASK_API_BASE, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(taskData)
    });
}

/**
 * 更新任务信息。
 * @param {object} taskData - 包含ID和要更新字段的 TaskDTO 对象。
 * @param {number} taskData.id - 任务ID (必填)。
 * // ... 其他 TaskDTO 字段
 * @returns {Promise<object>} - 更新后的 AgvTask 对象。
 */
export async function updateTask(taskData) {
    return request(TASK_API_BASE, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(taskData)
    });
}

/**
 * 逻辑删除任务。
 * @param {number} id - 任务ID。
 * @returns {Promise<object>} - 被删除的 AgvTask 对象。
 */
export async function deleteTask(id) {
    return request(`${TASK_API_BASE}/${id}`, {
        method: 'DELETE'
    });
}

/**
 * 启动任务。
 * @param {number} id - 任务ID。
 * @returns {Promise<object>} - 启动后的 AgvTask 对象。
 */
export async function startTask(id) {
    return request(`${TASK_API_BASE}/start/${id}`, {
        method: 'PUT'
    });
}

/**
 * 结束任务。
 * @param {number} id - 任务ID。
 * @param {boolean} [isAbort=false] - 是否中止任务。
 * @returns {Promise<object>} - 结束后的 AgvTask 对象。
 */
export async function endTask(id, isAbort = false) {
    const queryParam = isAbort ? '?isAbort=true' : '';
    return request(`${TASK_API_BASE}/stop/${id}${queryParam}`, {
        method: 'PUT'
    });
}

/**
 * 查询待上传任务数据。
 * @param {number} id - 任务ID。
 * @returns {Promise<object>} - 待上传任务的 AgvTask 对象。
 */
export async function preUploadTask(id) {
    return request(`${TASK_API_BASE}/preUpload/${id}`, {
        method: 'PUT'
    });
}

/**
 * 上传任务数据。
 * @param {number} id - 任务ID。
 * @returns {Promise<object>} - 上传后的 AgvTask 对象。
 */
export async function uploadTask(id) {
    return request(`${TASK_API_BASE}/upload/${id}`, {
        method: 'PUT'
    });
}

// 你可以在这里继续添加其他模块的API接口，例如系统配置、AGV移动控制等。
// 例如：
// const CONFIG_API_BASE = `${BASE_URL}/agv/config`;
// export async function getConfig() { return request(CONFIG_API_BASE, { method: 'GET' }); }
// export async function updateConfig(configData) { /* ... */ }
