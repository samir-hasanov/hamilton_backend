package www.hamilton.com.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import www.hamilton.com.entity.Company;
import www.hamilton.com.entity.TaskCategory;
import www.hamilton.com.entity.TaskStatus;
import www.hamilton.com.entity.User;
import www.hamilton.com.serializer.InstantSerializer;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverdueTask {

    private Long id;

    private String title;

    private TaskStatus status;

    private String assignedUser;

    private String company;

    private Instant startedAt;

    private Instant dueDate;

}
