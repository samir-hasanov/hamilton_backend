package www.hamilton.com.dto.response;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record AvatarView(Resource resource, MediaType mediaType) {
}
