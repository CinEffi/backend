package shinzo.cineffi.redis.chat.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateRedisChatroomDTO {
    private String title;
    private Long ownerId;
    private String ownerNickname;
}