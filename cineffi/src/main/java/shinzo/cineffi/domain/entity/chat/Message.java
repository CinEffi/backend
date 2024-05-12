package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

import java.time.LocalDateTime;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
@Getter
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

//    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
//    @ManyToOne
//    private User sender;

    @Column(name = "sender_id")
    Long senderId;

    @JoinColumn(name = "chatroom_id", referencedColumnName = "chatroom_id")
    @ManyToOne
    private Chatroom chatroom;

    @Column(name = "content")
    private String content;

    @Column
    private LocalDateTime timestamp;

}