package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatroomBriefDTO {
    @JsonProperty
    String title;
    @JsonProperty
    List<String> tags;
    @JsonProperty
    String closedAT;
}
