package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.chat.redisObject.RedisChatroom;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @OneToMany(mappedBy = "chatroom")
    private List<ChatroomTag> tagList;

    @JoinColumn(name = "owner_id", referencedColumnName = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChat> userChats;

    @Column(columnDefinition = "TIMESTAMP(3) WITHOUT TIME ZONE")
    private LocalDateTime closedAt;



    // @ColumnDefault("false")
    // private boolean debate;


//    userRepository
//    chatroomTagRepository
//


    public void fromRedisChatroom(RedisChatroom redisChatroom, User owner) {

    }

    public RedisChatroom ChatroomToRedisChatroom() {
        List<String> redisTagList = this.tagList.stream().map(ChatroomTag::getContent).collect(Collectors.toList());

        return RedisChatroom.builder()
                .title(this.title)
                .tags(redisTagList)
                .createdAt(this.getCreatedAt().toString())
                .closedAt(this.closedAt.toString())
                .build();
    }
}
