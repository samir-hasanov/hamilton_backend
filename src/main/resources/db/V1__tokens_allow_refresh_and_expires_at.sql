-- Token idarəetmə standartı: Refresh token DB-də saxlanılır (REFRESH tipi, expires_at).
-- Bir dəfə işə salın: PostgreSQL-də hamilton schema və tokens cədvəli üçün.

-- Constraint-i yenilə: REFRESH tipinə icazə ver
ALTER TABLE hamilton.tokens DROP CONSTRAINT IF EXISTS tokens_token_type_check;
ALTER TABLE hamilton.tokens ADD CONSTRAINT tokens_token_type_check
    CHECK (token_type IN ('BEARER', 'REFRESH'));

-- expires_at sütunu (Hibernate ddl-auto=update da əlavə edə bilər)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'hamilton' AND table_name = 'tokens' AND column_name = 'expires_at'
    ) THEN
        ALTER TABLE hamilton.tokens ADD COLUMN expires_at TIMESTAMP WITH TIME ZONE;
    END IF;
END $$;
