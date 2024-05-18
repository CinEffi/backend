package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonSerialize
@Builder
public class JoinedChatUserDTO {
    @JsonProperty
    private String nickname;
    @JsonProperty
    private Integer level;
    @JsonProperty
    private String userId;
    @JsonProperty
    private Boolean isBad;
    @JsonProperty
    private Boolean isCertified;
}