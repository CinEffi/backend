package shinzo.cineffi.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor
public class JWToken {

    private String accessToken;

    private String refreshToken;
}
