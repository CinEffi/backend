package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class RedisUserChat {
    private Long userId;
    private Long chatroomId;
    private String nickname;
    private RedisUserChatStatus redisUserChatStatus;
    private RedisUserChatRole redisUserChatRole;
    private Boolean isMuted;
    @JsonCreator
    public RedisUserChat(
            @JsonProperty("userId") Long userId,
            @JsonProperty("chatroomId") Long chatroomId,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("redisUserChatStatus") RedisUserChatStatus status,
            @JsonProperty("redisUserChatRole") RedisUserChatRole role,
            @JsonProperty("isMuted") Boolean isMuted
            ) {
        this.userId = userId;
        this.chatroomId = chatroomId;
        this.nickname = nickname;
        this.redisUserChatStatus = status;
        this.redisUserChatRole = role;
        this.isMuted = isMuted;
    }

}
