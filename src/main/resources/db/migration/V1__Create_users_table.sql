CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE tb_users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON tb_users(email);
CREATE INDEX idx_users_active ON tb_users(active);
CREATE INDEX idx_users_name ON tb_users(name);

COMMENT ON TABLE tb_users IS 'Users table for the digital wallet system';
COMMENT ON COLUMN tb_users.id IS 'Unique identifier for the user (UUID)';
COMMENT ON COLUMN tb_users.name IS 'Full name of the user';
COMMENT ON COLUMN tb_users.email IS 'Email address of the user (unique)';
COMMENT ON COLUMN tb_users.phone IS 'Phone number of the user (optional)';
COMMENT ON COLUMN tb_users.active IS 'Whether the user account is active';
COMMENT ON COLUMN tb_users.created_at IS 'Timestamp when the user was created';
COMMENT ON COLUMN tb_users.updated_at IS 'Timestamp when the user was last updated';