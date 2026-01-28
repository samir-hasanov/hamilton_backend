package www.hamilton.com.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

@Getter
@Builder
public class CompanyImportResponse {

    private final int totalRows;
    private final int createdCount;
    private final int updatedCount;
    private final int skippedRows;
    private final String sheetName;
    private final String importStatus;

    @Singular("error")
    private final List<String> errors;
    
    // Helper methods for better response handling
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    public int getTotalProcessed() {
        return createdCount + updatedCount;
    }
    
    public String getSummary() {
        return String.format("Cəmi: %d, Yaradıldı: %d, Yeniləndi: %d, Skip edildi: %d", 
                totalRows, createdCount, updatedCount, skippedRows);
    }
}


