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
    private Long userId;
    private String nickname;
    private Integer level;
    private byte[] profileImage;
    private Boolean isBad;
    private Boolean isCertified;
}
