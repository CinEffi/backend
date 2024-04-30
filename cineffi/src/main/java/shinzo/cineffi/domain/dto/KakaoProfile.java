package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoProfile {
    public Long id;
    public String connectedAt;
    public KakaoAccount kakaoAccount;

    @Data
    @NoArgsConstructor
    @JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
    public class KakaoAccount{
        public Boolean nameNeedsAgreement;
        public String name;
        public Boolean hasEmail;
        public Boolean emailNeedsAgreement;
        public Boolean isEmailValid;
        public Boolean isEmailVerified;
        public String email;
        public Boolean hasPhoneNumber;
        public Boolean phoneNumberNeedsAgreement;
        public String phoneNumber;
    }
}
