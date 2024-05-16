package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class ChatroomDTO {
    Long chatroomId;
    String title;
    List<String> tags;
    String createdAt;
    String closedAt;
    Integer userCount;
}