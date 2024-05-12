package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder(toBuilder = true)
@Entity
@DynamicInsert
public class Chatroom extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatroom_id")
    private Long id;

    @Column(unique = true)
    private String title;

    ///////////////////////////// 이부분은 검색을 위해서 존재하는데, 일대 다 관계로 다시 만들어주시면 됩니다.
    @Column
    private String tags; // 이렇게 말고 아래처럼 해주시면 됩니다..
  // @ManytoOne(mappedBy = chatroom)
  // @Column
  // private List<String> tagList = new ArrayList<>();

    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChat> userChats;

    @ColumnDefault("false")
    private boolean debate;
}