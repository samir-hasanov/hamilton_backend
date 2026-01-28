package www.hamilton.com.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import www.hamilton.com.serializer.InstantSerializer;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateCompanyRequest {

    @NotBlank(message = "Şirkət adı məcburidir")
    @Size(max = 200, message = "Şirkət adı 200 simvoldan çox ola bilməz")
    private String name;


    @Size(max = 50, message = "Vergi nömrəsi 50 simvoldan çox ola bilməz")
    private String taxNumber;


    private String accountant;

    private String asanId;

    private String pins;

    private String statisticalCode;

    private String column2;

    private String taxType; // Sadə/ƏDV

    private Instant lastCheckDate;

    private String status; // OK/Not OK

    private String complianceDate; // Uyğunsuzluq gəlmə tarixi

    private String notes; // Qeyd

    private String bank;

    private String column1;

    private String bankCurator;

    private String otherNumbers; // Şirkətlə əlaqəli digər nömrələr

    private String cashStatus; // Kassa (Bəli/Xeyr)

    private String ygbStatus; // YGB (Bəli/Xeyr)

    private String certificateDate; // ASAN nömrə sertifikat

    private String notes2; // Qeyd2

    private String activityCodes; // Fəaliyyət kodları


}
