package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class Message extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    @ManyToOne
    private User sender;

    @JoinColumn(name = "chatroom_id", referencedColumnName = "chatroom_id")
    @ManyToOne
    private Chatroom chatroom;

    @Column
    private String data;
}