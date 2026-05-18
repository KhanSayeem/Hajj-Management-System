CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'AGENT', 'PILGRIM')),
    admin_role_key VARCHAR(20) GENERATED ALWAYS AS (CASE WHEN role = 'ADMIN' THEN role ELSE NULL END) STORED,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY ux_users_single_admin (admin_role_key)
);

CREATE TABLE IF NOT EXISTS packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(10) NOT NULL CHECK (type IN ('HAJJ', 'UMRAH')),
    year INT NOT NULL,
    capacity INT NOT NULL,
    price_usd DECIMAL(10,2) NOT NULL,
    departure_date DATE,
    return_date DATE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_packages_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS `groups` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL,
    package_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    max_size INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_groups_package FOREIGN KEY (package_id) REFERENCES packages(id),
    CONSTRAINT fk_groups_agent FOREIGN KEY (agent_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS pilgrims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    group_id BIGINT,
    passport_number VARCHAR(50) UNIQUE NOT NULL,
    date_of_birth DATE,
    nationality VARCHAR(60),
    phone VARCHAR(20),
    gender VARCHAR(10) NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    mahram_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED'
        CHECK (status IN ('REGISTERED', 'APPROVED', 'DEPARTED', 'IN_MAKKAH', 'RETURNED', 'CANCELLED')),
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pilgrims_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_pilgrims_group FOREIGN KEY (group_id) REFERENCES `groups`(id),
    CONSTRAINT fk_pilgrims_mahram FOREIGN KEY (mahram_id) REFERENCES pilgrims(id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    details TEXT,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_id) REFERENCES users(id)
);
