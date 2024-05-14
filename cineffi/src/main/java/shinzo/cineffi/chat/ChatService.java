package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.chat.redisObject.RedisChatMessage;
import shinzo.cineffi.chat.redisObject.RedisChatroom;
import shinzo.cineffi.chat.redisObject.RedisUserChat;
import shinzo.cineffi.chat.repository.ChatMessageRepository;
import shinzo.cineffi.chat.repository.ChatroomRepository;
import shinzo.cineffi.chat.repository.ChatroomTagRepository;
import shinzo.cineffi.chat.repository.UserChatRepository;
import shinzo.cineffi.domain.entity.chat.ChatMessage;
import shinzo.cineffi.domain.entity.chat.Chatroom;
import shinzo.cineffi.domain.entity.chat.ChatroomTag;
import shinzo.cineffi.domain.entity.chat.UserChat;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.enums.UserChatStatus;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorMsg.USER_NOT_FOUND));

        Chatroom chatroom = chatroomRepository.save(Chatroom.builder()
                .title(title)
                .owner(user)
                .closedAt(null) // 처음에는 닫힌 시간이 없습니다.
                .build());

        for (String tag : tags){
            chatroomTagRepository.save(ChatroomTag
                    .builder()
                    .content(tag)
                    .chatroom(chatroom)
                    .build());
        }
        // User user = user 있는사람인지 검사

        // RedisChatroom 객체 생성
        RedisChatroom redisChatroom = chatroom.toRedisChatroom(tags);


        // redis에 RedisChatroom 보내주기
        redisTemplate.opsForHash().put("chatroom", chatroom.getId().toString(), redisChatroom);

        String chatroomId = chatroom.getId().toString();
        String chatroomTitle = chatroom.getTitle();
        String channelName = "chatroom:" + chatroom.getId();
        String notificationMessage = "새로운 채팅방이 생성되었습니다: " + chatroomTitle;

        // 채팅방 생성에 대한 알림
        listenerContainer.addMessageListener(subscriber, new ChannelTopic(channelName));
        redisTemplate.convertAndSend(channelName, notificationMessage);

        // RedisChatroom 객체 반환


    }

    public void backupToDatabase() {
        //레디스 -> DB
        //  backupChatroom();채팅방
        //  backupUserChat();레디스 채팅 유저 목록(HASH)
        //  backupChatlog();레디스 메세지 리스트 chatMessage


//        chatroom 백업
        stringRedisTemplate.opsForSet().members("updateChatroom").stream()
                .map(String.class::cast).collect(Collectors.toList()).forEach(id -> {
                    RedisChatroom redisChatroom = (RedisChatroom) redisTemplate.opsForHash().get("chatroom", id);
                    if (redisChatroom != null) {
                        User user = userRepository.findById(redisChatroom.getOwnerId()).get();
                        Long chatroomId = Long.parseLong(id);
                        String chatroomTitle = redisChatroom.getTitle();
                        Chatroom chatroom = chatroomRepository.save(chatroomRepository.findById(
                                        chatroomId).get().toBuilder().owner(user)
                                .title(chatroomTitle).build());

                    } else
                        System.out.println("redisChatroom is null");
                    // tag를 재설정하는 로직도 삽입해야함.
                });
        redisTemplate.delete("updateChatroom");



        //UserChat 백업
        stringRedisTemplate.opsForSet().members("updateUserChat").stream().map(String.class::cast).toList()
                .forEach(entry -> {
                    String[] parts = entry.split(":");
                    Long chatroomId = Long.parseLong(parts[0]);
                    Long userId = Long.parseLong(parts[1]);

                    UserChat userchat = userChatRepository.findByUserIdAndChatroomId(userId, chatroomId);
                    RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, userId.toString());
                    UserChatStatus redisUserChatStatus = redisUserChat.getRedisUserChatStatus();
                    userChatRepository.save(userchat.toBuilder().userChatStatus(redisUserChatStatus).build());
                });
        // 여기서, 업데이트가 아무것도 안되고 있다. { 원래는 userChatStatus의 업데이트를 갈겨야 한다. }
        // mute 하는걸로는 update목록에 올리면 안되겠군
        stringRedisTemplate.delete("updateUserChat");


        //chatlog 백업
        for (String id : stringRedisTemplate.opsForHash().keys("chatroom")
                .stream().map(String.class::cast).collect(Collectors.toSet())) {

            for (Object messageObj : Objects.requireNonNull(redisTemplate.opsForList().range("chatlog:" + id, 0, -1))) {
                RedisChatMessage message = (RedisChatMessage)messageObj;
                User sender = userRepository.findById(message.getUserId()).orElseThrow(() ->
                        new IllegalArgumentException("User not found with id: " + message.getUserId()));

                ChatMessage chatMessage = ChatMessage.builder()
                        .sender(sender)
                        .chatroom(chatroomRepository.findById(Long.parseLong(id)).get())
                        .content(message.getContent())
                        .timestamp(LocalDateTime.parse(message.getTimestamp()))
                        .build();
                chatMessageRepository.save(chatMessage);

            }
            redisTemplate.delete("chatlog:" + id);
        }
    }


    public void sendMessageToChatroom(Long chatroomId, Long senderId, String message) {

        Object obj = redisTemplate.opsForHash().get("userlist:" + chatroomId, senderId.toString());
        if (obj == null) throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
        if (((RedisUserChat) obj).getRedisUserChatStatus() != UserChatStatus.JOINED)
            throw new CustomException(ErrorMsg.NOT_JOINED_CHATROOM);
        if (((RedisUserChat) obj).getIsMuted())
            throw new CustomException(ErrorMsg.USER_MUTED);
        LocalDateTime now = LocalDateTime.now(); //LocalDateTime.now();
        RedisChatMessage redisChatMessage
                = RedisChatMessage.builder().userId(senderId).content(message).timestamp(now.toString()).build();
        redisTemplate.convertAndSend("chatroom:" + chatroomId, senderId + ":" + message + "(" + now + ")");



    }

    public void joinChatroom() {
        // 유저 있는지 검증
        // 챗룸이 있는 챗룸인지 검증하고
//        UserChat
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