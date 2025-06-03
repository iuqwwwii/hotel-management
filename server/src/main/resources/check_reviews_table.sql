-- Проверка структуры таблицы review
SELECT 
    column_name, 
    data_type, 
    is_nullable
FROM 
    information_schema.columns
WHERE 
    table_name = 'review'
ORDER BY 
    ordinal_position;

-- Проверка ограничений таблицы review
SELECT 
    conname AS constraint_name,
    contype AS constraint_type,
    pg_get_constraintdef(pg_constraint.oid) AS constraint_definition
FROM 
    pg_constraint
JOIN 
    pg_class ON pg_constraint.conrelid = pg_class.oid
WHERE 
    pg_class.relname = 'review';

-- Проверка существующих отзывов
SELECT 
    r.review_id,
    r.user_id,
    u.username,
    r.room_id,
    room.number AS room_number,
    r.booking_id,
    r.rating,
    r.comment,
    r.review_date
FROM 
    review r
JOIN 
    "user" u ON r.user_id = u.user_id
JOIN 
    room ON r.room_id = room.room_id
ORDER BY 
    r.review_date DESC;

-- Проверка отзывов для пользователя с ID 3
SELECT 
    r.review_id,
    r.user_id,
    u.username,
    r.room_id,
    room.number AS room_number,
    r.booking_id,
    r.rating,
    r.comment,
    r.review_date
FROM 
    review r
JOIN 
    "user" u ON r.user_id = u.user_id
JOIN 
    room ON r.room_id = room.room_id
WHERE 
    r.user_id = 3
ORDER BY 
    r.review_date DESC;

-- Проверка бронирований для пользователя с ID 3
SELECT 
    b.booking_id,
    b.user_id,
    u.username,
    b.room_id,
    r.number AS room_number,
    b.start_date,
    b.end_date,
    b.status
FROM 
    booking b
JOIN 
    "user" u ON b.user_id = u.user_id
JOIN 
    room r ON b.room_id = r.room_id
WHERE 
    b.user_id = 3
ORDER BY 
    b.booking_id; 