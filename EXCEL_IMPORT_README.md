# 🚀 Hamilton Excel Import Funksionallığı (Təkmilləşdirilmiş)

## 📋 Təsvir
Bu funksionallıq Excel faylından şirkət məlumatlarını Hamilton sisteminə avtomatik import etmək üçün yaradılıb. Excel-dəki bütün sütunlar sistemə əlavə olunub və cədvəldə göstərilir. **YENİ**: Row count, daha yaxşı error handling və import tracking əlavə edilib.

## 🎯 Dəstəklənən Excel Formatı
**Fayl adı**: `Kargüzarlıq.xlsx`  
**Sheet**: `HAMILTON` (ikinci sheet, əgər varsa)

### 📊 Sütun Xəritəsi
| Excel Sütunu | Sütun Adı | Təsvir | Məcburi |
|--------------|-----------|---------|---------|
| A | VOEN | Vergi nömrəsi | ✅ |
| B | Müştərilər | Şirkət adı | ✅ |
| C | Mühasib | Mühasib adı | ❌ |
| D | ASAN/ID | ASAN identifikatoru | ❌ |
| E | PİNLƏR | PIN kodları | ❌ |
| F | Statistika Kodu | Statistika kodu | ❌ |
| G | Column2 | Əlavə məlumat | ❌ |
| H | Sadə/ƏDV | Vergi növü | ❌ |
| I | Sonuncu Yoxlanış Tarixi | Son yoxlanış tarixi | ❌ |
| J | OK/Not OK | Status | ❌ |
| K | Uyğunsuzluq gəlmə tarixi | Uyğunsuzluq tarixi | ❌ |
| L | Qeyd | Əlavə qeydlər | ❌ |
| M | Bank | Bank adı | ❌ |
| N | Column1 | Əlavə məlumat | ❌ |
| O | Bank Kuratoru | Bank kuratoru | ❌ |
| P | Şirkətlə əlaqəli digər nömrələr | Digər nömrələr | ❌ |
| Q | Kassa (Bəli/Xeyr) | Kassa statusu | ❌ |
| R | YGB (Bəli/Xeyr) | YGB statusu | ❌ |
| S | ASAN nömrə sertifikat | Sertifikat tarixi | ❌ |
| T | Qeyd2 | İkinci qeyd | ❌ |
| U | Fəaliyyət kodları | Fəaliyyət kodları | ❌ |

## 🆕 Yeni Xüsusiyyətlər

### 1. **Row Count Display**
- Cədvəlin sol tərəfində sıra nömrələri (№)
- Üst hissədə ümumi şirkət sayı statistikası
- Import nəticəsində dəqiq sətir sayı

### 2. **Təkmilləşdirilmiş Import**
- Boş sətirlər avtomatik skip edilir
- Daha yaxşı error handling
- Detailed logging və tracking
- Import status monitoring

### 3. **Enhanced Error Reporting**
- Hər sətir üçün ayrı xəta mesajı
- Console-da ətraflı xəta log-ları
- Import summary toast mesajları

## 🛠️ Quraşdırma

### 1. Database Migration
```bash
# PostgreSQL-ə qoşulun
psql -h localhost -U postgres -d hamilton_manager

# Migration script-i işə salın
\i database_migration.sql
```

### 2. Backend Build
```bash
cd hamilton_backend
./gradlew clean build
```

### 3. Frontend Update
Frontend avtomatik olaraq yenilənib. Əlavə dəyişiklik tələb olunmur.

## 📱 İstifadə

### 1. Admin kimi Login Olun
- Sistemə admin rolunda daxil olun
- Şirkətlər səhifəsinə keçin

### 2. Excel Import
- "Excel-dən Import" düyməsinə basın
- `Kargüzarlıq.xlsx` faylını seçin
- Import nəticəsini gözləyin

### 3. Nəticəni Yoxlayın
- **Row Count**: Cədvəlin sol tərəfində sıra nömrələri
- **Statistics**: Üst hissədə ümumi şirkət sayı
- **Import Summary**: Detailed toast mesajı
- **Error Logs**: Console-da xəta detalları

## 🔧 Texniki Detallar

### Backend Endpoint
```
POST /api/v1/companies/import
Content-Type: multipart/form-data
Authorization: Bearer {JWT_TOKEN}
```

### Enhanced Response Format
```json
{
  "totalRows": 78,
  "createdCount": 45,
  "updatedCount": 33,
  "skippedRows": 0,
  "sheetName": "HAMILTON",
  "importStatus": "SUCCESS",
  "errors": []
}
```

### Import Process
1. **Header Detection**: Birinci sətir avtomatik skip edilir
2. **Empty Row Filtering**: Boş sətirlər avtomatik skip edilir
3. **Data Validation**: Şirkət adı məcburidir
4. **Duplicate Handling**: VOEN və ya ada görə mövcud şirkətlər update olunur
5. **Error Logging**: Hər xəta detailed log ilə qeyd edilir

## ⚠️ Diqqət Ediləsi Məqamlar

### 1. Excel Formatı
- Fayl `.xlsx` və ya `.xls` formatında olmalıdır
- Birinci sətir header sayılır və skip edilir
- Boş sətirlər avtomatik skip edilir
- Şirkət adı (B sütunu) məcburidir

### 2. Tarix Formatı
- Tarixlər `DD/MM/YYYY` formatında olmalıdır
- Yanlış format olarsa, tarix `null` olaraq saxlanılır
- Tarix parsing xətaları log edilir

### 3. Data Quality
- Bütün məlumatlar `trim()` edilir
- Null və empty string-lər düzgün handle edilir
- Import nəticəsində data validation

## 🚨 Xəta Həlli

### Import Xətaları
- **Console Logs**: Detailed xəta mesajları
- **Toast Messages**: User-friendly xəta bildirişləri
- **Error Summary**: Xəta sayı və növü
- **Row Tracking**: Hansı sətirdə xəta olduğu

### Database Xətaları
- Migration script-i `IF NOT EXISTS` istifadə edir
- Mövcud məlumatlar qorunur
- Yeni sütunlar avtomatik əlavə olunur
- Transaction rollback əgər xəta baş verərsə

## 📈 Performans

### Optimizasiyalar
- Database indexlər yaradılıb
- Batch processing istifadə olunur
- Transaction management
- Empty row filtering
- Smart duplicate detection

### Məhdudiyyətlər
- Fayl ölçüsü: 10MB
- Sətir sayı: limitsiz (78+ şirkət dəstəklənir)
- Import vaxtı: fayl ölçüsündən asılı
- Memory usage: Optimized

## 🔄 Gələcək Təkmilləşdirmələr

### Planlaşdırılan
- [x] Excel import functionality
- [x] Row count display
- [x] Enhanced error handling
- [x] Import statistics
- [ ] Excel template download
- [ ] Import validation rules
- [ ] Bulk export functionality
- [ ] Import history tracking

### Təklif Edilən
- [ ] Real-time import progress bar
- [ ] Import scheduling
- [ ] Data validation rules
- [ ] Error correction suggestions
- [ ] Import preview before execution

## 📊 Import Nəticəsi Nümunəsi

```
✅ Import Uğurlu!

📊 Sheet: HAMILTON
📝 Cəmi sətir: 78
✅ Yaradıldı: 45
🔄 Yeniləndi: 33
⏭️ Skip edildi: 0
📈 Ümumi işlənən: 78
```

## 📞 Dəstək

Əgər problem yaşayırsınızsa:
1. **Console Logs**: Backend və frontend log-larını yoxlayın
2. **Database Connection**: PostgreSQL connection-ı yoxlayın
3. **Excel Format**: Sütun sırası və format düzgün olmalıdır
4. **Admin Role**: Admin rolunda olduğunuzu təsdiqləyin
5. **File Size**: Fayl 10MB-dan kiçik olmalıdır

---

**Versiya**: 2.0.0  
**Son yeniləmə**: 2024-08-11  
**Developer**: Hamilton Team  
**Status**: Production Ready ✅

