package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserCore userCore;



    @Builder.Default
    private int level = 1;

    private String name;

    //@Builder.Default
    @Lob
    private byte[] profileImage;

    @Builder.Default
    private boolean isBad = false;

    @Builder.Default
    private boolean isCertified = false;

    @Builder.Default
    private int exp = 0;


}
