-- Проверяем, существует ли колонка description в таблице room
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT column_name 
        FROM information_schema.columns 
        WHERE table_name='room' AND column_name='description'
    ) THEN
        -- Добавляем колонку description, если она не существует
        ALTER TABLE room ADD COLUMN description TEXT;
    END IF;
END $$; 