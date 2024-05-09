package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class RedisChatService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final RedisMessageSubscriber subscriber;

    private final AtomicLong chatroomIdGenerator = new AtomicLong(0);

    public RedisChatroom createChatroom(String title) {

        Object nextIdObj = redisTemplate.opsForValue().get("nextChatroomId");
        Long nextChatroomId = nextIdObj == null ? 1L : Long.parseLong(nextIdObj.toString());
        // 사실 1L이 아니라 db에서 Generated Id값을 조회해야함.
        // redis에 요청해서 (key : nextChatroomId를 관리합니다 하나씩 높여주면 됩니다.)
        // 이거 chatroomList의 size로 하면 안돼요!
        // redis가 막 (다시)켜졌을떄는 db에서 최상위 id(없으면 0) + 1을 가져오고요

        RedisChatroom redisChatroom = RedisChatroom.builder().id(nextChatroomId).title(title).build();
        redisTemplate.opsForHash().put("chatroom", nextChatroomId.toString(), redisChatroom);
        listenerContainer.addMessageListener(subscriber, new ChannelTopic("chatroom:" + redisChatroom.getId()));
        // 단일 노드에서는 이 시점에만 이렇게 체결하는게 맞다.

        redisTemplate.opsForValue().set("nextChatroomId", (++nextChatroomId).toString());

        // 여러 노드인 경우에도 같으나, join 시 {해당 노드,목적지 채널} 에 대해 첫 Member인 경우에 체결하면 된다.
        // 실질 멤버가 사라지면 SUBS를 해제하는 로직도 해둬야한다.
        return redisChatroom;
    }

    public List<RedisChatroom> listChatroom() {
        Map<Object, Object> chatrooms = redisTemplate.opsForHash().entries("chatroom");
        List<RedisChatroom> chatroomList = new ArrayList<>(chatrooms.size());
        chatrooms.forEach((k, v) -> { chatroomList.add((RedisChatroom) v); });
        return chatroomList;
    }

    public RedisUserChat joinChatroom(Long chatroomId, Long userId, String nickname) {
        boolean isRoomNotExist = false;// redis에서 room정보가 있는지 확인하고, 없으면 차단
        if (isRoomNotExist) {}
        // 근데 클릭해서 들어오는데 그러기 힘들지 않나 (완전 만료된 정보를 뿌려뒀으면 그럴수도 있긴함)
        // 이제는 이건 거의 그릴 리가 없다. 우선 놔두자.
        RedisUserChat redisUserChat = RedisUserChat.builder()
                .userId(userId)
                .chatroomId(chatroomId)
                .nickname(nickname)
                .redisUserChatStatus(RedisUserChatStatus.JOINED)
                .redisUserChatRole(RedisUserChatRole.MEMBER)
                .build();
        redisTemplate.opsForHash().put("userlist:" + chatroomId, userId.toString(), redisUserChat);
        return redisUserChat;
    }

    private void handleMessage(Message message) {
        // Handle the message received from Redis
        System.out.println("RedisChatService.handleMessage");
        System.out.println("message : " + message);
        String messageBody = new String(message.getBody());
        System.out.println("Received message: " + messageBody);
    }


    public List<RedisUserChat> listUserChat(Long chatroomId) {
        Map<Object, Object> userChats = redisTemplate.opsForHash().entries("userlist:" + chatroomId);
        List<RedisUserChat> userChatList = new ArrayList<>(userChats.size());
        userChats.forEach((k, v) -> { userChatList.add((RedisUserChat) v); });
        return userChatList;
    }

    // AOP를 통한 방식 선택이 필요한 서비스 메서드
    public RedisUserChat leaveChatroom(Long chatroomId, Long userId) {
        // 1안) 역직렬화 -> 수정 -> 직렬화 방식
        String userIdString = userId.toString();
        RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, userIdString);
        if (redisUserChat != null) {
            redisUserChat.setRedisUserChatStatus(RedisUserChatStatus.LEFT);
            redisTemplate.opsForHash().put("userlist:" + chatroomId, userIdString, redisUserChat);
        }
        return redisUserChat;
    /*
        2안) JSON 파싱 방식
        String key = "userlist:" + chatroomId;
        String userIdString = userId.toString();
        String jsonString = redisTemplate.opsForHash().get(key, userIdString);
        if (jsonString != null) {
            JSONObject jsonObject = new JSONObject(jsonString);
            jsonObject.put("nickname", newNickname);
            redisTemplate.opsForHash().put(key, userIdString, jsonObject.toString());
        }
        3안) 자료구조 파멸적 개혁하기 (성능은 젤좋을듯)
        redisTemplate.opsForHash().put("userlist:status:" + chatroomId
            , userIdString, RedisUserChatStatus.LEFT);
    */
    }

    public RedisChatMessage sendMaeesage(Long chatroomId, Long userId, String data) {
        RedisChatMessage redisChatMessage = RedisChatMessage.builder().userId(userId).data(data).build();
        Long epochMilli = Instant.now().toEpochMilli();
        RedisChatMessageList redisChatMessageList
                = RedisChatMessageList.builder().userId(userId).data(data).ms(epochMilli).build();
//        redisTemplate.opsForZSet().add("chatlog:" + chatroomId, redisChatMessage, (double) epochMilli);
        redisTemplate.opsForList().rightPush("chatlog:" + chatroomId, redisChatMessageList);

        redisTemplate.convertAndSend("chatroom:" + chatroomId, userId + ":" + data);
        return redisChatMessage;


    /*  2안) chatMessage에 timeStamp 저장하고, list에 저장하기
     ***********************************************************************************
     *  RedisChatMessage redisChatMessage = RedisChatMessage.builder()
     *      .userId(userId).data(data).ms(Instant.now().toEpochMilli()).build();
     *  redisTemplate.opsForList().rightPush("chatlog:" + chatroomId, redisChatMessage);
     ***********************************************************************************
     *  장점 : 자료구조 관리 비용 저하 <-> 단점 : 동기화 이슈 발생 시 대처 (시간 순서 보장) 어려움
     ***********************************************************************************
     *  채팅 처리 과부하 시 얼마나 오류가 많이 일어나는지 <-> 얼마나 많은 용량을 처리하는지
     *  아무래도 점점 list의 승리가 확실시된다..
     */
    }


    public static String parseObjectToJSON(Object obj) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String jsonString = objectMapper.writeValueAsString(obj);
            return jsonString;
        } catch (IOException e) {
            e.printStackTrace();
            return "[parsedToRedisString] : exception occurs";
        }
    }
    /*
     *  [역직렬화 오버헤드 vs 짧아진 문자열이 줄이는 저장비용]
     *  밑의 함수를 통해, 꼭 필요한 데이터만 모으면 객체화할때 생성이 필요하지만, redis에 담기는 데이터 크기가 작아짐
     *  redisTemplate.opsForHash().put("Chatrooms", id.toString(), parseToRedisString(redisChatroom)); 형태로 씀
     *  우선은 사용하지 않는 것으로 하지만, 이후에 논의해서 결정할 수 있도록 하자.
     *  별개로, redis 저장소에 담기는 객체들만 최상위 패키지에 놓는 방법으로 데이터 크기를 훨씬 줄여줄 수 있겠다.
     */

//    극도의 메모리 절약을 위한 함수
//    private static String parseToRedisString(Object obj) {
//        ObjectMapper objectMapper = new ObjectMapper();
//        ObjectNode jsonNode = (ObjectNode) objectMapper.readTree(jsonString);
//        jsonNode.remove("id");
//        jsonNode.remove("@class");
//        return jsonNode.toString();
//  }
}
