-- 1) Добавляем новый простой столбец
ALTER TABLE tickets
    ADD COLUMN scheduled_at_min TIMESTAMPTZ;

-- 2) Заливаем текущее значение для уже существующих строк
UPDATE tickets
SET scheduled_at_min = date_trunc('minute', scheduled_at);

-- 3) Создаём функцию-триггер, которая будет поддерживать колонку в актуальном состоянии
CREATE OR REPLACE FUNCTION trg_set_scheduled_at_min()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.scheduled_at_min := date_trunc('minute', NEW.scheduled_at);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 4) Повешаем триггер на INSERT и UPDATE
CREATE TRIGGER tickets_set_scheduled_at_min
    BEFORE INSERT OR UPDATE ON tickets
    FOR EACH ROW
EXECUTE FUNCTION trg_set_scheduled_at_min();

-- 5) Наконец — уникальный индекс уже по немутируемому столбцу
CREATE UNIQUE INDEX ux_ticket_address_minute
    ON tickets(address, scheduled_at_min);