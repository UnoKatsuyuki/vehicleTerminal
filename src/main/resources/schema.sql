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

CREATE TABLE IF NOT EXISTS agv_flaw (
    id BIGSERIAL PRIMARY KEY,
    defect_code VARCHAR(32) NOT NULL UNIQUE,
    task_id BIGINT,
    round INTEGER,
    flaw_type VARCHAR(50),
    flaw_name VARCHAR(100),
    flaw_desc TEXT,
    flaw_distance DOUBLE PRECISION,
    flaw_image VARCHAR(255),
    flaw_image_url VARCHAR(255),
    flaw_rtsp VARCHAR(255),
    shown BOOLEAN DEFAULT FALSE,
    confirmed BOOLEAN DEFAULT FALSE,
    uploaded BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP WITH TIME ZONE,
    remark TEXT,
    flaw_length DOUBLE PRECISION,
    flaw_area DOUBLE PRECISION,
    flaw_level VARCHAR(20),
    count_num INTEGER,
    delete_flag BOOLEAN DEFAULT FALSE
    );

CREATE TABLE IF NOT EXISTS "user" (
    uid BIGSERIAL PRIMARY KEY,
    uname VARCHAR(30) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
    );

CREATE INDEX idx_agv_task_status ON agv_task(task_status);
CREATE INDEX idx_agv_task_executor ON agv_task(executor);
CREATE INDEX idx_agv_flaw_task_id ON agv_flaw(task_id);