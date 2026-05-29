-- binary_content
CREATE TABLE binary_contents
(
    id           uuid PRIMARY KEY,
    created_at   TIMESTAMP with time zone NOT NULL,
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL,
    bytes        bytea                    NOT NULL
);

-- channels
CREATE TABLE channels
(
    id          uuid PRIMARY KEY,
    created_at  TIMESTAMP with time zone NOT NULL,
    updated_at  TIMESTAMP with time zone,
    name        VARCHAR(100),
    description VARCHAR(500),
    type        VARCHAR(10)              NOT NULL CHECK (type in ('PUBLIC', 'PRIVATE'))
);

-- users
CREATE TABLE users
(
    id         uuid PRIMARY KEY,
    created_at TIMESTAMP with time zone NOT NULL,
    updated_at TIMESTAMP with time zone,
    username   VARCHAR(50)              NOT NULL UNIQUE,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    password   VARCHAR(60)              NOT NULL,
    profile_id uuid UNIQUE,
    CONSTRAINT fk_users_profile FOREIGN KEY (profile_id)
        REFERENCES binary_contents (id) ON DELETE SET NULL
);

-- user_statuses
CREATE TABLE user_statuses
(
    id             uuid PRIMARY KEY,
    created_at     TIMESTAMP with time zone NOT NULL,
    updated_at     TIMESTAMP with time zone,
    user_id        uuid UNIQUE,
    last_active_at TIMESTAMP with time zone NOT NULL,
    CONSTRAINT fk_user_statuses_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
);

-- messages
CREATE TABLE messages
(
    id         uuid PRIMARY KEY,
    created_at TIMESTAMP with time zone NOT NULL,
    updated_at TIMESTAMP with time zone,
    content    TEXT,
    channel_id uuid,
    author_id  uuid,
    CONSTRAINT fk_messages_channel FOREIGN KEY (channel_id)
        REFERENCES channels (id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_author FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE SET NULL
);

-- read_statuses
CREATE TABLE read_statuses
(
    id           uuid PRIMARY KEY,
    created_at   TIMESTAMP with time zone NOT NULL,
    updated_at   TIMESTAMP with time zone,
    user_id      uuid                     NOT NULL,
    channel_id   uuid                     NOT NULL,
    last_read_at TIMESTAMP with time zone NOT NULL,
    UNIQUE (user_id, channel_id),
    CONSTRAINT fk_read_statuses_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_read_statuses_channel FOREIGN KEY (channel_id)
        REFERENCES channels (id) ON DELETE CASCADE
);

-- message_attachments
CREATE TABLE message_attachments
(
    message_id    uuid NOT NULL,
    attachment_id uuid NOT NULL,
    CONSTRAINT fk_message_attachments_message FOREIGN KEY (message_id)
        REFERENCES messages (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_attachments_binary FOREIGN KEY (attachment_id)
        REFERENCES binary_contents (id) ON DELETE CASCADE
)

