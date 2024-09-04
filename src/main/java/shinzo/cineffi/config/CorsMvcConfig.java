package shinzo.cineffi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        corsRegistry.addMapping("/**")
                .allowedOrigins("https://k7f10638b4382a.user-app.krampoline.com/") // 프론트 주소
                .allowedOrigins("http://cineffi-2.s3-website.ap-northeast-2.amazonaws.com/") // S3 주소
                .allowedOrigins("https://cineffi-2.s3-website.ap-northeast-2.amazonaws.com/") // S3 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
