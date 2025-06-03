-- Проверяем, существует ли колонка booking_id в таблице review
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name='review' AND column_name='booking_id'
    ) THEN
        -- Добавляем колонку booking_id, если она не существует
        ALTER TABLE review ADD COLUMN booking_id INTEGER REFERENCES booking(booking_id) ON DELETE SET NULL;
        
        -- Логируем изменение схемы
        RAISE NOTICE 'Колонка booking_id добавлена в таблицу review';
    ELSE
        RAISE NOTICE 'Колонка booking_id уже существует в таблице review';
    END IF;
END $$; 