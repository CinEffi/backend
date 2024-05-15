package shinzo.cineffi.chat.redisObject;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RedisUser {
//    private String nickname; redis에서는 nickname이 식별자인데, key로 담길것 같음
    private boolean isBad;
    private boolean isCertified;
    private Long id;
}
