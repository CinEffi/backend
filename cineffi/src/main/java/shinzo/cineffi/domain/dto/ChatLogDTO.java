package shinzo.cineffi.domain.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ChatLogDTO {
    @JsonProperty
    private String nickname;
    @JsonProperty
    private String content;
    @JsonProperty
    private String timestamp;
    @JsonProperty
    private boolean mine;
}