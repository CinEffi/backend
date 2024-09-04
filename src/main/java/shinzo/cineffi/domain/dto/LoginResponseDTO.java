package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
public class LoginResponseDTO {
    private String userId;
    private String nickname;
    private Integer level;
    private String profileImage;
    private Boolean isBad;
    private Boolean isCertified;
    private Boolean isKakao;
}
