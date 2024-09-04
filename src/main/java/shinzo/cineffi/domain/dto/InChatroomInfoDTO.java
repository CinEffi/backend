package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InChatroomInfoDTO {
    @JsonProperty
    Long chatroomId;
    @JsonProperty
    ChatroomBriefDTO chatroomBriefDTO;
    @JsonProperty
    List<JoinedChatUserDTO> joinedChatUserDTOList;
    @JsonProperty
    List<ChatLogDTO> chatLogDTOList;
}
