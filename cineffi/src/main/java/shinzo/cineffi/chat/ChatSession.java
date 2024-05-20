package shinzo.cineffi.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ChatSession {
    private final WebSocketSession session;
    private Long userId;
    private Long chatroomId; // 0이면 쿼리중임
    private String browserSession;
}
