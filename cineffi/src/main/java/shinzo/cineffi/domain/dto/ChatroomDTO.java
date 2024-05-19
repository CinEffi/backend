package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Getter;
import shinzo.cineffi.domain.entity.chat.Chatroom;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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