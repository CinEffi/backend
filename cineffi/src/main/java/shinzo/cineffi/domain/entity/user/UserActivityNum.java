package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
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
    private User user;

    @ColumnDefault("0")
    private Integer collectionNum;

    @ColumnDefault("0")
    private Integer scrapNum;

    @ColumnDefault("0")
    private Integer followingsNum;

    @ColumnDefault("0")
    private Integer followersNum;


}
