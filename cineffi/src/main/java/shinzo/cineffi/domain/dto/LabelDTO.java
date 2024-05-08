package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LabelDTO {
    String label;
    String description;
}
