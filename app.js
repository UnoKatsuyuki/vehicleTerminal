const speakButton = document.getElementById('speak-button');
const conversationLog = document.getElementById('conversation-log');
const widget = document.querySelector('.voice-assistant-widget');
const widgetHeader = document.querySelector('.voice-assistant-header');
const minimizeButton = document.getElementById('minimize-button');

// --- NEW: Draggable and Collapsible Widget Logic ---

// 1. Collapsing Logic
function toggleWidget() {
    widget.classList.toggle('collapsed');
}

minimizeButton.addEventListener('click', (e) => {
    e.stopPropagation(); // Prevent drag from starting
    toggleWidget();
});

widget.addEventListener('click', () => {
    if (widget.classList.contains('collapsed')) {
        toggleWidget();
    }
});


// 2. Dragging Logic - REMOVED

// --- END NEW ---


// --- REVISED: Push-to-Talk Speech Recognition Logic ---

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

if (!SpeechRecognition) {
    alert("抱歉，您的浏览器不支持语音识别。");
    speakButton.disabled = true;
    speakButton.textContent = '语音功能不可用';
} else {
    const recognition = new SpeechRecognition();
    recognition.lang = 'zh-CN';
    recognition.continuous = false;
    recognition.interimResults = false;

    // -- 新增: 用于跟踪录音状态 --
    let isListening = false;

    recognition.onstart = () => {
        // 更新按钮状态，但不由 onstart 直接触发 isListening 的变化
        speakButton.textContent = "正在聆listen，再次点击结束...";
        speakButton.classList.add('listening');
    };

    recognition.onend = () => {
        // onend 发生时，确保我们的状态是同步的
        isListening = false;
        speakButton.textContent = "点击说话";
        speakButton.classList.remove('listening');
    };

    recognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        logMessage('您说: ' + transcript);
        processCommand(transcript);
    };

    recognition.onerror = (event) => {
        // 'no-speech' is common if the user clicks but says nothing.
        // 'aborted' is common if the user releases the button early.
        // We can safely ignore these non-critical errors.
        if (event.error !== 'no-speech' && event.error !== 'aborted') {
            logMessage('语音识别错误: ' + event.error);
        }
    };

    // -- 修改: 将 "按住说话" 逻辑改为 "点击切换" --
    speakButton.addEventListener('click', () => {
        if (!isListening) {
            // 开始识别
            try {
                recognition.start();
                isListening = true; // 在成功启动后更新状态
            } catch (e) {
                console.info("语音识别已在运行或启动时出错。", e);
                // 如果启动失败，重置状态
                isListening = false;
            }
        } else {
            // 停止识别
            recognition.stop();
            isListening = false; // 立即更新状态
        }
    });

    // -- 移除旧的事件监听器 --
    // speakButton.addEventListener('mousedown', ...);
    // speakButton.addEventListener('mouseup', ...);
    // speakButton.addEventListener('mouseleave', ...);
}

// The original speakButton.addEventListener('click', ...) and startSpeechRecognition() are now replaced.


function logMessage(message) {
    conversationLog.innerHTML += `<p>${message}</p>`;
    conversationLog.scrollTop = conversationLog.scrollHeight;
}

// 这个函数是所有逻辑的中心
async function processCommand(commandText) {
    const DIFY_API_KEY = 'app-kajqxBWQlKpm4sJI9ywiTPCp'; // 这是您提供的密钥
    const DIFY_ENDPOINT = 'https://api.dify.ai/v1/chat-messages'; // 正确的端点

    let conversationId = window.conversationId || null;

    try {
        const response = await fetch(DIFY_ENDPOINT, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${DIFY_API_KEY}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                query: commandText,
                inputs: {}, // 根据指南，同时发送 query 和一个空的 inputs 对象
                user: 'user-123',
                response_mode: 'streaming', // Agent模式必须使用 streaming
                conversation_id: conversationId
            })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: '无法解析错误信息。' }));
            logMessage(`AI错误: ${response.status} - ${errorData.message || '未知错误'}`);
            return;
        }

        if (!response.body) {
            logMessage('AI错误: 响应体为空。');
            return;
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder('utf-8');
        let fullAnswer = '';
        let toolCalls = [];
        let finalFeedback = '';
        let responseReceived = false;

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            const chunk = decoder.decode(value, { stream: true });
            const lines = chunk.split('\n\n').filter(line => line.trim());

            for (const line of lines) {
                if (line.startsWith('data:')) {
                    const dataStr = line.substring(5).trim();
                    if (dataStr === '[DONE]') continue;

                    try {
                        const data = JSON.parse(dataStr);
                        responseReceived = true;

                        if (!conversationId && data.conversation_id) {
                            window.conversationId = data.conversation_id;
                            conversationId = data.conversation_id;
                        }

                        // agent_thought 事件表示工具调用
                        if (data.event === 'agent_thought' && data.tool && data.tool_input) {
                            logMessage(`AI思考: 准备使用工具 ${data.tool}...`);
                            const toolInput = JSON.parse(data.tool_input);
                            toolCalls.push({
                                tool_name: data.tool,
                                tool_args: toolInput[data.tool]
                            });
                        } else if (data.event === 'agent_message' || data.event === 'message') {
                            fullAnswer += data.answer;
                        } else if (data.event === 'message_end') {
                            if (toolCalls.length > 0) {
                                finalFeedback = await executeLocalApi(toolCalls);
                            } else {
                                finalFeedback = fullAnswer;
                            }
                        } else if (data.event === 'error') {
                            logMessage(`AI错误: ${data.code} - ${data.message}`);
                        }
                    } catch (e) {
                        console.error('解析数据流时出错:', e, '原始数据:', dataStr);
                        logMessage('AI错误: 解析数据流失败，请检查控制台。');
                    }
                }
            }
        }

        if (finalFeedback) {
            logMessage('AI说: ' + finalFeedback);
            speak(finalFeedback);
        } else if (responseReceived) {
            logMessage('AI说: ' + fullAnswer);
            speak(fullAnswer);
        } else {
            logMessage('AI未响应: 未收到任何有效数据。');
        }

    } catch (error) {
        console.error('调用Dify API时出错:', error);
        logMessage('AI错误: 无法连接到Dify服务，请检查网络和浏览器控制台。');
    }
}

async function executeLocalApi(toolCalls) {
    const feedbacks = [];
    const BASE_URL = 'http://192.168.2.57/prod-api';
    const CAMERA_API_BASE_URL = 'http://192.168.2.57/easy-api';

    // 辅助函数：处理API响应并生成反馈文本
    const processResponse = async (response, successMessage) => {
        if (!response.ok) {
            try {
                const errorResult = await response.json();
                return `操作失败: ${errorResult.msg || response.statusText}`;
            } catch (e) {
                return `操作失败，服务器返回 ${response.status}。`;
            }
        }
        const result = await response.json();
        return result.msg || (result.code === 200 || result.code === 0 ? successMessage : `操作失败: ${result.msg}`);
    };

    for (const call of toolCalls) {
        const toolName = call.tool_name;
        const toolArgs = call.tool_args || {};
        let feedback = '';

        try {
            switch (toolName) {
                // --- AGV移动控制 ---
                case 'agvForward':
                    feedback = await processResponse(await fetch(`${BASE_URL}/agv/movement/forward`, { method: 'POST' }), "好的，已命令车辆前进。");
                    break;
                case 'agvStop':
                    feedback = await processResponse(await fetch(`${BASE_URL}/agv/movement/stop`, { method: 'POST' }), "车辆已停止。");
                    break;
                case 'agvBackward':
                    feedback = await processResponse(await fetch(`${BASE_URL}/agv/movement/backward`, { method: 'POST' }), "好的，已命令车辆后退。");
                    break;
                case 'heartbeat': {
                    const response = await fetch(`${BASE_URL}/agv/movement/heartbeat`);
                    const result = await response.json();
                    if (response.ok && result.code === 200) {
                        const status = result.data;
                        feedback = `AGV状态：${status.isRunning ? '正在行驶' : '已停止'}，当前位置 ${status.currentPosition} 米。`;
                    } else {
                        feedback = `查询AGV状态失败: ${result.msg}`;
                    }
                    break;
                }

                // --- 任务管理 ---
                case 'listTask': {
                    const params = new URLSearchParams(toolArgs).toString();
                    const response = await fetch(`${BASE_URL}/agv/task/list?${params}`);
                    const result = await response.json();
                    if (response.ok && result.code === 200) {
                        if (result.rows && result.rows.length > 0) {
                            const taskNames = result.rows.map(t => `${t.taskName}(${t.taskStatus})`).join('；');
                            feedback = `查询到 ${result.total} 个任务：${taskNames}`;
                        } else {
                            feedback = "当前没有任务。";
                        }
                    } else {
                        feedback = `获取任务列表失败: ${result.msg}`;
                    }
                    break;
                }
                case 'addTask': {
                    const requiredArgs = ['taskName', 'startPos', 'taskTrip', 'creator', 'executor'];
                    if (!requiredArgs.every(arg => toolArgs[arg])) {
                        feedback = `新增任务需要提供任务名称、起始地点、任务距离、创建人和执行人。`;
                        break;
                    }
                    const response = await fetch(`${BASE_URL}/agv/task`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(toolArgs)
                    });
                    feedback = await processResponse(response, `任务“${toolArgs.taskName}”已成功创建。`);
                    break;
                }
                case 'startTask': {
                    if (!toolArgs.id) { feedback = "需要提供任务ID才能启动。"; break; }
                    feedback = await processResponse(await fetch(`${BASE_URL}/agv/task/start/${toolArgs.id}`, { method: 'POST' }), `任务 ${toolArgs.id} 已启动。`);
                    break;
                }
                case 'endTask': {
                    if (!toolArgs.id) { feedback = "需要提供任务ID才能结束。"; break; }
                    const isAbort = toolArgs.isAbort || false;
                    const response = await fetch(`${BASE_URL}/agv/task/end/${toolArgs.id}?isAbort=${isAbort}`, { method: 'POST' });
                    feedback = await processResponse(response, `任务 ${toolArgs.id} 已${isAbort ? '中止' : '结束'}。`);
                    break;
                }
                case 'delTask': {
                    if (!toolArgs.id) { feedback = "需要提供任务ID才能删除。"; break; }
                    feedback = await processResponse(await fetch(`${BASE_URL}/agv/task/${toolArgs.id}`, { method: 'DELETE' }), `任务 ${toolArgs.id} 已删除。`);
                    break;
                }
                case 'uploadTask': {
                    if (!toolArgs.id) { feedback = "需要提供任务ID才能上传。"; break; }
                    feedback = await processResponse(await fetch(`${BASE_URL}/agv/task/upload/${toolArgs.id}`, { method: 'POST' }), `任务 ${toolArgs.id} 已开始上传。`);
                    break;
                }

                // --- 缺陷管理 ---
                case 'listFlaw': {
                    const params = new URLSearchParams(toolArgs).toString();
                    const response = await fetch(`${BASE_URL}/agv/flaw/list?${params}`);
                    const result = await response.json();
                    if (response.ok && result.code === 200) {
                        if (result.rows && result.rows.length > 0) {
                            const flawNames = result.rows.map(f => f.flawName).join('；');
                            feedback = `查询到 ${result.total} 个缺陷：${flawNames}`;
                        } else {
                            feedback = "当前没有缺陷记录。";
                        }
                    } else {
                        feedback = `获取缺陷列表失败: ${result.msg}`;
                    }
                    break;
                }
                case 'addFlaw': {
                    if (!toolArgs.taskId || !toolArgs.flawName) { feedback = "需要任务ID和缺陷名称。"; break; }
                    const response = await fetch(`${BASE_URL}/agv/flaw`, {
                        method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(toolArgs)
                    });
                    feedback = await processResponse(response, "缺陷已添加。");
                    break;
                }
                case 'delFlaw': {
                    if (!toolArgs.id) { feedback = "需要提供缺陷ID。"; break; }
                    feedback = await processResponse(await fetch(`${BASE_URL}/agv/flaw/${toolArgs.id}`, { method: 'DELETE' }), `缺陷 ${toolArgs.id} 已删除。`);
                    break;
                }

                // --- 系统检查 ---
                case 'checkFs':
                case 'checkDb':
                case 'checkAgv':
                case 'checkCam': {
                    const checkType = toolName.replace('check', '').toLowerCase();
                    feedback = await processResponse(await fetch(`${BASE_URL}/system/check/${checkType}`), `${checkType.toUpperCase()} 检查正常。`);
                    break;
                }

                // --- 摄像头信息 ---
                case 'deviceList': {
                    const camResponse = await fetch(`${CAMERA_API_BASE_URL}/devices?page=1&size=999`, {
                        headers: { 'Authorization': 'Basic YWRtaW4xMjM6QWRtaW5AMTIz' }
                    });
                    if (camResponse.ok) {
                        const result = await camResponse.json();
                        if (result && result.rows) {
                            const deviceNames = result.rows.map(d => d.name || `摄像头 ${d.id}`).join('、');
                            feedback = `共找到 ${result.rows.length} 个摄像头：${deviceNames}`;
                        } else {
                            feedback = '成功连接摄像头服务，但未找到设备列表。';
                        }
                    } else {
                        feedback = `查询摄像头列表失败，状态码 ${camResponse.status}。`;
                    }
                    break;
                }

                default:
                    feedback = `未知的本地工具: ${toolName}`;
            }
            feedbacks.push(feedback);
        } catch (error) {
            console.error(`执行本地API ${toolName} 时出错:`, error);
            feedbacks.push(`执行 ${toolName} 操作时遇到问题。`);
        }
    }
    return feedbacks.join(' ');
}

function speak(text) {
    // Voice is now hardcoded to 'Dylan' (Beijing Male Voice).
    const selectedVoice = 'Dylan';
    speakWithQwenTTS(text, selectedVoice);
}

async function speakWithQwenTTS(text, voice = 'Dylan') {
    if (!text) {
        console.log("TTS received empty text, skipping.");
        return;
    }
    // logToUI(`AI 正在合成语音 (音色: ${voice})...`); // Status message removed as requested.

    // 后端代理服务器的地址
            const proxyUrl = 'http://localhost:3000/api/qwen-tts';

    try {
        const response = await fetch(proxyUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                text: text,
                voice: voice,
                format: 'mp3',
                sample_rate: 24000
            })
        });

        if (!response.ok) {
            let errorDetails = await response.text();
            try {
                const errorJson = JSON.parse(errorDetails);
                errorDetails = `Code: ${errorJson.code}, Message: ${errorJson.message}`;
            } catch (e) {
                // 如果响应不是JSON格式，直接显示文本
            }
            const errorMessage = `语音合成失败: ${response.status} ${response.statusText}. 详情: ${errorDetails}`;
            console.error(errorMessage);
            logToUI(errorMessage);
            return;
        }

        const audioBlob = await response.blob();
        const audioUrl = URL.createObjectURL(audioBlob);
        const audio = new Audio(audioUrl);

        audio.oncanplaythrough = () => {
            // logToUI('AI 正在说话...'); // Status message removed as requested.
            audio.play();
        };

        audio.onended = () => {
            // logToUI('AI 说完了。'); // Status message removed as requested.
            URL.revokeObjectURL(audioUrl); // 播放完毕后释放资源
        };

        audio.onerror = (e) => {
            logToUI('播放音频失败。');
            console.error('Audio playback error:', e);
            URL.revokeObjectURL(audioUrl); // 出错时也释放资源
        };

    } catch (error) {
        console.error('调用语音合成服务时出错:', error);
        logToUI(`调用TTS服务时出错: ${error.message}`);
    }
}


function logToUI(message) {
    conversationLog.innerHTML += `<p>${message}</p>`;
    conversationLog.scrollTop = conversationLog.scrollHeight;
}
