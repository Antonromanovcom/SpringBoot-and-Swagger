CREATE SEQUENCE IF NOT EXISTS email_from_user_id_seq MINVALUE 1 START 1;

CREATE TABLE public.email_messages
(
    account_application_id BIGINT    NOT NULL,
    msg_content            TEXT      NOT NULL,
    date_time              TIMESTAMP NOT NULL,
    id                     BIGINT DEFAULT nextval('email_from_user_id_seq') PRIMARY KEY

);
CREATE INDEX account_application_id_index ON email_messages (account_application_id);
