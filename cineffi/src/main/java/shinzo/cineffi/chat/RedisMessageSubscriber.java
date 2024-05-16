package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.chat.redisObject.RedisUserChat;
import shinzo.cineffi.domain.dto.ChatLogDTO;
import shinzo.cineffi.domain.enums.UserChatStatus;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class RedisMessageSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("onMessage(message = " + message + ", pattern = " + pattern + "); just called");
        String channel = new String(message.getChannel());
        String chatroomIdStr = channel.substring(channel.lastIndexOf(':') + 1);

        String[] parts = CinEffiUtils.extractSegments(new String(message.getBody()), ':');
        String sender = parts[0];
        String content = parts[1];
        String timeStamp = parts[2];

        Map<String, ChatSession> sessions = ChatController.getSessions();
        for (Map.Entry<Object, Object> entry : redisTemplate.opsForHash().entries("userlist:" + chatroomIdStr).entrySet()) {
            if (((RedisUserChat) entry.getValue()).getRedisUserChatStatus() != UserChatStatus.JOINED) continue;
            String receiver = (String)entry.getKey();
            ChatSession chatSession = sessions.get(receiver);
            if (chatSession != null) {
                try {
                    CinEffiWebSocketHandler.sendToSession(chatSession.getSession(), WebSocketMessage.builder()
                            .type("SEND").sender(sender).data(ChatLogDTO.builder()
                                    .nickname(sender)
                                    .content(content)
                                    .timestamp(timeStamp)
                                    .isMine(sender.equals(receiver))
                                    .build())
                            .build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}