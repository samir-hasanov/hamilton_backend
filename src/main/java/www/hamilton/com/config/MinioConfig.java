package www.hamilton.com.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(
            @Value("${hamilton.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${hamilton.minio.access-key:minioadmin}") String accessKey,
            @Value("${hamilton.minio.secret-key:minioadmin}") String secretKey
    ) {
        String ep = blankToDefault(endpoint, "http://localhost:9000");
        String ak = blankToDefault(accessKey, "minioadmin");
        String sk = blankToDefault(secretKey, "minioadmin");
        return MinioClient.builder()
                .endpoint(ep)
                .credentials(ak, sk)
                .build();
    }

    private static String blankToDefault(String value, String def) {
        if (value == null) {
            return def;
        }
        String t = value.trim();
        return t.isEmpty() ? def : t;
    }
}
