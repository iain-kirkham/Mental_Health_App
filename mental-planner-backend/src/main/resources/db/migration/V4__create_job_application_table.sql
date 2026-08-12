CREATE TABLE IF NOT EXISTS job_application (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    role_title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    CONSTRAINT chk_job_application_status CHECK (status IN ('APPLIED', 'INTERVIEWING', 'OFFER', 'REJECTED'))
);

CREATE INDEX IF NOT EXISTS idx_job_application_user_id ON job_application(user_id);

