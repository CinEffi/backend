package shinzo.cineffi.domain.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter@Setter@RequiredArgsConstructor
public class EmailRequestDTO {
    private final String Email;
    private final String password;
}
