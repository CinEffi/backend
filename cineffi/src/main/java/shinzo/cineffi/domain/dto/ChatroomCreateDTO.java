package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class ChatroomCreateDTO {
    private String chatroomName;
    private List<String> chatroomTag;
    private Boolean chatroomType;
}