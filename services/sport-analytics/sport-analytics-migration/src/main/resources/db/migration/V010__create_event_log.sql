CREATE TABLE event_log (
    id          SERIAL PRIMARY KEY,
    event_type  VARCHAR(50)  NOT NULL,
    message     TEXT,
    status      VARCHAR(20)  NOT NULL DEFAULT 'info',
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_event_log_event_type ON event_log (event_type);
CREATE INDEX idx_event_log_created_at ON event_log (created_at);
