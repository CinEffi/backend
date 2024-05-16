package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
@Getter
@Builder
public class ChatroomListDTO {
    List<ChatroomDTO> list;
    Integer count;
    Boolean isOpen;
    // + 필요한 ETC
}
