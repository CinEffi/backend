package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.*;
import shinzo.cineffi.domain.entity.user.User;

import java.time.LocalDateTime;

public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @JoinColumn(name = "sender_id", referencedColumnName = "user_id")
    @ManyToOne
    private User sender;

    @JoinColumn(name = "chatroom_id", referencedColumnName = "chatroom_id")
    @ManyToOne
    private Chatroom chatroom;

    @Column(name = "content")
    private String content;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;
}
