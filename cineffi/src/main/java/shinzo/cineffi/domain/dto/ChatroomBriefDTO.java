package shinzo.cineffi.domain.dto;

import lombok.Builder;

import java.util.List;

@Builder
public class ChatroomBriefDTO {
    String title;
    List<String> tags;
    String closedAT;
}
