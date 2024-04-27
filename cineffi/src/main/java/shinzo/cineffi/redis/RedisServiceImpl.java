package shinzo.cineffi.redis;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * RedisService의 구현체
 *
 * @author : lee
 * @fileName : RedisServiceImpl
 * @since : 3/29/24
 */
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 값을 등록/수정합니다.
     *
     * @param {String} key : redis key
     * @param {String} value : redis value
     * @return {void}
     */
    @Override
    public void setValues(String key, String value) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value);
    }

    /**
     * Redis 값을 등록/수정합니다.
     *
     * @param {String}   key : redis key
     * @param {String}   value: redis value
     * @param {Duration} duration: redis 값 메모리 상의 유효시간.
     * @return {void}
     */
    @Override
    public void setValues(String key, String value, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value, duration);
    }

    /**
     * Redis 키를 기반으로 값을 조회합니다.
     *
     * @param {String} key : redis key
     * @return {String} redis value 값 반환 or 미 존재시 빈 값 반환
     */
    @Override
    public String getValue(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        if (values.get(key) == null) return "";
        return String.valueOf(values.get(key));
    }

    /**
     * Redis 키값을 기반으로 row 삭제합니다.
     *
     * @param key
     */
    @Override
    public void deleteValue(String key) {
        redisTemplate.delete(key);
    }
}