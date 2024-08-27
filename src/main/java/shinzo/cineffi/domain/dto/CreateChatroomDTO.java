package shinzo.cineffi.domain.dto;

import lombok.Getter;

import java.util.List;
@Getter
public class CreateChatroomDTO {
    String title;
    List<String> tags;
}
