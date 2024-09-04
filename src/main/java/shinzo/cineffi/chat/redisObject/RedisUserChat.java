package shinzo.cineffi.chat.redisObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import shinzo.cineffi.domain.enums.UserChatRole;
import shinzo.cineffi.domain.enums.UserChatStatus;

@Builder(toBuilder = true)
@Getter
@Setter

public class RedisUserChat {
    private Long userId; // 두개 다 필요없을 가능성 농후
    private Long chatroomId; // 두개 다 필요없을 가능성 농후
    private String nickname;
    private UserChatStatus redisUserChatStatus;
    private UserChatRole redisUserChatRole;
    private Boolean isMuted;
    @JsonCreator
    public RedisUserChat(
            @JsonProperty("userId") Long userId,
            @JsonProperty("chatroomId") Long chatroomId,
            @JsonProperty("nickname") String nickname,
            @JsonProperty("status") UserChatStatus status,
            @JsonProperty("role") UserChatRole role,
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
