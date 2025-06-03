-- Выводим структуру таблицы review
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns 
WHERE table_name = 'review'
ORDER BY ordinal_position;

-- Проверяем количество отзывов в таблице
SELECT COUNT(*) AS review_count FROM review;

-- Проверяем внешние ключи для таблицы review
SELECT
    tc.constraint_name, 
    tc.table_name, 
    kcu.column_name, 
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM 
    information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
      AND tc.table_schema = kcu.table_schema
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
      AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' AND tc.table_name='review';

-- Выполняем запрос для проверки данных
SELECT r.review_id, r.user_id, r.room_id, r.booking_id, r.rating, LEFT(r.comment, 30) AS comment_preview, 
       r.review_date, u.username, room.number AS room_number
FROM review r
JOIN "user" u ON r.user_id = u.user_id
JOIN room ON r.room_id = room.room_id
ORDER BY r.review_date DESC; 