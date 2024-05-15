package shinzo.cineffi.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatroomDTO {
    String title;
    List<String> tags;
    LocalDateTime createdAt;
    LocalDateTime closedAt;
    Integer userCount;
}