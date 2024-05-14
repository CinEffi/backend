package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.chat.redisObject.RedisChatroom;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
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

    @OneToMany(mappedBy = "chatroom")
    private List<ChatroomTag> tagList;

    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChat> userChats;

    @Column(columnDefinition = "TIMESTAMP(3) WITHOUT TIME ZONE")
    private LocalDateTime closedAt;

    public RedisChatroom toRedisChatroom(List<String> tagList) {

        return RedisChatroom.builder()
                .title(title)
                .tags(tagList)
                .memberNum(1) // 새로운 채팅방이 생성되면 최소한 한 명의 멤버가 있습니다.
                .createdAt(LocalDateTime.now().toString()) // 현재 시간으로 생성 시간 설정
                .closedAt(null)// 처음에는 닫힌 시간이 없습니다.
                .ownerId(owner.getId())//생성자
                .build();

    }
}
