package shinzo.cineffi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import shinzo.cineffi.Utils.EncryptUtil;

@Configuration
public class EncryptConfig {
    @Value("${app.encryption.key}")
    private String key;

    @Bean
    public EncryptUtil encryptUtil() {
        EncryptUtil.setKey(key);
        return new EncryptUtil();
    }
}
