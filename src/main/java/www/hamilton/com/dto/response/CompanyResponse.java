package www.hamilton.com.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import www.hamilton.com.serializer.InstantSerializer;

import java.time.Instant;

@Getter
@Builder
public class CompanyResponse {

    private Long id;
    private String name;
    private String taxNumber;
    private String accountant;
    private String asanId;
    private String pins;
    private String statisticalCode;
    private String column2;
    private String taxType;
    
    @JsonSerialize(using = InstantSerializer.class)
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

    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;
    
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;

    // Task statistics
    private Long taskCount;
    private Long activeTaskCount;
    private Long completedTaskCount;

    // Assignment and visibility
    private String assignedUsername;
    private Boolean isPublic;
}
