package shinzo.cineffi.domain.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ChatLogDTO {
    private String nickname;
    private String content;
    private String timestamp;
    private boolean isMine;
}