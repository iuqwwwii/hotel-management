-- Удаляем старые таблицы, чтобы можно было заново создать всю схему
DROP TABLE IF EXISTS profit_report CASCADE;
DROP TABLE IF EXISTS audit_log CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS service_order CASCADE;
DROP TABLE IF EXISTS service CASCADE;
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS image CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS room_type CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS partner CASCADE;
DROP TABLE IF EXISTS role CASCADE;

-- Создаём таблицу ролей
CREATE TABLE role (
    role_id   SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
);

-- Партнёры
CREATE TABLE partner (
    partner_id   SERIAL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    contact_info VARCHAR(255)
);

-- Пользователи
CREATE TABLE "user" (
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    full_name     VARCHAR(100),
    card_number   VARCHAR(20),
    card_expiry   VARCHAR(5),
    cvv           VARCHAR(3),
    role_id       INTEGER NOT NULL REFERENCES role(role_id) ON DELETE RESTRICT,
    partner_id    INTEGER REFERENCES partner(partner_id) ON DELETE SET NULL
);

-- Типы номеров
CREATE TABLE room_type (
    type_id    SERIAL PRIMARY KEY,
    type_name  VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    capacity   INTEGER NOT NULL,
    base_price NUMERIC(12,2) NOT NULL  -- BYN
);

-- Номера
CREATE TABLE room (
    room_id SERIAL PRIMARY KEY,
    number  VARCHAR(10) NOT NULL UNIQUE,
    floor   INTEGER NOT NULL,
    status  VARCHAR(20) NOT NULL,
    type_id INTEGER NOT NULL REFERENCES room_type(type_id) ON DELETE RESTRICT
);

-- Изображения номеров
CREATE TABLE image (
    image_id  SERIAL PRIMARY KEY,
    room_id   INTEGER NOT NULL REFERENCES room(room_id) ON DELETE CASCADE,
    file_path VARCHAR(255) NOT NULL,
    description VARCHAR(255)
);

-- Бронирования
CREATE TABLE booking (
    booking_id SERIAL PRIMARY KEY,
    user_id    INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    room_id    INTEGER NOT NULL REFERENCES room(room_id) ON DELETE RESTRICT,
    partner_id INTEGER REFERENCES partner(partner_id) ON DELETE SET NULL,
    start_date DATE NOT NULL,
    end_date   DATE NOT NULL,
    status     VARCHAR(20) NOT NULL,  -- 'NEW','CONFIRMED','CANCELLED'
    total_cost NUMERIC(12,2) NOT NULL  -- BYN
);

-- Оплаты
CREATE TABLE payment (
    payment_id  SERIAL PRIMARY KEY,
    booking_id  INTEGER NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,
    amount      NUMERIC(12,2) NOT NULL,
    method      VARCHAR(20) NOT NULL,  -- 'CASH','CARD','ONLINE'
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20) NOT NULL   -- 'PAID','FAILED'
);

-- Услуги
CREATE TABLE service (
    service_id   SERIAL PRIMARY KEY,
    service_name VARCHAR(100) NOT NULL,
    description  TEXT,
    price        NUMERIC(12,2) NOT NULL  -- BYN
);

-- Заказы услуг
CREATE TABLE service_order (
    service_order_id SERIAL PRIMARY KEY,
    booking_id       INTEGER NOT NULL REFERENCES booking(booking_id) ON DELETE CASCADE,
    service_id       INTEGER NOT NULL REFERENCES service(service_id) ON DELETE RESTRICT,
    quantity         INTEGER NOT NULL,
    price            NUMERIC(12,2) NOT NULL,  -- BYN за единицу * quantity
    order_date       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Отзывы
CREATE TABLE review (
    review_id   SERIAL PRIMARY KEY,
    user_id     INTEGER NOT NULL REFERENCES "user"(user_id) ON DELETE CASCADE,
    room_id     INTEGER NOT NULL REFERENCES room(room_id) ON DELETE CASCADE,
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    review_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Лог действий
CREATE TABLE audit_log (
    log_id    SERIAL PRIMARY KEY,
    user_id   INTEGER REFERENCES "user"(user_id) ON DELETE SET NULL,
    action    VARCHAR(100) NOT NULL,
    details   TEXT,
    log_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Отчёты о прибыли
CREATE TABLE profit_report (
    report_id    SERIAL PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end   DATE NOT NULL,
    total_income NUMERIC(14,2) NOT NULL,
    total_expense NUMERIC(14,2) NOT NULL,
    profit       NUMERIC(14,2) NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Индексы на часто запрашиваемые поля
CREATE INDEX idx_booking_user   ON booking(user_id);
CREATE INDEX idx_booking_room   ON booking(room_id);
CREATE INDEX idx_payment_booking ON payment(booking_id);
CREATE INDEX idx_review_room    ON review(room_id);

-- --------------------------------------------------
-- Заполняем таблицы начальными данными (пример)
-- --------------------------------------------------

-- Роли
INSERT INTO role(role_name) VALUES
 ('ADMIN'),
 ('EMPLOYEE'),
 ('GUEST');

-- Партнёры
INSERT INTO partner(name, contact_info) VALUES
 ('Турфирма «БелТур»', 'тел:+375291234567, office@beltur.by'),
 ('Компания «Отдых+»', 'тел:+375445678901, info@otdyh-plus.by');

-- Типы номеров (цены в BYN)
INSERT INTO room_type(type_name, description, capacity, base_price) VALUES
 ('Одноместный', 'Уютный номер с одной кроватью', 1, 50.00),
 ('Двухместный', 'Номер на двоих с двумя кроватями', 2, 80.00),
 ('Полулюкс',    'Раскладывающийся диван, максимум 3 человека', 3, 120.00),
 ('Люкс',        'Просторный номер с гостиной зоной', 4, 200.00);

-- Номера
INSERT INTO room(number, floor, status, type_id) VALUES
 ('101', 1, 'AVAILABLE', 1),
 ('102', 1, 'MAINTENANCE', 1),
 ('201', 2, 'AVAILABLE', 2),
 ('202', 2, 'AVAILABLE', 2),
 ('301', 3, 'AVAILABLE', 3),
 ('302', 3, 'AVAILABLE', 3),
 ('401', 4, 'AVAILABLE', 4),
 ('402', 4, 'AVAILABLE', 4);

-- Изображения номеров (пути условные)
INSERT INTO image(room_id, file_path, description) VALUES
 (101, 'images/room101_1.jpg', 'Вид на кровать'),
 (101, 'images/room101_2.jpg', 'Рабочий стол'),
 (201, 'images/room201_1.jpg', 'Две кровати'),
 (401, 'images/room401_1.jpg', 'Люкс с диваном');

-- Пользователи (пароль хранится plain — для примера; лучше хешировать)
INSERT INTO "user"(username, password_hash, email, phone, full_name, card_number, card_expiry, cvv, role_id, partner_id) VALUES
 ('admin', 'adminpass', 'admin@hotel.by', '+375291000001', 'Иван Иванов', '4000123412341234', '12/25', '123', 1, NULL),
 ('director', 'dirpass', 'director@hotel.by', '+375291000002', 'Мария Петрова', '4000567812345678', '11/24', '234', 2, NULL),
 ('guest1', 'guestpass', 'guest1@mail.ru', '+375291000003', 'Ольга Смирнова', '4000987612340987', '10/23', '345', 3, NULL),
 ('beltourist', 'tour123', 'tourist@beltur.by', '+375292000004', 'Алексей Козлов', '4000222233334444', '09/24', '456', 3, 1);

-- Услуги
INSERT INTO service(service_name, description, price) VALUES
 ('Завтрак', 'Шведский стол с завтраком', 12.50),
 ('Трансфер', 'Трансфер из аэропорта', 40.00),
 ('SPA', 'Посещение SPA-центра (1 час)', 30.00),
 ('Прачечная', 'Стирка и глажка до 5 кг', 15.00);

-- Пример бронирования
INSERT INTO booking(user_id, room_id, partner_id, start_date, end_date, status, total_cost) VALUES
 (3, 201, NULL, '2025-06-01', '2025-06-05', 'CONFIRMED', 4 * 80.00),
 (4, 401, 1,    '2025-07-10', '2025-07-15', 'NEW',       5 * 200.00);

-- Оплаты к бронированиям
INSERT INTO payment(booking_id, amount, method, status) VALUES
 (1, 4 * 80.00, 'CARD', 'PAID'),
 (2, 5 * 200.00,'CASH', 'PENDING');

-- Заказы услуг
INSERT INTO service_order(booking_id, service_id, quantity, price) VALUES
 (1, 1, 4, 4 * 12.50),
 (1, 3, 2, 2 * 30.00),
 (2, 2, 1, 1 * 40.00);

-- Отзывы
INSERT INTO review(user_id, room_id, rating, comment) VALUES
 (3, 201, 5, 'Отличный номер, чисто и удобно!'),
 (4, 401, 4, 'Очень понравился люкс, но дороговато.');

-- Лог действий (пример)
INSERT INTO audit_log(user_id, action, details) VALUES
 (1, 'LOGIN', 'Администратор вошёл в систему'),
 (3, 'BOOK_ROOM', 'guest1 забронировал номер 201 на 4 ночи');

-- Отчёт о прибыли
INSERT INTO profit_report(period_start, period_end, total_income, total_expense, profit) VALUES
 ('2025-06-01','2025-06-30', 5000.00, 2000.00, 3000.00);
