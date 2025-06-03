-- Проверяем существующее ограничение уникальности для booking_id
DO $$
DECLARE
    constraint_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'unique_booking_review'
    ) INTO constraint_exists;
    
    IF constraint_exists THEN
        RAISE NOTICE 'Найдено ограничение unique_booking_review, удаляем его';
        EXECUTE 'ALTER TABLE review DROP CONSTRAINT IF EXISTS unique_booking_review';
    ELSE
        RAISE NOTICE 'Ограничение unique_booking_review не найдено';
    END IF;
END $$;

-- Создаем правильное ограничение уникальности (user_id + booking_id)
DO $$
DECLARE
    constraint_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'unique_user_booking_review'
    ) INTO constraint_exists;
    
    IF NOT constraint_exists THEN
        RAISE NOTICE 'Создаем ограничение unique_user_booking_review';
        EXECUTE 'ALTER TABLE review ADD CONSTRAINT unique_user_booking_review UNIQUE (user_id, booking_id)';
    ELSE
        RAISE NOTICE 'Ограничение unique_user_booking_review уже существует';
    END IF;
END $$;

-- Удаляем существующие отзывы для бронирований 4, 6 и 10 (чтобы можно было добавить новые)
DELETE FROM review WHERE booking_id IN (4, 6, 10);

-- Проверяем, что отзывы удалены
SELECT * FROM review WHERE booking_id IN (4, 6, 10); 