package www.hamilton.com.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
public class TaskCategoryResponse {

    private Long id;
    private String name;
    private String description;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    private Long taskCount;
    private Long activeTaskCount;
    private Long completedTaskCount;
}
