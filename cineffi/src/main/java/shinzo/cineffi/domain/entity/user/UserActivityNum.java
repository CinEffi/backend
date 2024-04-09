package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivityNum {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserCore userCore;


    @Builder.Default
    private int collectionNum = 0;

    @Builder.Default
    private int scrapNum = 0;

    @Builder.Default
    private int followingsNum = 0;

    @Builder.Default
    private int followersNum = 0;


}
