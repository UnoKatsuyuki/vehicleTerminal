CREATE TABLE IF NOT EXISTS agv_task (
    id BIGSERIAL PRIMARY KEY,
    task_code VARCHAR(32) NOT NULL UNIQUE,
    task_name VARCHAR(100) NOT NULL,
    start_pos VARCHAR(100) NOT NULL,
    task_trip VARCHAR(50) NOT NULL,
    creator VARCHAR(50) NOT NULL,
    executor VARCHAR(50) NOT NULL,
    exec_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    create_time TIMESTAMP WITH TIME ZONE NOT NULL,
    task_status VARCHAR(20) NOT NULL,
    round INTEGER,
    uploaded BOOLEAN DEFAULT FALSE,
    remark TEXT,
    cloud_task_id BIGINT,
    delete_flag BOOLEAN DEFAULT FALSE
    );

CREATE INDEX idx_agv_task_status ON agv_task(task_status);
CREATE INDEX idx_agv_task_executor ON agv_task(executor);