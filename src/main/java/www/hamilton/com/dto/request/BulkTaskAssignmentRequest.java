package www.hamilton.com.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkTaskAssignmentRequest {

    @NotEmpty(message = "Şirkət ID-ləri məcburidir")
    private List<Long> companyIds;

    @NotNull(message = "Tapşırıq kateqoriyası məcburidir")
    private Long categoryId;

    @Size(max = 100, message = "İstifadəçi adı 100 simvoldan çox ola bilməz")
    private String assignedUsername;

    @Size(max = 200, message = "Tapşırıq başlığı 200 simvoldan çox ola bilməz")
    private String title;

    @Size(max = 1000, message = "Təsvir 1000 simvoldan çox ola bilməz")
    private String description;

    // Vaxt təyin etmə seçimləri
    private Long delayMinutes;  // Dəqiqə ilə gecikmə
    private Long delayHours;    // Saat ilə gecikmə
    private Long delayDays;     // Gün ilə gecikmə
    private Long delayWeeks;    // Həftə ilə gecikmə

    // Və ya dəqiq tarix
    private String executionDate; // yyyy-MM-dd HH:mm formatında
}
