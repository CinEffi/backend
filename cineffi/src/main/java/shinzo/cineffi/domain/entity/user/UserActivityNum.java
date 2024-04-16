package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DialectOverride;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class UserActivityNum {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private UserCore userCore;

    @ColumnDefault("0")
    private int collectionNum;

    @ColumnDefault("0")
    private int scrapNum;

    @ColumnDefault("0")
    private int followingsNum;

    @ColumnDefault("0")
    private int followersNum;


}
