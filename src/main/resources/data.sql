-- 金仓可能需要设置搜索路径
SET search_path TO public;

INSERT INTO agv_task (task_code, task_name, start_pos, task_trip, creator, executor, create_time, task_status, uploaded, delete_flag)
VALUES
    ('TASK-0001', '隧道常规巡检', '200米', '500米', '系统管理员', '张三', NOW(), '待巡视', false, false),
    ('TASK-0002', '紧急故障排查', '200米', '300米', '系统管理员', '李四', NOW(), '待巡视', false, false);