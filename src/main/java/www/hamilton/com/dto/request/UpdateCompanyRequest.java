package www.hamilton.com.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCompanyRequest {

    @NotBlank(message = "Şirkət adı məcburidir")
    @Size(max = 200, message = "Şirkət adı 200 simvoldan çox ola bilməz")
    private String name;

    @Size(max = 500, message = "Təsvir 500 simvoldan çox ola bilməz")
    private String description;

    @Size(max = 50, message = "Vergi nömrəsi 50 simvoldan çox ola bilməz")
    private String taxNumber;
    private String accountant;
    private String asanId;
    private String pins;
    private String statisticalCode;
    private String column2;
    private String taxType;
    private Instant lastCheckDate;
    private String status;
    private String complianceDate;
    private String notes;
    private String bank;
    private String column1;
    private String bankCurator;
    private String otherNumbers;
    private String cashStatus;
    private String ygbStatus;
    private String certificateDate;
    private String notes2;
    private String activityCodes;
}
