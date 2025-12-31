-- Migration: Create scheduled_job_executions table for job monitoring
-- Author: System
-- Date: 2025-12-31
-- Description: Track all scheduled job executions for SUPERADMIN monitoring, retry, and control

CREATE TABLE scheduled_job_executions (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED', 'CANCELED')),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_ms BIGINT,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message TEXT,
    stack_trace TEXT,
    items_processed INTEGER,
    items_failed INTEGER,
    metadata TEXT,
    manually_triggered BOOLEAN NOT NULL DEFAULT FALSE,
    triggered_by VARCHAR(100),
    canceled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_job_executions_job_name ON scheduled_job_executions(job_name);
CREATE INDEX idx_job_executions_status ON scheduled_job_executions(status);
CREATE INDEX idx_job_executions_start_time ON scheduled_job_executions(start_time DESC);
CREATE INDEX idx_job_executions_failed_retry ON scheduled_job_executions(status, retry_count) WHERE status = 'FAILED';

-- Comments
COMMENT ON TABLE scheduled_job_executions IS 'Tracks all scheduled job executions for monitoring and auditing';
COMMENT ON COLUMN scheduled_job_executions.job_name IS 'Job identifier matching ScheduledTasks method name';
COMMENT ON COLUMN scheduled_job_executions.status IS 'Current job status: RUNNING, COMPLETED, FAILED, or CANCELED';
COMMENT ON COLUMN scheduled_job_executions.retry_count IS 'Number of retry attempts for this execution';
COMMENT ON COLUMN scheduled_job_executions.manually_triggered IS 'TRUE if SUPERADMIN manually triggered this job';
COMMENT ON COLUMN scheduled_job_executions.triggered_by IS 'SUPERADMIN email who triggered the job (if manual)';
