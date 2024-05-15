package shinzo.cineffi.domain.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class ChatroomListDTO {
    List<ChatroomDTO> list;
    Integer count;
    Boolean isOpen;
    // + 필요한 ETC
}
