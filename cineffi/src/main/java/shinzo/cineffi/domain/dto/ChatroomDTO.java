package shinzo.cineffi.domain.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public class ChatroomDTO {
    String title;
    List<String> tags;
    LocalDateTime createdAt;
    LocalDateTime closedAt;
    Integer userCount;
}