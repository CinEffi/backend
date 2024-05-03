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
    private Integer collectionNum; // 내가 쓴 평론 개수

    @ColumnDefault("0")
    private Integer scrapNum; // 내가 스크랩한 영화 개수

    @ColumnDefault("0")
    private Integer followingsNum;

    @ColumnDefault("0")
    private Integer followersNum;

    public void addCollectionNum() { this.collectionNum ++; }
    public void addScrapNum() { this.scrapNum ++; }
    public void subCollectionNum() { this.collectionNum --; }
    public void subScrapNum() { this.scrapNum --; }


}
