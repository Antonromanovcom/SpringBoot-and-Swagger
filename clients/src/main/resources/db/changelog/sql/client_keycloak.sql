-- Данные клиента для авторизации в keycloak
CREATE TABLE client_keycloak
(
    client_id   BIGINT NOT NULL PRIMARY KEY,
    login       TEXT,
    keycloak_id UUID,
    created_at  TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES account_application (id)
);
