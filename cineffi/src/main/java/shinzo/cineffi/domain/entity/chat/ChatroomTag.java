package shinzo.cineffi.domain.entity.chat;

import jakarta.persistence.Column;
import lombok.Getter;
import jakarta.persistence.*;

@Getter
public class ChatroomTag {

    @Column(name = "chatroom_tag_id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id", referencedColumnName = "chatroom_id")
    private Chatroom chatroom;
}
