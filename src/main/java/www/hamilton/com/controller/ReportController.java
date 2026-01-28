package www.hamilton.com.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import www.hamilton.com.dto.response.DashboardStatsResponse;
import www.hamilton.com.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Hesabat və statistikalar API-ləri")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "İstifadəçi performans statistikası", description = "Bütün işçilərin performans statistikasını almaq (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user-performance")
    public ResponseEntity<List<DashboardStatsResponse.PerformanceData>> getUserPerformance() {
        return ResponseEntity.ok(reportService.getUserPerformance());
    }
}


