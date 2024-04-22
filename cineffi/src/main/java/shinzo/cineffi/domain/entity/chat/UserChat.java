package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.enums.UserChatStatus;


@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class UserChat extends BaseEntity {
    @Column(name = "user_chat_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", referencedColumnName = "chatroom_id")
    private Chatroom chatroom;

    @Column(name = "user_chat_status")
    @Enumerated(EnumType.STRING)
    private UserChatStatus userChatStatus;
}