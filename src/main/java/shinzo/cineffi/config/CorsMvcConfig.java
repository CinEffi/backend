package shinzo.cineffi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry){
        corsRegistry.addMapping("/**")
                .allowedOrigins("https://d1za9u0b27ndib.cloudfront.net/")
                .allowedOrigins("http://d1za9u0b27ndib.cloudfront.net/")
                .allowedOrigins("https://cineffi-elb-994338199.ap-northeast-2.elb.amazonaws.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
