package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.chat.redisObject.RedisChatroom;
import shinzo.cineffi.chat.repository.ChatMessageRepository;
import shinzo.cineffi.chat.repository.ChatroomRepository;
import shinzo.cineffi.chat.repository.ChatroomTagRepository;
import shinzo.cineffi.chat.repository.UserChatRepository;
import shinzo.cineffi.domain.entity.chat.Chatroom;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    private final RedisMessageListenerContainer listenerContainer;
    private final RedisMessageSubscriber subscriber;
    private final UserRepository userRepository;
    private final ChatroomTagRepository chatroomTagRepository;
    private final UserChatRepository userChatRepository;
    private final ChatroomRepository chatroomRepository;
    private final ChatMessageRepository chatMessageRepository;


    public void createChatroom(Long userId,
                               String title
                                , List<String> tags) {

//        type "CREATE" data CreateChatroomDTO

        // User user = user 있는사람인지 검사
        // 챗룸 생성 Chatroom.builder(); db에 담고
        // RedisChatroom redisChatroom = saved.toRedisChatroom();
        // redis에 RedisChatroom 보내주기


        /*
        *

        User user = userRepository.findById(ownerId).orElseThrow(() -> new CustomException(ErrorMsg.USER_NOT_FOUND));
        Chatroom chatroom = chatroomRepository.save(Chatroom.builder().title(title).owner(user).build());
        RedisChatroom redisChatroom = RedisChatroom.builder().id(chatroom.getId()).title(title).ownerId(ownerId).build();
        redisTemplate.opsForHash().put("chatroom", nextChatroomId.toString(), redisChatroom);

//        stringRedisTemplate.opsForSet().add("updateChatroom", nextChatroomId.toString());
//        이부분은, 채팅방의 어떤 설정을 바꿀때 이렇게 합시다. 만드는게 엄청 빠를 필요는 없어요.
        listenerContainer.addMessageListener(subscriber, new ChannelTopic("chatroom:" + redisChatroom.getId()));

        // 단일 노드에서는 이 시점에만 이렇게 체결하는게 맞다.
//        redisTemplate.opsForValue().set("nextChatroomId", (++nextChatroomId).toString());
        // 여러 노드인 경우에도 같으나, join 시 {해당 노드,목적지 채널} 에 대해 첫 Member인 경우에 체결하면 된다.
        // 실질 멤버가 사라지면 SUBS를 해제하는 로직도 해둬야한다.
        return redisChatroom;
        * */
        // WebSocketHandler -> joinChatroom(); 방만든사람 여기로 조인시키기
    }



    public void joinChatroom() {
        // 유저 있는지 검증
        // 챗룸이 있는 챗룸인지 검증하고

        // redisUserChat.getOpsHash().get("userlist:" + 1, 2);
        // userChat 만들어서 db에 save (처음 왔을때만 만들어)
        // 있는지 확인해서, 없을때만 만든다.

        // RedisUserChat ruc = saved.toRedisUserChat();
        // 레디스에 저장하기


        // db에서 그동안 쌓인 메시지 긁어오기 (방번호로 찾아서)
        // redis에서 마저 쌓인 메시지 긁어오기 (방번호 chatlog:1)

        // updatedUserChat에 등록해줘야함

        // sendMessageToChatroom();
        // sendMessage(chatroomId, userId, "[ notice ] : user joined the room");
        // 센드메시지를 보냅니다. (나 왔다고 [Notice : {nickname} 님이 입장하셨습니다.])
    }

    public void leaveChatroom(Long chatroomId, Long userId) {

        //

/*
*      String userIdString = userId.toString();
        RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, userIdString);
        if (redisUserChat == null) throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
        RedisUserChatStatus redisUserChatStatus = redisUserChat.getRedisUserChatStatus();
        if (redisUserChat.getRedisUserChatStatus() != RedisUserChatStatus.JOINED)
            throw new CustomException(ErrorMsg.NOT_JOINED_CHATROOM);
        redisUserChat.setRedisUserChatStatus(RedisUserChatStatus.LEFT);

        redisTemplate.opsForHash().put("userlist:" + chatroomId, userIdString, redisUserChat);

        stringRedisTemplate.opsForSet().add("updateUserChat", chatroomId + ":" + userId);
        sendMessage(chatroomId, userId, "[ notice ] : user left the room");
        return redisUserChat;
*
*
*
* */


    }

    public void listChatroom() {

        /*
        *
        Map<Object, Object> chatrooms = redisTemplate.opsForHash().entries("chatroom");
        List<RedisChatroom> chatroomList = new ArrayList<>(chatrooms.size());
        chatrooms.forEach((k, v) -> {
            chatroomList.add((RedisChatroom) v);
        });
        return chatroomList;

        *
        * */
    }


    public void sendMessageToChatroom(Long chatroomId, Long senderId, String message) {
        /*
        *
        Object obj = redisTemplate.opsForHash().get("userlist:" + chatroomId, userId.toString());
        if (obj == null) throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
        if (((RedisUserChat) obj).getRedisUserChatStatus() != RedisUserChatStatus.JOINED)
            throw new CustomException(ErrorMsg.NOT_JOINED_CHATROOM);
        if (((RedisUserChat) obj).getIsMuted())
            throw new CustomException(ErrorMsg.USER_MUTED);
        LocalDateTime now = LocalDateTime.now(); //LocalDateTime.now();
        RedisChatMessage redisChatMessage
                = RedisChatMessage.builder().userId(userId).data(data).ms(now.toString()).build();
≈        redisTemplate.convertAndSend("chatroom:" + chatroomId, userId + ":" + data + "(" + now + ")");
        return redisChatMessage;
        * */

    }

    public void backupToDatabase() {
        //  backupChatroom();
        //  backupUserChat();
        //  backupChatlog();

        /*
       * //
       * chatroom에 쌓여있는 얘들 전부 백업하기
        stringRedisTemplate.opsForSet().members("updateChatroom").stream()
                .map(String.class::cast).collect(Collectors.toList()).forEach(id -> {
                    RedisChatroom redisChatroom = (RedisChatroom) redisTemplate.opsForHash().get("chatroom", id);
                    if (redisChatroom != null) {
                        User user = userRepository.findById(redisChatroom.getOwnerId()).get();


                        Chatroom chatroom = chatroomRepository.save(chatroomRepository.findById(
                                        redisChatroom.getId()).get().toBuilder().owner(user)
                                .title(redisChatroom.getTitle()).build());

                    } else
                        System.out.println("redisChatroom is null");
                    // tag를 재설정하는 로직도 삽입해야함.
                });
        redisTemplate.delete("updateChatroom");

        stringRedisTemplate.opsForSet().members("updateUserChat").stream().map(String.class::cast).collect(Collectors.toList())
                .forEach(entry -> {
                    String[] parts = entry.split(":");
                    Long chatroomId = Long.parseLong(parts[0]);
                    Long userId = Long.parseLong(parts[1]);

                    UserChat userchat = userChatRepository.findByUserIdAndChatroomId(userId, chatroomId).get();
                    RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, userId.toString());
                    RedisUserChatStatus redisUserChatStatus = redisUserChat.getRedisUserChatStatus();
                    userChatRepository.save(userchat.toBuilder().userChatStatus(userChatStatus).build());
                });
        // 여기서, 업데이트가 아무것도 안되고 있다. { 원래는 userChatStatus의 업데이트를 갈겨야 한다. }
        // mute 하는걸로는 update목록에 올리면 안되겠군
        stringRedisTemplate.delete("updateUserChat");


        for (String id : stringRedisTemplate.opsForHash().keys("chatroom")
                .stream().map(String.class::cast).collect(Collectors.toSet())) {

            for (Object messageObj : redisTemplate.opsForList().range("chatlog:" + id, 0, -1)) {
                RedisChatMessage message = (RedisChatMessage)messageObj;
                messageRepository.save(Message.builder().senderId(message.getUserId())
                        .chatroom(chatroomRepository.findById(Long.parseLong(id)).get())
                        .content(message.getData())
                        .timestamp(LocalDateTime.parse(message.getMs())).build());
            }
            redisTemplate.delete("chatlog:" + id);
        }
    }
        *
        * */
    }


    public void test() {
        redisTemplate.opsForValue().set("a", "apple");
        redisTemplate.opsForValue().set("b", "banana");
        redisTemplate.opsForValue().set("c", "orange");


        stringRedisTemplate.opsForSet().add("updatedChatroom", "1");
        stringRedisTemplate.opsForSet().remove("updatedChatroom", "1");
        Set<String> updatedChatroom = stringRedisTemplate.opsForSet().members("updatedChatroom");


        Long chatroomId = 1L;

//        List<Object> range = redisTemplate.opsForList().range("chatlog:" + chatroomId, 0, -1)
    }

//    public RedisChatroom chatroomToRedis(Chatroom chatroom);
//    public RedisChatroom userChatToRedis(UserChat userChat);

//    public Chatroom redisToChatroom(Long chatroomId, RedisChatroom redisChatroom) {
//        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
//        Long ownerId =redisChatroom.getOwnerId();
//        User newOwner = chatroom.getOwner().getId() == ownerId ? null : userRepository.findById(ownerId).get();
//        return chatroomRepository.save(chatroom.fromRedisChatroom(redisChatroom, newOwner);
//    }
}