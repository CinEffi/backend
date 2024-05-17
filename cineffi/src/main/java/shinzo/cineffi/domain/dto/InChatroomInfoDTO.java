package shinzo.cineffi.domain.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class InChatroomInfoDTO {
    ChatroomBriefDTO chatroomBriefDTO;
    List<JoinedChatUserDTO> joinedChatUserDTOList;
    List<ChatLogDTO> chatLogDTOList;
}
