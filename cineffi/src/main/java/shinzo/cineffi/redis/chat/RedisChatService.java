package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import shinzo.cineffi.chat.ChatRepository;
import shinzo.cineffi.chat.MessageRepository;
import shinzo.cineffi.chat.UserChatRepository;
import shinzo.cineffi.domain.entity.chat.Chatroom;
import shinzo.cineffi.domain.entity.chat.Message;
import shinzo.cineffi.domain.entity.chat.UserChat;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.enums.UserChatStatus;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisChatService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final RedisMessageSubscriber subscriber;
    private final ChatRepository chatroomRepository;
    private final UserRepository userRepository;
    private final UserChatRepository userChatRepository;
    private final MessageRepository messageRepository;

    // 아주 temporary 한 함수, 실제론 바로 지워집니다.
    public User tmpCreateUser(String nickname) {return userRepository.save(User.builder().nickname(nickname).build());}

    // 태그에 대해 정의해야하고, redis에서 어떻게 정의하고 있어야 하는지 생각해야함.
    // updateChatroom API가 필요함. - 없는 경우 title, owner 만 조정 가능함.


    public RedisChatroom createChatroom(String title, Long ownerId) {
        Object nextIdObj = redisTemplate.opsForValue().get("nextChatroomId");
        Long nextChatroomId = nextIdObj != null ? Long.parseLong(nextIdObj.toString()) : 1L;


        User user = userRepository.findById(ownerId).orElseThrow(() -> new CustomException(ErrorMsg.USER_NOT_FOUND));
        Chatroom chatroom = chatroomRepository.save(Chatroom.builder().title(title).owner(user).build());
        RedisChatroom redisChatroom = RedisChatroom.builder().id(chatroom.getId()).title(title).ownerId(ownerId).build();
        redisTemplate.opsForHash().put("chatroom", nextChatroomId.toString(), redisChatroom);
    /*
        // 사실 1L이 아니라 db에서 Generated Id값을 조회해야함.
        // redis에 요청해서 (key : nextChatroomId를 관리합니다 하나씩 높여주면 됩니다.)
        // 이거 chatroomList의 size로 하면 안돼요!
        // redis가 막 (다시)켜졌을떄는 db에서 최상위 id(없으면 0) + 1을 가져오고요
    저의 이 고민들은 create를 바로 db로 때림으로서 다 사라졌습니다.
    그냥 Chatroom.builder().build(); 해서 갈기면 됩니다.

*/

//        stringRedisTemplate.opsForSet().add("updateChatroom", nextChatroomId.toString());
//        이부분은, 채팅방의 어떤 설정을 바꿀때 이렇게 합시다. 만드는게 엄청 빠를 필요는 없어요.
        listenerContainer.addMessageListener(subscriber, new ChannelTopic("chatroom:" + redisChatroom.getId()));

        // 단일 노드에서는 이 시점에만 이렇게 체결하는게 맞다.
//        redisTemplate.opsForValue().set("nextChatroomId", (++nextChatroomId).toString());
        // 여러 노드인 경우에도 같으나, join 시 {해당 노드,목적지 채널} 에 대해 첫 Member인 경우에 체결하면 된다.
        // 실질 멤버가 사라지면 SUBS를 해제하는 로직도 해둬야한다.
        return redisChatroom;
    }

    public List<RedisChatroom> listChatroom() {
        Map<Object, Object> chatrooms = redisTemplate.opsForHash().entries("chatroom");
        List<RedisChatroom> chatroomList = new ArrayList<>(chatrooms.size());
        chatrooms.forEach((k, v) -> {
            chatroomList.add((RedisChatroom) v);
        });
        return chatroomList;
    }

    public RedisUserChat joinChatroom(Long chatroomId, Long userId, String nickname) {
        // redis에서 room정보가 있는지 확인하고, 없으면 차단

        // 임시 코드 (통상에서는 당연히 join을 할 시점에서는 user가 있어야함) => 유저가 없으면 만들어
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorMsg.USER_NOT_FOUND));
        if (redisTemplate.opsForHash().get("chatroom", chatroomId.toString()) == null)
            throw new CustomException(ErrorMsg.CHATROOM_NOT_FOUND);

        // 근데 클릭해서 들어오는데 그러기 힘들지 않나 (완전 만료된 정보를 뿌려뒀으면 그럴수도 있긴함)
        // 이제는 이건 거의 그릴 리가 없다. 우선 놔두자.

        // *** JOINED User, Banned User 면 접속하면 안되고, Muted User일 경우 상태를 바꾸지 않는다.
        // -> Mute User를 실행한 타이머가 해줘야할일

        // 여기서부터 db에서 가져와야한다.

        UserChat userchat = userChatRepository.findByUserIdAndChatroomId(userId, chatroomId)
                .orElse(userChatRepository.save(UserChat.builder().user(user)
                        .chatroom(chatroomRepository.findById(chatroomId).get())
                        .userChatStatus(UserChatStatus.JOINED).build()));

        RedisUserChat redisUserChat;
        Object obj = redisTemplate.opsForHash().get("userlist" + chatroomId, userId.toString());
        if (obj != null) {
            redisUserChat = (RedisUserChat) obj;
            if (redisUserChat.getRedisUserChatStatus() != RedisUserChatStatus.LEFT)
                throw new CustomException(ErrorMsg.USER_CANNOT_JOIN);
            redisUserChat.setRedisUserChatStatus(RedisUserChatStatus.JOINED);
        } else {
            redisUserChat = RedisUserChat.builder()
                    .userId(userId)
                    .chatroomId(chatroomId)
                    .nickname(nickname)
                    .isMuted(false)
                    .redisUserChatStatus(RedisUserChatStatus.JOINED)
                    .redisUserChatRole(RedisUserChatRole.MEMBER)
                    .build();
        }
        redisTemplate.opsForHash().put("userlist:" + chatroomId, userId.toString(), redisUserChat);
        stringRedisTemplate.opsForSet().add("updateUserChat", chatroomId + ":" + userId);

        List<RedisChatMessage> chatlogList = new ArrayList<>();

        List<Message> allByChatroomIdOrderByCreatedAtAsc = messageRepository.findAllByChatroomIdOrderByTimestampAsc(chatroomId);
        if (allByChatroomIdOrderByCreatedAtAsc.isEmpty())
            System.out.println("messageRepository - Empty");
        else {
            for (Message message : allByChatroomIdOrderByCreatedAtAsc) {
                System.out.println("message.getData() = " + message.getContent());
                System.out.println("message.getChatroom().getId() = " + message.getChatroom().getId());
                System.out.println("message.getSenderId() = " + message.getSenderId());
            }
        }

        allByChatroomIdOrderByCreatedAtAsc.forEach(
                message -> {
                    chatlogList.add(RedisChatMessage.builder()
                            .userId(message.getSenderId())
                            .data(message.getContent())
                            .ms(message.getTimestamp().toString())
                            .build());
                }
        );

        chatlogList.addAll(redisTemplate.opsForList()
                .range("chatlog:" + chatroomId, 0, -1).stream()
                .map(RedisChatMessage.class::cast).collect(Collectors.toList()));
        for (RedisChatMessage chatlog : chatlogList) {
            RedisMessageSubscriber.onChat(redisUserChat, "chatroom:" + chatroomId,
                    chatlog.getUserId().toString(), chatlog.getData());
        }

        sendMessage(chatroomId, userId, "[ notice ] : user joined the room");
        return redisUserChat;
    }
//
//    private void handleMessage(Message message) {
//        // Handle the message received from Redis
//        System.out.println("RedisChatService.handleMessage");
//        System.out.println("message : " + message);
//        String messageBody = new String(message.getBody());
//        System.out.println("Received message: " + messageBody);
//    }PU

    public List<RedisUserChat> listUserChat(Long chatroomId) {
        Map<Object, Object> userChats = redisTemplate.opsForHash().entries("userlist:" + chatroomId);
        List<RedisUserChat> userChatList = new ArrayList<>(userChats.size());
        userChats.forEach((k, v) -> {
            userChatList.add((RedisUserChat) v);
        });
        return userChatList;
    }

    // [MUTED <-> // UNMUTED], [ LEFT, JOINED, BANNED] // [OWNER, MEMBER, OPERATOR]
    // AOP를 통한 방식 선택이 필요한 서비스 메서드
    public RedisUserChat leaveChatroom(Long chatroomId, Long userId) {
        // 1안) 역직렬화 -> 수정 -> 직렬화 방식
        String userIdString = userId.toString();
        RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, userIdString);
        if (redisUserChat == null) throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
        ////////////////////////////////////////////////////////////////////////////////////////////
        System.out.println("RedisChatService.leaveChatroom");
        System.out.println("redisUserChat.getChatroomId() = " + redisUserChat.getChatroomId());
        System.out.println("redisUserChat.getNickname() = " + redisUserChat.getNickname());
        System.out.println("redisUserChat.getUserId() = " + redisUserChat.getUserId());
        RedisUserChatRole redisUserChatRole = redisUserChat.getRedisUserChatRole();
        System.out.println("redisUserChat.getRedisUserChatRole() = " + redisUserChatRole);
        if (redisUserChatRole == RedisUserChatRole.MEMBER) System.out.println("Member");
        else if (redisUserChatRole == RedisUserChatRole.OPERATOR) System.out.println("Operator");
        else if (redisUserChatRole == RedisUserChatRole.OWNER) System.out.println("Owner");
        else System.out.println("Unknown RedisUserChatRole");
        RedisUserChatStatus redisUserChatStatus = redisUserChat.getRedisUserChatStatus();
        System.out.println("redisUserChat.getRedisUserChatStatus() = " + redisUserChatStatus);
        if (redisUserChatStatus == RedisUserChatStatus.LEFT) System.out.println("Left");
        else if (redisUserChatStatus == RedisUserChatStatus.JOINED) System.out.println("Joined");
        else if (redisUserChatStatus == RedisUserChatStatus.BANNED) System.out.println("Banned");
        else System.out.println("Unknown RedisUserChatStatus");
        ////////////////////////////////////////////////////////////////////////////////////////////
        if (redisUserChat.getRedisUserChatStatus() != RedisUserChatStatus.JOINED)
            throw new CustomException(ErrorMsg.NOT_JOINED_CHATROOM);
        redisUserChat.setRedisUserChatStatus(RedisUserChatStatus.LEFT);
        redisTemplate.opsForHash().put("userlist:" + chatroomId, userIdString, redisUserChat);
        stringRedisTemplate.opsForSet().add("updateUserChat", chatroomId + ":" + userId);
        sendMessage(chatroomId, userId, "[ notice ] : user left the room");
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

    public RedisChatMessage sendMessage(Long chatroomId, Long userId, String data) {

        Object obj = redisTemplate.opsForHash().get("userlist:" + chatroomId, userId.toString());
        if (obj == null) throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
        if (((RedisUserChat) obj).getRedisUserChatStatus() != RedisUserChatStatus.JOINED)
            throw new CustomException(ErrorMsg.NOT_JOINED_CHATROOM);
        if (((RedisUserChat) obj).getIsMuted())
            throw new CustomException(ErrorMsg.USER_MUTED);
        LocalDateTime now = LocalDateTime.now(); //LocalDateTime.now();
        RedisChatMessage redisChatMessage
                = RedisChatMessage.builder().userId(userId).data(data).ms(now.toString()).build();
        redisTemplate.opsForList().rightPush("chatlog:" + chatroomId, redisChatMessage);
        redisTemplate.convertAndSend("chatroom:" + chatroomId, userId + ":" + data + "(" + now + ")");
        return redisChatMessage;

    /* 1안 이었던 것 [sorted map (with Score)]
     *      RedisChatMessage redisChatMessage = RedisChatMessage.builder().userId(userId).data(data).build();
     *    redisTemplate.opsForZSet().add("chatlog:" + chatroomId, redisChatMessage, (double) epochMilli);
     ***********************************************************************************
          2안) chatMessage에 timeStamp 저장하고, list에 저장하기
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

    public void backupToDatabase() {
//        chatroom에 쌓여있는 얘들 전부 백업하기
        stringRedisTemplate.opsForSet().members("updateChatroom").stream()
                .map(String.class::cast).collect(Collectors.toList()).forEach(id -> {
                    System.out.println("id = " + id);
                    RedisChatroom redisChatroom = (RedisChatroom) redisTemplate.opsForHash().get("chatroom", id);
                    if (redisChatroom != null) {

                        System.out.println("redisChatroom.getTitle() = " + redisChatroom.getTitle());
                        System.out.println("redisChatroom.getOwnerId() = " + redisChatroom.getOwnerId());
                        System.out.println("redisChatroom.getId() = " + redisChatroom.getId());

                        User user = userRepository.findById(redisChatroom.getOwnerId()).get();


                        Chatroom chatroom = chatroomRepository.save(chatroomRepository.findById(
                                        redisChatroom.getId()).get().toBuilder().owner(user)
                                .title(redisChatroom.getTitle()).build());

                        System.out.println("chatroom.getId() = " + chatroom.getId());
                        System.out.println("chatroom.getTags() = " + chatroom.getTags());
                        System.out.println("chatroom.getTitle() = " + chatroom.getTitle());
                        System.out.println("chatroom.getUserChats() = " + chatroom.getUserChats());
                        System.out.println("chatroom.getOwner() = " + chatroom.getOwner());
                        System.out.println("chatroom saved = " + chatroom);

                    } else
                        System.out.println("redisChatroom is null");
                    // tag를 재설정하는 로직도 삽입해야함.
                });
        redisTemplate.delete("updateChatroom");

        stringRedisTemplate.opsForSet().members("updateUserChat").stream().map(String.class::cast).collect(Collectors.toList())
                .forEach(entry -> {
                    String[] parts = entry.split(":");
                    Long chatroomId = Long.parseLong(parts[0]);
                    System.out.println("chatroomId = " + chatroomId);
                    Long userId = Long.parseLong(parts[1]);
                    System.out.println("userId = " + userId);

                    UserChat userchat = userChatRepository.findByUserIdAndChatroomId(userId, chatroomId).get();
                    RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, userId.toString());
                    RedisUserChatStatus redisUserChatStatus = redisUserChat.getRedisUserChatStatus();

                    UserChatStatus userChatStatus = null;
                    if (redisUserChatStatus == redisUserChatStatus.JOINED)
                        userChatStatus = UserChatStatus.JOINED;
                    else if (redisUserChatStatus == redisUserChatStatus.LEFT)
                        userChatStatus = UserChatStatus.LEAVED;
                    else if (redisUserChatStatus == redisUserChatStatus.BANNED)
                        userChatStatus = UserChatStatus.BANNED;
                    else
                        System.out.println("userChatStatus = " + userChatStatus);

                    System.out.println("userchat = " + userchat);
                    userChatRepository.save(userchat.toBuilder().userChatStatus(userChatStatus).build());
                });
        // 여기서, 업데이트가 아무것도 안되고 있다. { 원래는 userChatStatus의 업데이트를 갈겨야 한다. }
        // mute 하는걸로는 update목록에 올리면 안되겠군
        stringRedisTemplate.delete("updateUserChat");


        for (String id : stringRedisTemplate.opsForHash().keys("chatroom")
                .stream()
                .map(String.class::cast)
                .collect(Collectors.toSet())) {
            System.out.println("id = " + id);
            for (RedisChatMessage message : redisTemplate.opsForList().range("chatlog:" + id, 0, -1)
                    .stream().map(RedisChatMessage.class::cast).collect(Collectors.toList())) {
                System.out.println("messageUserId = " + message.getUserId());
                System.out.println("messageData = " + message.getData());
                System.out.println("message.getMs() = " + message.getMs());
                messageRepository.save(Message.builder().senderId(message.getUserId())
//                  .sender(userRepository.findById(message.getUserId()).get())
                        .chatroom(chatroomRepository.findById(Long.parseLong(id)).get())
                        .content(message.getData())
                        .timestamp(LocalDateTime.parse(message.getMs())).build());
            }
            redisTemplate.delete("chatlog:" + id);
        }
        // 리팩토링이 오지게 필요하다! (왜냐면 userId가 등장할떄마다 userRepository에서 언급해서 가져가고있기 때문에 엄청 비효율적이다.)
    }
}