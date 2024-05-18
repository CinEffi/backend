package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.hibernate.mapping.Join;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.chat.redisObject.RedisUser;
import shinzo.cineffi.chat.redisObject.RedisUserChat;
import shinzo.cineffi.domain.dto.ChatLogDTO;
import shinzo.cineffi.domain.dto.JoinedChatUserDTO;
import shinzo.cineffi.domain.enums.UserChatStatus;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class RedisMessageSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("onMessage(message = " + message + ", pattern = " + pattern + "); just called");
        String channel = new String(message.getChannel());
        String chatroomIdStr = channel.substring(channel.lastIndexOf(':') + 1);
        String[] parts = CinEffiUtils.extractSegments(new String(message.getBody()), '|');

        String sender = parts[0];
        String content = parts[1];
        String timeStamp = parts[2];

        ChatLogDTO chatLogDTO = null;
        JoinedChatUserDTO joinedChatUserDTO = null;
        String type = "SEND";

        if (sender.equals("SERVER:COME") || sender.equals("SERVER:LEAVE")) {
            RedisUser redisUser = (RedisUser) redisTemplate.opsForHash().get("redisUsers", content);
            int splitedIndex = sender.lastIndexOf(':');
            type = sender.substring(splitedIndex + 1);
            sender = sender.substring(0, splitedIndex);
            if (type.equals("COME")) { // JOIN 하는 경우
                joinedChatUserDTO = JoinedChatUserDTO.builder()
                        .nickname(content)
                        .userId(EncryptUtil.LongEncrypt(redisUser.getId()))
                        .level(redisUser.getLevel())
                        .isBad(redisUser.getIsBad())
                        .isCertified(redisUser.getIsCertified())
                        .build();
            }
        }
        else { // 단순 메시지 SEND인 경우
             chatLogDTO = ChatLogDTO.builder().nickname(sender).content(content).timestamp(timeStamp).build();
        }
        sendToChatroomMembers(chatroomIdStr, type, sender, content, chatLogDTO, joinedChatUserDTO);
    }

    private void sendToChatroomMembers(String chatroomIdStr, String type, String sender, String content, ChatLogDTO chatLogDTO, JoinedChatUserDTO joinedChatUserDTO) {
        Map<String, ChatSession> sessions = ChatController.getSessions();
        for (Map.Entry<Object, Object> entry : redisTemplate.opsForHash().entries("userlist:" + chatroomIdStr).entrySet()) {
            if (((RedisUserChat) entry.getValue()).getRedisUserChatStatus() != UserChatStatus.JOINED) continue;
            String receiver = (String)entry.getKey();
            ChatSession chatSession = sessions.get(receiver);
            if (chatSession != null) {
                try {
                    CinEffiWebSocketHandler.sendToSession(chatSession.getSession(), WebSocketMessage.builder()
                            .type(type).sender(sender)
                            .data(chatLogDTO != null ?
                                chatLogDTO.toBuilder().mine(receiver.equals(sender)).build() :
                                (joinedChatUserDTO != null ? joinedChatUserDTO : content)
                            )
                            .build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}