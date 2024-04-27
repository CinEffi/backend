package shinzo.cineffi.redis.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JoinRedisChatroomDTO {
    private Long userId; // userId는 JWT에서 가져올거라 사라져야합니다.
    private String nickname;
}