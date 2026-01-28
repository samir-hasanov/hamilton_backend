package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import www.hamilton.com.dto.request.CreateCompanyRequest;
import www.hamilton.com.dto.request.UpdateCompanyRequest;
import www.hamilton.com.dto.response.CompanyResponse;
import www.hamilton.com.dto.response.CompanyImportResponse;
import www.hamilton.com.service.CompanyService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Şirkət idarəetmə API-ləri")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "Yeni şirkət yarat", description = "Yeni şirkət yaratmaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.ok(companyService.createCompany(request));
    }

    @Operation(summary = "Bütün şirkətləri al", description = "Sistemdəki bütün şirkətləri siyahıya almaq və ya VOEN-ə görə axtarmaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies(@RequestParam(value = "voen", required = false) String voen) {
        if (voen != null && !voen.isBlank()) {
            return ResponseEntity.ok(companyService.searchCompaniesByTaxNumber(voen));
        }
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @Operation(summary = "Mənim şirkətlərim", description = "Cari istifadəçiyə təyin edilmiş və ya ictimai şirkətləri siyahıya almaq")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    public ResponseEntity<List<CompanyResponse>> getMyCompanies() {
        String currentUsername = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();
        return ResponseEntity.ok(companyService.getMyCompanies(currentUsername));
    }

    @Operation(summary = "Şirkət məlumatlarını al", description = "ID-yə görə şirkət məlumatlarını almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @Operation(summary = "Şirkət məlumatlarını tam yenilə", description = "Şirkət məlumatlarını tam yeniləmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/full")
    public ResponseEntity<CompanyResponse> updateCompanyFull(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCompanyRequest request
    ) {
        return ResponseEntity.ok(companyService.updateCompanyFull(id, request));
    }

    @Operation(summary = "Şirkəti sil", description = "Şirkəti silmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Excel-dən şirkətləri import et", description = "Excel faylından şirkətləri import etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyImportResponse> importCompanies(@RequestParam("file") MultipartFile file) throws Exception {
        try (var is = file.getInputStream()) {
            return ResponseEntity.ok(companyService.importCompaniesFromExcel(is));
        }
    }

    @Operation(summary = "Tarix parsing testi", description = "Tarix parsing funksionallığını test etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/test-date-parsing")
    public ResponseEntity<String> testDateParsing() {
        String testDate = "05/08/2025";
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            java.time.LocalDate localDate = java.time.LocalDate.parse(testDate, formatter);
            java.time.Instant instant = localDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
            return ResponseEntity.ok("Test tarix parse edildi: " + testDate + " -> " + instant);
        } catch (Exception e) {
            return ResponseEntity.ok("Tarix parsing xətası: " + e.getMessage());
        }
    }

    @Operation(summary = "Son yoxlama tarixini yenilə", description = "Şirkətin son yoxlama tarixini yeniləmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/last-check-date")
    public ResponseEntity<CompanyResponse> updateLastCheckDate(
            @PathVariable Long id,
            @RequestParam String date
    ) {
        return ResponseEntity.ok(companyService.updateLastCheckDate(id, date));
    }

    @Operation(summary = "Son yoxlama tarixinə görə sıralanmış şirkətlər", description = "Son yoxlama tarixinə görə sıralanmış şirkətləri siyahıya almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sorted-by-last-check-date")
    public ResponseEntity<List<CompanyResponse>> getCompaniesSortedByLastCheckDate() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @Operation(summary = "Şirkəti istifadəçiyə təyin et", description = "Şirkəti müəyyən istifadəçiyə təyin etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/assign/{username}")
    public ResponseEntity<CompanyResponse> assignCompany(@PathVariable Long id, @PathVariable String username) {
        return ResponseEntity.ok(companyService.assignCompanyToUser(id, username));
    }

    @Operation(summary = "Şirkətdən istifadəçi təyinatını ləğv et", description = "Şirkətdən istifadəçi təyinatını ləğv etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/unassign")
    public ResponseEntity<CompanyResponse> unassignCompany(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.unassignCompany(id));
    }

    @Operation(summary = "Şirkəti ictimai/gizli et", description = "Şirkəti ictimai və ya gizli etmək (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/public")
    public ResponseEntity<CompanyResponse> setCompanyPublic(@PathVariable Long id, @RequestParam boolean value) {
        return ResponseEntity.ok(companyService.setCompanyPublic(id, value));
    }
}

