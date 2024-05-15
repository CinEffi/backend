package shinzo.cineffi.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Builder
@RequiredArgsConstructor
public class ChatSession {
    private final WebSocketSession session;
    private Long userId;
    private Long chatroomId; // 0이면 쿼리중임
}
