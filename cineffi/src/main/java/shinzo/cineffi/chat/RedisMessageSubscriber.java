package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RedisMessageSubscriber implements MessageListener {

    private final RedisTemplate<String, Object> redisTemplate;
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 여가서,
        System.out.println("onMessage(message = " + message + ", pattern = " + pattern + "); just called");

        String channel = new String(message.getChannel());
        String[] parts = new String(message.getBody()).split(":");
        String userlist = "userlist:" + channel.split(":")[1];

//        for (RedisUserChat redisUserChat :
//                redisTemplate.opsForHash().entries(userlist).values().stream()
//                        .map(RedisUserChat.class::cast).collect(Collectors.toList()))
//            onChat(redisUserChat, channel, parts[0], parts[1]);
        // 지금은 출력이 잘 되는지만 확인하고 있지만, FE와 연결되는 경우
        // 이 부분에서 이제 Map<chatroomId, List<WebSocket>> 으로
        // 웹소켓에 전송해주는 처리를 해주면 된다.
    }

//    public static void onChat(RedisUserChat redisUserChat, String channel, String userId, String message) {
//        System.out.printf("At %s, %s said to %s(%d) : %s\n" , channel, userId
//                , redisUserChat.getNickname(), redisUserChat.getUserId(), message);
//    }

}