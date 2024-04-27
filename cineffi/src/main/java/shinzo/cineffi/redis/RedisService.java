package shinzo.cineffi.redis;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public interface RedisService {

    void setValues(String key, String value);                       // 값 등록 / 수정
    void setValues(String key, String value, Duration duration);    // 값 등록 / 수정
    String getValue(String key);                                    // 값 조회
    void deleteValue(String key);                                   // 값 삭제
}
