package shinzo.cineffi.redis.chat;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class RedisMessageSubscriber implements MessageListener {

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 여가서,
        System.out.println("onMessage(message = " + message + ", pattern = " + pattern + "); just called");
//        String value = new String(pattern, StandardCharsets.UTF_8);
//        System.out.println("Decoded pattern: " + value);
 //        pattern = [B@4e5f3962); just called
 //       Decoded pattern: chatroom:1 이렇게 나오네


        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        System.out.println("Received message: " + body + " from channel: " + channel);
        // 지금은 출력이 잘 되는지만 확인하고 있지만, FE와 연결되는 경우
        // 이 부분에서 이제 Map<chatroomId, List<WebSocket>> 으로
        // 웹소켓에 전송해주는 처리를 해주면 된다.
    }
}