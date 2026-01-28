package www.hamilton.com.dto.response;

import lombok.*;

@Value
@Builder
public class AuthResponse {
     String accessToken;
     String refreshToken;
     UserInfoResponse user;
}
