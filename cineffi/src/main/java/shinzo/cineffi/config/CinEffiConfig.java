package shinzo.cineffi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import shinzo.cineffi.Utils.EncryptUtil;

@Configuration
public class CinEffiConfig {
    @Value("${app.encryption.key}")
    private String encryptionKey;

    @Bean
    public EncryptUtil encryptUtil() {
        EncryptUtil.setKey(encryptionKey);
        return new EncryptUtil();
    }

    public static Long chatroomDurationMinute = 1440L;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(10);
        return taskScheduler;
    }

}