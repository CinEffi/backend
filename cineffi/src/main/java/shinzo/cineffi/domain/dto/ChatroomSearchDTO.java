package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatroomSearchDTO {
    private String chatroomName;
    private List<String> chatroomTag;
    private Boolean chatroomType;
    // 구현하는 사람이 잘 채워주세요
}