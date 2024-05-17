package shinzo.cineffi.domain.dto;

import lombok.Builder;

@Builder
public class JoinedChatUserDTO {
    private String nickname;
    private Integer level;
    private String userId;
    private Boolean isBad;
    private Boolean isCertified;
}