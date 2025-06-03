-- Проверка структуры таблицы отзывов
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default
FROM 
    information_schema.columns
WHERE 
    table_name = 'review'
ORDER BY 
    ordinal_position;

-- Проверка существующих отзывов
SELECT 
    r.review_id,
    r.user_id,
    u.username,
    r.room_id,
    room.room_number,
    r.booking_id,
    r.rating,
    r.comment,
    r.review_date
FROM 
    review r
LEFT JOIN 
    "user" u ON r.user_id = u.user_id
LEFT JOIN 
    room ON r.room_id = room.room_id
ORDER BY 
    r.review_date DESC;

-- Удаление дубликатов отзывов (если они есть)
-- Сначала создаем временную таблицу для определения дубликатов
CREATE TEMP TABLE review_duplicates AS
SELECT 
    MIN(review_id) as keep_id,
    booking_id,
    user_id
FROM 
    review
WHERE 
    booking_id IS NOT NULL
GROUP BY 
    booking_id, user_id
HAVING 
    COUNT(*) > 1;

-- Удаляем дубликаты (все записи кроме тех, которые мы решили оставить)
DELETE FROM review
WHERE 
    booking_id IN (SELECT booking_id FROM review_duplicates)
    AND user_id IN (SELECT user_id FROM review_duplicates)
    AND review_id NOT IN (SELECT keep_id FROM review_duplicates);

-- Проверяем отзывы для определенного пользователя
SELECT 
    r.review_id,
    r.user_id,
    u.username,
    r.room_id,
    room.room_number,
    r.booking_id,
    r.rating,
    r.comment,
    r.review_date
FROM 
    review r
LEFT JOIN 
    "user" u ON r.user_id = u.user_id
LEFT JOIN 
    room ON r.room_id = room.room_id
WHERE 
    r.user_id = 3
ORDER BY 
    r.review_date DESC;

-- Проверяем отзывы для определенного бронирования
SELECT 
    r.review_id,
    r.user_id,
    u.username,
    r.room_id,
    room.room_number,
    r.booking_id,
    r.rating,
    r.comment,
    r.review_date
FROM 
    review r
LEFT JOIN 
    "user" u ON r.user_id = u.user_id
LEFT JOIN 
    room ON r.room_id = room.room_id
WHERE 
    r.booking_id IN (4, 6, 10)
ORDER BY 
    r.booking_id, r.review_date DESC;

-- Добавляем столбец для уникального ограничения, если его нет
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.table_constraints 
        WHERE constraint_name = 'unique_booking_review' 
        AND table_name = 'review'
    ) THEN
        ALTER TABLE review ADD CONSTRAINT unique_booking_review UNIQUE (booking_id);
    END IF;
END
$$; 