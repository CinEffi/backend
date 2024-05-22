package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.chat.redisObject.RedisUser;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.enums.ScoreTypeEvent;
import shinzo.cineffi.score.ScoreService;

import java.util.Base64;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users")
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    //자동닉네임 생성해주기("cow@123", "horse@999", "soft-cow@a123") //나중에 구현하기
    private String nickname;

    @ColumnDefault("1")
    private Integer level;

    @Lob
    private byte[] profileImage;

    @ColumnDefault("false")
    private Boolean isBad;

    @ColumnDefault("false")
    private Boolean isCertified;

    @ColumnDefault("false")
    private Boolean isKakao;

    @ColumnDefault("0")
    private Integer exp;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,mappedBy = "user")
    @PrimaryKeyJoinColumn
    private UserActivityNum userActivityNum;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    @PrimaryKeyJoinColumn
    private UserAccount userAccount;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    @PrimaryKeyJoinColumn
    private UserAnalysis userAnalysis;

    @PrePersist
    private void setDefaultProfileImage() {
        String defaultProfileImage = "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";
        this.profileImage = Base64.getDecoder().decode(defaultProfileImage);

    }

    private static int maxExp(int level) { return ((Long) Math.round(5 * Math.pow(1.1, level - 1))).intValue(); }

    private ScoreTypeEvent refreshExpStatus() {
        ScoreTypeEvent scoreTypeEvent = ScoreTypeEvent.NOTHING;
        if (this.exp < 0) {
            Integer beforeLevel = this.level;
            while (this.exp < 0)
                this.exp += maxExp(--this.level);
            if (this.level < 10 && 10 <= beforeLevel)
                scoreTypeEvent = ScoreTypeEvent.DOWN_LV10;
        }
        else {
            Integer beforeLevel = this.level;
            int maxExpAmount = maxExp(this.level);
            while (maxExpAmount < this.exp) {
                this.exp -= maxExpAmount;
                this.level++;
                maxExpAmount = maxExp(this.level);
            }
            if (beforeLevel < 10 && 10 <= this.level)
                scoreTypeEvent = ScoreTypeEvent.UP_LV10;
        }
        return scoreTypeEvent;
    }

    public ScoreTypeEvent addExp(int expDelta) {
        this.exp += expDelta;
        return refreshExpStatus();
    }

    // 유저의 Certified 상태를 바꾸는 메서드
    public void changeUserCertifiedStatus(Boolean flag) {
        this.isCertified = flag;
    }

    public RedisUser getRedisUser() {
        return RedisUser.builder()
          //.nickname(this.nickname) // Redis에서는 nickname이 식별자인데, key로 담기니까 그거 가져가셈
            .isBad(this.isBad)
            .isCertified(this.isCertified)
            .id(this.id)
            .level(this.level)
            .build();
    }
}