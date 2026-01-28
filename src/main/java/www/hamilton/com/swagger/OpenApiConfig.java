package www.hamilton.com.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public OpenApiConfig() {
        SpringDocUtils.getConfig().removeRequestWrapperToIgnore(java.util.Optional.class);
    }

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
        
        Components components = new Components()
                .addSecuritySchemes(securitySchemeName, securityScheme);
        
        Info info = new Info()
                .title("Hamilton Maliyyə Consulting API")
                .version("1.0")
                .description("Hamilton Maliyyə Consulting Backend API Sənədləşməsi");
        
        Server server = new Server()
                .url("http://localhost:8085")
                .description("Local Development Server");
        
        return new OpenAPI()
                .info(info)
                .components(components)
                .addServersItem(server);
    }
}
