package shinzo.cineffi.domain.dto;

import lombok.Getter;

@Getter
public class SendChatMessageDTO {
    private Long chatroomId;
    private String message;
}
