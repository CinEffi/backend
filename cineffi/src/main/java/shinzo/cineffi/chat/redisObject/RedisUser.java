package shinzo.cineffi.chat.redisObject;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RedisUser {
    private String nickname;
    private boolean isBad;
    private boolean isCertified;
}
