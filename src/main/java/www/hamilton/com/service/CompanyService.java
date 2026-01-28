package www.hamilton.com.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import www.hamilton.com.dto.request.CreateCompanyRequest;
import www.hamilton.com.dto.request.UpdateCompanyRequest;
import www.hamilton.com.dto.response.CompanyResponse;
import www.hamilton.com.dto.response.CompanyImportResponse;
import www.hamilton.com.entity.Company;
import www.hamilton.com.entity.User;
import www.hamilton.com.entity.TaskStatus;
import www.hamilton.com.exception.ResourceNotFoundException;
import www.hamilton.com.repository.CompanyRepository;
import www.hamilton.com.repository.TaskRepository;
import www.hamilton.com.repository.UserRepository;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CompanyResponse createCompany(CreateCompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new RuntimeException("Bu adla şirkət artıq mövcuddur");
        }

        if (request.getTaxNumber() != null && companyRepository.existsByTaxNumber(request.getTaxNumber())) {
            throw new RuntimeException("Bu vergi nömrəsi ilə şirkət artıq mövcuddur");
        }

        Company company = Company.builder()
                .name(request.getName())
                .taxNumber(request.getTaxNumber())
                .accountant(request.getAccountant())
                .asanId(request.getAsanId())
                .pins(request.getPins())
                .statisticalCode(request.getStatisticalCode())
                .column2(request.getColumn2())
                .taxType(request.getTaxType())
                .lastCheckDate(request.getLastCheckDate())
                .status(request.getStatus())
                .complianceDate(request.getComplianceDate())
                .notes(request.getNotes())
                .bank(request.getBank())
                .column1(request.getColumn1())
                .bankCurator(request.getBankCurator())
                .otherNumbers(request.getOtherNumbers())
                .cashStatus(request.getCashStatus())
                .ygbStatus(request.getYgbStatus())
                .certificateDate(request.getCertificateDate())
                .notes2(request.getNotes2())
                .activityCodes(request.getActivityCodes())
                .build();

        Company savedCompany = companyRepository.save(company);
        return mapToResponse(savedCompany);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAllOrderByLastCheckDate().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getMyCompanies(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + username));
        List<Company> assigned = companyRepository.findByAssignedUser(user);
        List<Company> publicOnes = companyRepository.findAllPublic();
        return java.util.stream.Stream.concat(assigned.stream(), publicOnes.stream())
                .distinct()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CompanyResponse assignCompanyToUser(Long companyId, String username) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + companyId));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("İstifadəçi tapılmadı: " + username));
        company.setAssignedUser(user);
        return mapToResponse(companyRepository.save(company));
    }

    public CompanyResponse unassignCompany(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + companyId));
        company.setAssignedUser(null);
        return mapToResponse(companyRepository.save(company));
    }

    public CompanyResponse setCompanyPublic(Long companyId, boolean isPublic) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + companyId));
        company.setIsPublic(isPublic);
        return mapToResponse(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> searchCompaniesByTaxNumber(String voen) {
        if (voen == null || voen.isBlank()) {
            return getAllCompanies();
        }
        return companyRepository.findByTaxNumberContainingOrderByLastCheckDate(voen).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + id));
        return mapToResponse(company);
    }


    public CompanyResponse updateCompanyFull(Long id, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + id));

        if (!company.getName().equals(request.getName()) && companyRepository.existsByName(request.getName())) {
            throw new RuntimeException("Bu adla şirkət artıq mövcuddur");
        }

        company.setName(request.getName());
        company.setTaxNumber(request.getTaxNumber());
        company.setAccountant(request.getAccountant());
        company.setAsanId(request.getAsanId());
        company.setPins(request.getPins());
        company.setStatisticalCode(request.getStatisticalCode());
        company.setColumn2(request.getColumn2());
        company.setTaxType(request.getTaxType());
        company.setLastCheckDate(request.getLastCheckDate());
        company.setStatus(request.getStatus());
        company.setComplianceDate(request.getComplianceDate());
        company.setNotes(request.getNotes());
        company.setBank(request.getBank());
        company.setColumn1(request.getColumn1());
        company.setBankCurator(request.getBankCurator());
        company.setOtherNumbers(request.getOtherNumbers());
        company.setCashStatus(request.getCashStatus());
        company.setYgbStatus(request.getYgbStatus());
        company.setCertificateDate(request.getCertificateDate());
        company.setNotes2(request.getNotes2());
        company.setActivityCodes(request.getActivityCodes());

        Company updatedCompany = companyRepository.save(company);
        log.info("Şirkət {} bütün məlumatları yeniləndi", company.getName());
        return mapToResponse(updatedCompany);
    }

    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Şirkət tapılmadı: " + id);
        }
        companyRepository.deleteById(id);
    }

    public CompanyResponse updateLastCheckDate(Long id, String dateStr) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Şirkət tapılmadı: " + id));

        try {
            // Tarixi parse et
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(dateStr, formatter);
            Instant lastCheckDate = localDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
            
            company.setLastCheckDate(lastCheckDate);
            Company updatedCompany = companyRepository.save(company);
            
            log.info("Şirkət {} üçün son yoxlanış tarixi yeniləndi: {} -> {}", 
                    company.getName(), dateStr, lastCheckDate);
            
            return mapToResponse(updatedCompany);
        } catch (Exception e) {
            log.error("Tarix yenilənərkən xəta: {}", e.getMessage());
            throw new RuntimeException("Tarix formatı düzgün deyil. Format: yyyy-MM-dd");
        }
    }

    public CompanyImportResponse importCompaniesFromExcel(InputStream inputStream) {
        int totalRows = 0;
        int created = 0;
        int updated = 0;
        int skippedRows = 0;
        List<String> errors = new ArrayList<>();
        String sheetName = "Unknown"; // Default sheet name

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            log.info("Excel faylı açıldı. Sheet sayı: {}", workbook.getNumberOfSheets());
            
            XSSFSheet sheet = workbook.getNumberOfSheets() > 1
                    ? workbook.getSheetAt(1) // HAMILTON sheet 2-ci şəkilə uyğun ola bilər
                    : workbook.getSheetAt(0);
            
            sheetName = sheet.getSheetName(); // Sheet adını saxla
            int totalPhysicalRows = sheet.getPhysicalNumberOfRows(); // Sətir sayını saxla
            log.info("Sheet seçildi: '{}'. Ümumi sətir sayı: {}", sheetName, totalPhysicalRows);

            boolean headerSkipped = false;
            int rowNumber = 0;
            
            for (Row row : sheet) {
                rowNumber++;
                
                // Skip header və boş sətirlər
                if (!headerSkipped) { 
                    headerSkipped = true; 
                    log.info("Header sətir skip edildi: {}", rowNumber);
                    continue; 
                }
                
                // Boş sətirləri skip et
                if (isEmptyRow(row)) {
                    log.debug("Boş sətir skip edildi: {}", rowNumber);
                    continue;
                }
                
                totalRows++;
                log.debug("Sətir {} işlənir: {}", rowNumber, totalRows);

                try {
                    // Excel sütunlarına uyğun mapping (A=0, B=1, C=2, ...)
                    String taxNumber = getString(row, 0);        // A: VOEN
                    String name = getString(row, 1);             // B: Müştərilər
                    String accountant = getString(row, 2);       // C: Mühasib
                    String asanId = getString(row, 3);           // D: ASAN/ID
                    String pins = getString(row, 4);             // E: PİNLƏR
                    String statisticalCode = getString(row, 5);  // F: Statistika Kodu
                    String column2 = getString(row, 6);          // G: Column2
                    String taxType = getString(row, 7);          // H: Sadə/ƏDV
                    String lastCheckDateStr = getString(row, 8); // I: Sonuncu Yoxlanış Tarixi
                    String status = getString(row, 9);           // J: OK/Not OK
                    
                    // Tarix parsing üçün ətraflı logging
                    if (lastCheckDateStr != null && !lastCheckDateStr.isBlank()) {
                        log.info("Sətir {}: Tarix parsing başladı - '{}'", rowNumber, lastCheckDateStr);
                    }
                    String complianceDate = getString(row, 10);  // K: Uyğunsuzluq gəlmə tarixi
                    String notes = getString(row, 11);           // L: Qeyd
                    String bank = getString(row, 12);            // M: Bank
                    String column1 = getString(row, 13);         // N: Column1
                    String bankCurator = getString(row, 14);     // O: Bank Kuratoru
                    String otherNumbers = getString(row, 15);    // P: Şirkətlə əlaqəli digər nömrələr
                    String cashStatus = getString(row, 16);      // Q: Kassa (Bəli/Xeyr)
                    String ygbStatus = getString(row, 17);       // R: YGB (Bəli/Xeyr)
                    String certificateDate = getString(row, 18); // S: ASAN nömrə sertifikat
                    String notes2 = getString(row, 19);          // T: Qeyd2
                    String activityCodes = getString(row, 20);   // U: Fəaliyyət kodları

                    // Validation
                    if (name == null || name.isBlank()) {
                        String error = String.format("Sətir %d: Şirkət adı boşdur", rowNumber);
                        errors.add(error);
                        log.warn(error);
                        skippedRows++;
                        continue;
                    }

                    // Tarix parsing - Excel-dən artıq düzgün formatda alırıq
                    Instant lastCheckDate = null;
                    if (lastCheckDateStr != null && !lastCheckDateStr.isBlank()) {
                        try {
                            // Excel-dən artıq dd/MM/yyyy formatında alırıq
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            LocalDate localDate = LocalDate.parse(lastCheckDateStr.trim(), formatter);
                            lastCheckDate = localDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
                            log.info("Sətir {}: Tarix uğurla parse edildi: {} -> {}", 
                                    rowNumber, lastCheckDateStr, lastCheckDate);
                        } catch (Exception e) {
                            log.warn("Sətir {}: Tarix parsing xətası: {} - {}", 
                                    rowNumber, lastCheckDateStr, e.getMessage());
                            lastCheckDate = null;
                        }
                    }

                    // Şirkəti tap və ya yarat
                    Company company = findExistingCompany(taxNumber, name);
                    
                    if (company == null) {
                        // Yeni şirkət yarat
                        company = Company.builder()
                                .name(name.trim())
                                .taxNumber(taxNumber != null ? taxNumber.trim() : null)
                                .accountant(accountant != null ? accountant.trim() : null)
                                .asanId(asanId != null ? asanId.trim() : null)
                                .pins(pins != null ? pins.trim() : null)
                                .statisticalCode(statisticalCode != null ? statisticalCode.trim() : null)
                                .column2(column2 != null ? column2.trim() : null)
                                .taxType(taxType != null ? taxType.trim() : null)
                                .lastCheckDate(lastCheckDate)
                                .status(status != null ? status.trim() : null)
                                .complianceDate(complianceDate != null ? complianceDate.trim() : null)
                                .notes(notes != null ? notes.trim() : null)
                                .bank(bank != null ? bank.trim() : null)
                                .column1(column1 != null ? column1.trim() : null)
                                .bankCurator(bankCurator != null ? bankCurator.trim() : null)
                                .otherNumbers(otherNumbers != null ? otherNumbers.trim() : null)
                                .cashStatus(cashStatus != null ? cashStatus.trim() : null)
                                .ygbStatus(ygbStatus != null ? ygbStatus.trim() : null)
                                .certificateDate(certificateDate != null ? certificateDate.trim() : null)
                                .notes2(notes2 != null ? notes2.trim() : null)
                                .activityCodes(activityCodes != null ? activityCodes.trim() : null)
                                .build();
                        
                        companyRepository.save(company);
                        created++;
                        log.debug("Yeni şirkət yaradıldı: {} (VOEN: {})", name, taxNumber);
                    } else {
                        // Mövcud şirkəti yenilə
                        company.setName(name.trim());
                        company.setTaxNumber(taxNumber != null ? taxNumber.trim() : null);
                        company.setAccountant(accountant != null ? accountant.trim() : null);
                        company.setAsanId(asanId != null ? asanId.trim() : null);
                        company.setPins(pins != null ? pins.trim() : null);
                        company.setStatisticalCode(statisticalCode != null ? statisticalCode.trim() : null);
                        company.setColumn2(column2 != null ? column2.trim() : null);
                        company.setTaxType(taxType != null ? taxType.trim() : null);
                        company.setLastCheckDate(lastCheckDate);
                        company.setStatus(status != null ? status.trim() : null);
                        company.setComplianceDate(complianceDate != null ? complianceDate.trim() : null);
                        company.setNotes(notes != null ? notes.trim() : null);
                        company.setBank(bank != null ? bank.trim() : null);
                        company.setColumn1(column1 != null ? column1.trim() : null);
                        company.setBankCurator(bankCurator != null ? bankCurator.trim() : null);
                        company.setOtherNumbers(otherNumbers != null ? otherNumbers.trim() : null);
                        company.setCashStatus(cashStatus != null ? cashStatus.trim() : null);
                        company.setYgbStatus(ygbStatus != null ? ygbStatus.trim() : null);
                        company.setCertificateDate(certificateDate != null ? certificateDate.trim() : null);
                        company.setNotes2(notes2 != null ? notes2.trim() : null);
                        company.setActivityCodes(activityCodes != null ? activityCodes.trim() : null);
                        
                        companyRepository.save(company);
                        updated++;
                        log.debug("Mövcud şirkət yeniləndi: {} (VOEN: {})", name, taxNumber);
                    }
                } catch (Exception ex) {
                    String error = String.format("Sətir %d: %s", rowNumber, ex.getMessage());
                    errors.add(error);
                    log.error("Sətir {} işlənərkən xəta: {}", rowNumber, ex.getMessage(), ex);
                    skippedRows++;
                }
            }
            
            log.info("Excel import tamamlandı. Cəmi: {}, Yaradıldı: {}, Yeniləndi: {}, Skip edildi: {}, Xətalar: {}", 
                    totalRows, created, updated, skippedRows, errors.size());
                    
        } catch (Exception e) {
            String error = "Fayl oxunarkən xəta: " + e.getMessage();
            errors.add(error);
            log.error("Excel import xətası", e);
        }

        return CompanyImportResponse.builder()
                .totalRows(totalRows)
                .createdCount(created)
                .updatedCount(updated)
                .skippedRows(skippedRows)
                .sheetName(sheetName)
                .importStatus(errors.isEmpty() ? "SUCCESS" : "PARTIAL_SUCCESS")
                .errors(errors)
                .build();
    }

    private Company findExistingCompany(String taxNumber, String name) {
        // Əvvəlcə vergi nömrəsinə görə yoxla
        if (taxNumber != null && !taxNumber.isBlank()) {
            Company company = companyRepository.findByTaxNumber(taxNumber).orElse(null);
            if (company != null) {
                log.debug("Şirkət vergi nömrəsinə görə tapıldı: {} (VOEN: {})", company.getName(), taxNumber);
                return company;
            }
        }
        
        // Sonra ada görə yoxla
        if (name != null && !name.isBlank()) {
            Company company = companyRepository.findByName(name).orElse(null);
            if (company != null) {
                log.debug("Şirkət ada görə tapıldı: {} (VOEN: {})", name, company.getTaxNumber());
                return company;
            }
        }
        
        return null;
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        
        for (int i = 0; i <= 20; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                String value = getString(row, i);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getString(Row row, int idx) {
        try {
            Cell cell = row.getCell(idx);
            if (cell == null) return null;
            
            // Cell type-ına görə düzgün oxuma
            switch (cell.getCellType()) {
                case STRING:
                    String stringVal = cell.getStringCellValue();
                    return stringVal != null ? stringVal.trim() : null;
                    
                case NUMERIC:
                    // Excel tarix serial nömrəsini yoxla
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        try {
                            java.util.Date date = cell.getDateCellValue();
                            java.time.LocalDate localDate = date.toInstant()
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .toLocalDate();
                            return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } catch (Exception dateEx) {
                            log.warn("Tarix cell-i parse edilə bilmədi: {}", dateEx.getMessage());
                            return null;
                        }
                    } else {
                        // Adi ədəd
                        double numericVal = cell.getNumericCellValue();
                        return String.valueOf((long) numericVal);
                    }
                    
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                    
                case FORMULA:
                    // Formula nəticəsini al
                    try {
                        switch (cell.getCachedFormulaResultType()) {
                            case STRING:
                                return cell.getStringCellValue();
                            case NUMERIC:
                                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                    java.util.Date date = cell.getDateCellValue();
                                    java.time.LocalDate localDate = date.toInstant()
                                            .atZone(java.time.ZoneId.systemDefault())
                                            .toLocalDate();
                                    return localDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                } else {
                                    return String.valueOf((long) cell.getNumericCellValue());
                                }
                            default:
                                return null;
                        }
                    } catch (Exception formulaEx) {
                        log.warn("Formula nəticəsi alınarkən xəta: {}", formulaEx.getMessage());
                        return null;
                    }
                    
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("Sətir {}, sütun {} oxunarkən xəta: {}", row.getRowNum(), idx, e.getMessage());
            return null;
        }
    }

    private CompanyResponse mapToResponse(Company company) {
        Long taskCount = taskRepository.countByCompanyId(company.getId());
        Long activeTaskCount = taskRepository.countByCompanyIdAndStatus(company.getId(), TaskStatus.ACTIVE);
        Long completedTaskCount = taskRepository.countByCompanyIdAndStatus(company.getId(), TaskStatus.COMPLETED);

        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .taxNumber(company.getTaxNumber())
                .accountant(company.getAccountant())
                .asanId(company.getAsanId())
                .pins(company.getPins())
                .statisticalCode(company.getStatisticalCode())
                .column2(company.getColumn2())
                .taxType(company.getTaxType())
                .lastCheckDate(company.getLastCheckDate())
                .status(company.getStatus())
                .complianceDate(company.getComplianceDate())
                .notes(company.getNotes())
                .bank(company.getBank())
                .column1(company.getColumn1())
                .bankCurator(company.getBankCurator())
                .otherNumbers(company.getOtherNumbers())
                .cashStatus(company.getCashStatus())
                .ygbStatus(company.getYgbStatus())
                .certificateDate(company.getCertificateDate())
                .notes2(company.getNotes2())
                .activityCodes(company.getActivityCodes())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .taskCount(taskCount)
                .activeTaskCount(activeTaskCount)
                .completedTaskCount(completedTaskCount)
                .assignedUsername(company.getAssignedUser() != null ? company.getAssignedUser().getUsername() : null)
                .isPublic(Boolean.TRUE.equals(company.getIsPublic()))
                .build();
    }
}
