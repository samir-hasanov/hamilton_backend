-- İstifadəçi profili: görünən ad və avatar fayl adı (diskdə saxlanılır).
ALTER TABLE hamilton.lombard_users
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(200);

ALTER TABLE hamilton.lombard_users
    ADD COLUMN IF NOT EXISTS profile_image_file VARCHAR(255);
