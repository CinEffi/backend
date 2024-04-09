package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import shinzo.cineffi.domain.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCore extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    //자동닉네임 생성해주기("cow@123", "horse@999", "soft-cow@a123") //나중에 구현하기
    private String nickname;




    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "userCore")
    @PrimaryKeyJoinColumn
    private UserProfile userProfile;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,mappedBy = "userCore")
    @PrimaryKeyJoinColumn
    private UserActivityNum userActivityNum;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "userCore")
    @PrimaryKeyJoinColumn
    private UserAccount userAccount;

}