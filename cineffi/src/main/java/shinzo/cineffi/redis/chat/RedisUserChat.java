package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

@Builder
@Getter
@Setter
public class RedisUserChat {
    private Long userId;
    private Long chatroomId;
    private String nickname;
    private RedisUserChatStatus redisUserChatStatus;
    private RedisUserChatRole redisUserChatRole;

    @JsonCreator
    public RedisUserChat(
            @JsonProperty("userId") Long userId,
            @JsonProperty("chatroomId") Long chatroomId,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("redisUserChatStatus") RedisUserChatStatus status,
            @JsonProperty("redisUserChatRole") RedisUserChatRole role
    ) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.nickname = nickname;
        this.redisUserChatStatus = status;
        this.redisUserChatRole = role;
    }

}
