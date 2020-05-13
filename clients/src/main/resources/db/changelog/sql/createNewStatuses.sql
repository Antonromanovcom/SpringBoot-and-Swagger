-- создание поля для сохранения состояния карточки клиента в соответствии с набором состояний SM
-- первоначальный маппинг старых статусов в новые

ALTER TABLE account_application ADD client_state VARCHAR(255);

-- умолчательное значение
UPDATE account_application SET client_state = 'NEW_CLIENT';

UPDATE account_application SET client_state = 'NEW_CLIENT' WHERE status = 'CONTACT_INFO_UNCONFIRMED';

UPDATE account_application SET client_state = 'CONTACT_INFO_CONFIRMED' WHERE status = 'CONTACT_INFO_CONFIRMED';

UPDATE account_application SET client_state = 'NO_ANSWER' WHERE status = 'NO_ANSWER';

UPDATE account_application SET client_state = 'WAIT_FOR_DOCS' WHERE status = 'WAIT_FOR_DOCS';

UPDATE account_application SET client_state = 'MANAGER_PROCESSING' WHERE
        status = 'RESERVING' OR
        status = 'NEW' OR
        status = 'MANAGER_PROCESSING' OR
        status = 'NOW_SIGNING' OR
        status = 'NO_ANSWER_DELIVERY' OR
        status = 'APPOINTMENT_MADE' OR
        status = 'SECURITY_PROCESSING' OR
        status = 'ISSUING_CERT' OR
        status = 'GO_OPEN';

UPDATE account_application SET client_state = 'AUTO_DECLINED' WHERE
        status = 'ERR_SECURITY_DECLINE' OR
        status = 'ERR_CLIENT_DECLINE' OR
        status = 'ERR_AUTO_DECLINE' OR
        status = 'ERR_MANAGER_DECLINE' OR
        status = 'BANK_REFUSED';

UPDATE account_application SET client_state = 'ACTIVE_CLIENT' WHERE
        status = 'GO_ACTIVE' OR
        status = 'FULFILLED';

UPDATE account_application SET client_state = 'INACTIVE_CLIENT' WHERE status = 'CLOSED';
