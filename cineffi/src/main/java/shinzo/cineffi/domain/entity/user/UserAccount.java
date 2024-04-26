package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import shinzo.cineffi.domain.enums.LoginType;

@Entity
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAccount {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    private String email;

    private String password;
    //JWT 토큰 저장
    private String userToken;
    //JWT 토큰 저장
    //로그인시 Refresh Token DB 저장
    private Boolean isAuthentication;
    public void changeToken(String token) {
        this.userToken = token;
    }
}
