package shinzo.cineffi.redis;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RedisDto {
    private String key;
    private String value;
}

