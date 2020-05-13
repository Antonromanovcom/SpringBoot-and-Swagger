CREATE TABLE if not exists sm_transition_log
(
    id BIGINT PRIMARY KEY,
    client_id BIGINT,
    created_at TIMESTAMP,
    previous_state VARCHAR(255),
    new_state VARCHAR(255),
    created_by TEXT,
    cause_message TEXT,
    FOREIGN KEY (client_id) REFERENCES account_application (id) ON DELETE RESTRICT
);
CREATE INDEX IF NOT EXISTS idx_sm_transition_log__id_client ON sm_transition_log (client_id);