-- Физлицо (которе может выполнять роли сотрудника, бенефициара и т.п.)
CREATE TABLE if not exists human
(
    id BIGINT PRIMARY KEY, --ключ таблицы
    first_name TEXT,
    middle_name TEXT,
    last_name TEXT,
    snils TEXT,
    registration_address TEXT,
    inn TEXT,
    citizenship TEXT
);
CREATE INDEX IF NOT EXISTS idx_human__id_human ON human (id);


-- Сотрудник клиента
CREATE TABLE if not exists  employee
(
    id                      BIGINT PRIMARY KEY,
    position                TEXT,
    dbo_login               TEXT,
    abs_code                TEXT,
    human_id                BIGINT NOT NULL,
    is_delegate             BOOLEAN,                                     -- сомнительно, т.к. для нахождения представителя есть специальная структура (если только для оптимизации чтения)
    client_id               BIGINT NOT NULL,

    FOREIGN KEY (client_id) REFERENCES account_application (id) ON DELETE CASCADE, -- нет клиента, нет сотрудника
    FOREIGN KEY (human_id) REFERENCES human (id) ON DELETE RESTRICT   -- нельзя удалять человека, если он чей-то сотрудник
);
CREATE INDEX IF NOT EXISTS idx_employee__id_employee ON employee (id);
CREATE INDEX IF NOT EXISTS idx_employee__client_id ON employee (client_id);
CREATE INDEX IF NOT EXISTS idx_employee__human_id ON employee (human_id);


-- Бенефициар клиента
CREATE TABLE if not exists  beneficiary
(
    id BIGINT PRIMARY KEY,

    --stake_fraction DOUBLE PRECISION, -- скользкий вопрос с точностью
    stake_percent INT, -- всегда ли проценты целое?
    stake_absolute BIGINT, -- в рублях, копейках (???)

    human_id BIGINT NOT NULL, -- бенефициар всегда человек
    client_id BIGINT NOT NULL, -- бенефициар всегда у клиента

    FOREIGN KEY (client_id) REFERENCES account_application (id) ON DELETE CASCADE,
    FOREIGN KEY (human_id) REFERENCES human (id) ON DELETE RESTRICT -- нельзя удалить человека, пока он чей-то бенефициар
);
CREATE INDEX IF NOT EXISTS idx_beneficiary__client ON beneficiary (client_id);
CREATE INDEX IF NOT EXISTS idx_beneficiary__human_id ON beneficiary (human_id);


-- Паспорт физического лица (паспортов у человека в течение жизни может быть несколько, один из них действующий на текущий момент)
CREATE TABLE if not exists  passport
(
    id BIGINT PRIMARY KEY,
    number TEXT NOT NULL,
    series TEXT NOT NULL,
    issue_department_code TEXT NOT NULL,
    issue_date TIMESTAMP,
    date_of_birth TIMESTAMP,
    place_of_birth TEXT,
    issue_department_name TEXT NOT NULL,
    is_valid BOOLEAN,
    human_id BIGINT NOT NULL,
    is_main BOOLEAN, -- признак действующего паспорта

    FOREIGN KEY (human_id) REFERENCES human (id) ON DELETE CASCADE -- паспорта удаляются вместе с человеком
);
CREATE INDEX IF NOT EXISTS idx_passport__human ON passport (human_id);

-- Номер телефона сотрудника (телефонов может быть много, один можно пометить, как основной)
CREATE TABLE IF NOT EXISTS phone_employee
(
    id BIGINT PRIMARY KEY,
    value TEXT,
    is_main BOOLEAN,
    owner_id BIGINT NOT NULL,

    FOREIGN KEY (owner_id) REFERENCES employee (id) ON DELETE CASCADE -- нет человека, нет телефонов
);
CREATE INDEX IF NOT EXISTS idx_phone_employee__human ON phone_employee (owner_id);


-- Почта сотрудника (может быть несколько, одну можно пометить основной)
CREATE TABLE IF NOT EXISTS email_employee
(
    id BIGINT PRIMARY KEY,
    value TEXT,
    is_main BOOLEAN,
    owner_id BIGINT NOT NULL,

    FOREIGN KEY (owner_id) REFERENCES employee (id) ON DELETE CASCADE -- нет человека, нет почты
);
CREATE INDEX IF NOT EXISTS idx_email_employee__human ON email_employee (owner_id);

-- Номер телефона бенефициара (телефонов может быть много, один можно пометить, как основной)
CREATE TABLE IF NOT EXISTS phone_beneficiary
(
    id BIGINT PRIMARY KEY,
    value TEXT,
    is_main BOOLEAN,
    owner_id BIGINT NOT NULL,

    FOREIGN KEY (owner_id) REFERENCES beneficiary (id) ON DELETE CASCADE -- нет человека, нет телефонов
);
CREATE INDEX IF NOT EXISTS idx_phone_beneficiary__human ON phone_beneficiary (owner_id);


-- Почта бенефициара (может быть несколько, одну можно пометить основной)
CREATE TABLE IF NOT EXISTS email_beneficiary
(
    id BIGINT PRIMARY KEY,
    value TEXT,
    is_main BOOLEAN,
    owner_id BIGINT NOT NULL,

    FOREIGN KEY (owner_id) REFERENCES beneficiary (id) ON DELETE CASCADE -- нет человека, нет почты
);
CREATE INDEX IF NOT EXISTS idx_email_beneficiary__human ON email_beneficiary (owner_id);
