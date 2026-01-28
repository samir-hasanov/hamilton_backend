package www.hamilton.com.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.*;
import www.hamilton.com.serializer.InstantSerializer;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lombard_companies", indexes = {
    @Index(name = "idx_companies_name", columnList = "name"),
    @Index(name = "idx_companies_created", columnList = "created_at"),
    @Index(name = "idx_companies_tax_number", columnList = "tax_number"),
    @Index(name = "idx_companies_asan_id", columnList = "asan_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "tax_number", length = 50)
    private String taxNumber;

    @Column(name = "accountant", length = 100)
    private String accountant;

    @Column(name = "asan_id", length = 100)
    private String asanId;

    @Column(name = "pins", length = 100)
    private String pins;

    @Column(name = "statistical_code", length = 100)
    private String statisticalCode;

    @Column(name = "column2", length = 100)
    private String column2;

    @Column(name = "tax_type", length = 50)
    private String taxType; // Sadə/ƏDV

    @Column(name = "last_check_date")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant lastCheckDate;

    @Column(name = "status", length = 50)
    private String status; // OK/Not OK

    @Column(name = "compliance_date", length = 200)
    private String complianceDate; // Uyğunsuzluq gəlmə tarixi

    @Column(name = "notes", length = 1000)
    private String notes; // Qeyd

    @Column(name = "bank", length = 100)
    private String bank;

    @Column(name = "column1", length = 50)
    private String column1;

    @Column(name = "bank_curator", length = 200)
    private String bankCurator;

    @Column(name = "other_numbers", length = 200)
    private String otherNumbers; // Şirkətlə əlaqəli digər nömrələr

    @Column(name = "cash_status", length = 50)
    private String cashStatus; // Kassa (Bəli/Xeyr)

    @Column(name = "ygb_status", length = 50)
    private String ygbStatus; // YGB (Bəli/Xeyr)

    @Column(name = "certificate_date", length = 50)
    private String certificateDate; // ASAN nömrə sertifikat

    @Column(name = "notes2", length = 500)
    private String notes2; // Qeyd2

    @Column(name = "activity_codes", length = 200)
    private String activityCodes; // Fəaliyyət kodları

    @Column(name = "created_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    @Column(name = "updated_at")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant updatedAt;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();

    // Assigned worker (user) responsible for this company
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    // If true, all workers can see this company
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
