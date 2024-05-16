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
import shinzo.cineffi.chat.redisObject.RedisUser;
import shinzo.cineffi.chat.redisObject.RedisUserChat;
import shinzo.cineffi.chat.repository.ChatMessageRepository;
import shinzo.cineffi.chat.repository.ChatroomRepository;
import shinzo.cineffi.chat.repository.ChatroomTagRepository;
import shinzo.cineffi.chat.repository.UserChatRepository;
import shinzo.cineffi.domain.dto.ChatLogDTO;
import shinzo.cineffi.domain.dto.ChatroomDTO;
import shinzo.cineffi.domain.dto.ChatroomListDTO;
import shinzo.cineffi.domain.entity.chat.ChatMessage;
import shinzo.cineffi.domain.entity.chat.Chatroom;
import shinzo.cineffi.domain.entity.chat.ChatroomTag;
import shinzo.cineffi.domain.entity.chat.UserChat;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.enums.UserChatRole;
import shinzo.cineffi.domain.enums.UserChatStatus;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static shinzo.cineffi.exception.message.ErrorMsg.USER_NOT_FOUND;

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

    public String userToRedis(User user) {
        RedisUser redisUser = user.getRedisUser();
        String nickname = user.getNickname();
        redisTemplate.opsForHash().put("users", nickname, redisUser);
        return nickname;
    }
    public String chatUserInit(Long userId) {
        return userToRedis(userRepository.findById(userId).orElseThrow(()->
                new CustomException(USER_NOT_FOUND)));
    }
    public void chatUserQuit(String nickname) {
        if(nickname != null)
        redisTemplate.opsForHash().delete("users", nickname);
        else throw new CustomException(USER_NOT_FOUND);
    }

    public ChatroomListDTO listOpenChatroom() {
        List<ChatroomDTO> chatroomDTOList = new ArrayList<>();
        Map<Object, Object> chatrooms = redisTemplate.opsForHash().entries("chatroom");
        Map<String, RedisChatroom> sortedChatrooms = new TreeMap<>(Comparator.comparingInt((String key) -> Integer.parseInt(key)).reversed());
        for (Map.Entry<Object, Object> entry : chatrooms.entrySet()) {
            sortedChatrooms.put((String) entry.getKey(), (RedisChatroom)entry.getValue());
        }

        for (Map.Entry<String, RedisChatroom> entry : sortedChatrooms.entrySet()) {
            RedisChatroom redisChatroom = entry.getValue();
            chatroomDTOList.add(ChatroomDTO.builder()
                    .title(redisChatroom.getTitle())
                    .tags(redisChatroom.getTags())
                    .createdAt(LocalDateTime.parse(redisChatroom.getCreatedAt()))
                    .closedAt(LocalDateTime.parse(redisChatroom.getClosedAt()))
                    .userCount(redisChatroom.getMemberNum())
                    .build()
            );
        }
        return ChatroomListDTO.builder().list(chatroomDTOList).count(chatroomDTOList.size()).isOpen(true).build();
    }

    public ChatroomListDTO listClosedChatroom() {
        List<ChatroomDTO> chatroomDTOList = new ArrayList<>();
        List<Chatroom> closedChatroomList = chatroomRepository.findAllByIsDeletedTrueOrderByIdDesc();
        for (Chatroom chatroom : closedChatroomList) {
            chatroomDTOList.add(ChatroomDTO.builder()
                    .title(chatroom.getTitle())
                    .tags(chatroom.getTagList().stream().map(ChatroomTag::getContent).collect(Collectors.toList()))
                    .createdAt(chatroom.getCreatedAt())
                    .closedAt(chatroom.getClosedAt())
                    .userCount(null)
                    .build()
            );
        }
        return ChatroomListDTO.builder().list(chatroomDTOList).count(chatroomDTOList.size()).isOpen(false).build();
    }

    public void sendMessageToChatroom(Long chatroomId, String nickname, String content) {
        if (!nickname.equals("SERVER")) {
            Object obj = redisTemplate.opsForHash().get("userlist:" + chatroomId, nickname);
            if (obj == null) throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
            if (((RedisUserChat) obj).getRedisUserChatStatus() != UserChatStatus.JOINED)
                throw new CustomException(ErrorMsg.NOT_JOINED_CHATROOM);
            if (((RedisUserChat) obj).getIsMuted())
                throw new CustomException(ErrorMsg.USER_MUTED);
        }
        LocalDateTime now = LocalDateTime.now(); //LocalDateTime.now();
        redisTemplate.convertAndSend("chatroom:" + chatroomId, nickname + ":" + content + ":" + now);
        redisTemplate.opsForList().rightPush("chatlog:" + chatroomId, RedisChatMessage.builder()
                .sender(nickname).content(content).timestamp(now.toString()).build()); ;
    }

    public Long createChatroom(String nickname, String title, List<String> tags) {
        RedisUser redisUser = (RedisUser) redisTemplate.opsForHash().get("users", nickname);
        if (redisUser == null) throw new CustomException(USER_NOT_FOUND);
        User creator = userRepository.findById(redisUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Chatroom chatroom = chatroomRepository.save(Chatroom.builder()
                .title(title)
                .owner(creator)
                .closedAt(null) // 처음에는 닫힌 시간이 없습니다.
                .build());
        for (String tag : tags){
            chatroomTagRepository.save(ChatroomTag
                    .builder()
                    .content(tag)
                    .chatroom(chatroom)
                    .build());
        }
        // RedisChatroom 객체 생성
        RedisChatroom redisChatroom = chatroomRepository.save(chatroom.toBuilder()
                .closedAt(chatroom.getCreatedAt().plusHours(24L)).build()).toRedisChatroom(tags);
        // redis에 RedisChatroom 보내주기
        redisTemplate.opsForHash().put("chatroom", chatroom.getId().toString(), redisChatroom);

        Long chatroomId = chatroom.getId();
        String chatroomTitle = chatroom.getTitle();
        String channelName = "chatroom:" + chatroomId;
        String notificationMessage = "새로운 채팅방이 생성되었습니다: " + chatroomTitle;

        // 채팅방 생성에 대한 알림 // 다중 서버에서는 추가 로직 필요 //
        listenerContainer.addMessageListener(subscriber, new ChannelTopic(channelName));
        sendMessageToChatroom(chatroomId, "SERVER", notificationMessage);
        return chatroomId;
    }

    private void backupChatroom() {
        stringRedisTemplate.opsForSet().members("updatedChatroom").stream().map(String.class::cast).collect(Collectors.toList())
                .forEach(id -> {
                    RedisChatroom redisChatroom = (RedisChatroom) redisTemplate.opsForHash().get("chatroom", id);
                    if (redisChatroom != null) {
                        User user = userRepository.findById(redisChatroom.getOwnerId()).get();
                        Long chatroomId = Long.parseLong(id);
                        String chatroomTitle = redisChatroom.getTitle();
                        Chatroom chatroom = chatroomRepository.save(chatroomRepository.findById(
                                        chatroomId).get().toBuilder().owner(user)
                                .title(chatroomTitle).build());
                    } else
                        System.out.println("redisChatroom is null"); // tag를 재설정하는 로직도 삽입해야함.
                });
        redisTemplate.delete("updatedChatroom");
    }

    private void backupUserChat() {
        stringRedisTemplate.opsForSet().members("updatedUserChat").stream().map(String.class::cast).toList()
                .forEach(entry -> {
                    String[] parts = entry.split(":");
                    Long chatroomId = Long.parseLong(parts[0]);
                    String nickname = parts[1];
                    //UserChat userchat = userChatRepository.findByUserIdAndChatroomId(nickname, chatroomId);
                    UserChat userChat = userChatRepository.findByUserNicknameAndChatroomId(nickname, chatroomId);
                    RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, nickname);
                    userChatRepository.save(userChat.toBuilder().userChatStatus(redisUserChat.getRedisUserChatStatus()).build());
                });
        // mute 하는걸로는 update목록에 올리면 안되겠군
        stringRedisTemplate.delete("updatedUserChat");
    }

    private void backupChatLog() {
        for (String id : stringRedisTemplate.opsForHash().keys("chatroom").stream().map(String.class::cast).collect(Collectors.toSet())) {
            Chatroom chatroom = chatroomRepository.findById(Long.parseLong(id)).get();
            for (Object messageObj : Objects.requireNonNull(redisTemplate.opsForList().range("chatlog:" + id, 0, -1))) {
                RedisChatMessage message = (RedisChatMessage)messageObj;
                User sender = userRepository.findByNickname(message.getSender()).orElseThrow(() ->
                        new IllegalArgumentException("User not found with nickname: " + message.getSender()));
                ChatMessage chatMessage = ChatMessage.builder()
                        .sender(sender)
                        .chatroom(chatroom)
                        .content(message.getContent())
                        .timestamp(message.getTimestamp())
                        .build();
                chatMessageRepository.save(chatMessage);
            }
            redisTemplate.delete("chatlog:" + id);
        }
    }

    public void backupToDatabase() { // 레디스 -> DB

        //  backupChatroom();채팅방
        //  backupUserChat();레디스 채팅 유저 목록(HASH)
        //  backupChatlog();레디스 메세지 리스트 chatMessage

        //  chatroom 백업 (그러나 쓰이지 않는다)
        //    backupChatroom();
        //UserChat 백업
        backupUserChat();
        //chatlog 백업
        backupChatLog();
    }

    private List<ChatLogDTO> collectMessageLogs(String nickname, Long chatroomId, RedisChatroom redisChatroom) {
        // 입장 멤버의 소켓에 보내줄 채팅 로그 모으기
        List<ChatLogDTO> chatlogList = new ArrayList<>();// db에서 메시지 받아오자
        for (ChatMessage message : chatMessageRepository.findAllByChatroomIdOrderByTimestampAsc(chatroomId)) {
            String sender = message.getSender().getNickname();
            chatlogList.add(ChatLogDTO.builder()
                    .nickname(sender)
                    .content(message.getContent())
                    .timestamp(message.getTimestamp().toString())
                    .isMine(sender.equals(nickname))
                    .build());
        }// redis에서 마저 쌓인 메시지 긁어오자
        for (Object messageObj : redisTemplate.opsForList().range("chatlog:" + chatroomId, 0, -1)) {
            RedisChatMessage message = (RedisChatMessage) messageObj;
            String sender = message.getSender();
            chatlogList.add(ChatLogDTO.builder()
                    .nickname(sender)
                    .content(message.getContent())
                    .timestamp(message.getTimestamp())
                    .isMine(sender.equals(nickname))
                    .build());
        }
        return chatlogList;
    }

    public List<ChatLogDTO> joinChatroom(String nickname, Long chatroomId) {
        // 유저 있는지 검증(레디스에서 있는지 찾아보기 -> 없으면 null)
        RedisUser redisUser = (RedisUser) redisTemplate.opsForHash().get("users", nickname);
        if (redisUser == null) throw new CustomException(USER_NOT_FOUND);
        // Redis에서 채팅방 정보 확인
        RedisChatroom redisChatroom = (RedisChatroom) redisTemplate.opsForHash().get("chatroom", chatroomId.toString());
        if (redisChatroom == null) throw new CustomException(ErrorMsg.CHATROOM_NON_FOUND);
        // UserChat을 만들거냐 말거냐? (처음들어오는 경우 UserChat이 없다) -> 만들어야함 RedisUserChat이 있는지 확인 -> 있었으면 채팅방에 한번이라도 들어왔던 사람
        RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, nickname);
        if (redisUserChat != null) { // 이전에 들어왔었던 사람
            if (redisUserChat.getRedisUserChatStatus() != UserChatStatus.LEFT) throw new CustomException(ErrorMsg.NOT_LEFT_CHATROOM); // 나가있지 않은 채팅방입니다.
            redisUserChat.setRedisUserChatStatus(UserChatStatus.JOINED);
            stringRedisTemplate.opsForSet().add("updatedUserChat", chatroomId + ":" + nickname);
        } else {  // RedisUserChat이 없다 -> UserChat이 없다 -> 들어온적이 없다 -> 처음들어온거다. -> UserChat을 만들어 줘야함 -> 만들고 레디스 userChat에도 저장
            redisUserChat = userChatRepository.save(UserChat.builder()//RedisUserChat에 저장
                    .user(userRepository.findByNickname(nickname)
                            .orElseThrow(() -> {throw new CustomException(USER_NOT_FOUND);}))
                    .chatroom(chatroomRepository.findById(chatroomId)
                            .orElseThrow(() -> {throw new CustomException(ErrorMsg.CHATROOM_NON_FOUND);}))
                    .userChatStatus(UserChatStatus.JOINED).userChatRole(UserChatRole.MEMBER).build()).userChatToRedis();
        }
        redisTemplate.opsForHash().put("userlist:" + chatroomId, nickname, redisUserChat);
        redisTemplate.opsForHash().put("chatroom", chatroomId.toString(), //RedisUserChat의 멤버 수 증가
                redisChatroom.toBuilder().memberNum(redisChatroom.getMemberNum() + 1).build());
        return collectMessageLogs(nickname, chatroomId, redisChatroom);// 입장 멤버의 소켓에 보내줄 채팅 로그 모으기
    }

    public Long leaveChatroom (Long chatroomId, String  nickname) {
        //유저 있는지 검증(레디스에서 확인 -> 없으면 null)
        RedisUser redisUser = (RedisUser) redisTemplate.opsForHash().get("users", nickname);
        if (redisUser == null) throw new CustomException(USER_NOT_FOUND);
        //채팅방도 존재하는지 검증
        RedisChatroom redisChatroom = (RedisChatroom) redisTemplate.opsForHash().get("chatroom", chatroomId.toString());
        if (redisChatroom == null) throw new CustomException(ErrorMsg.CHATROOM_NON_FOUND);
        //레디스에서 UserChat 정보 확인
        RedisUserChat redisUserChat = (RedisUserChat) redisTemplate.opsForHash().get("userlist:" + chatroomId, nickname);
        if (redisUserChat == null) throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
        if (redisUserChat.getRedisUserChatStatus() != UserChatStatus.JOINED) throw new CustomException(ErrorMsg.NOT_JOINED_CHATROOM);
        //레디스에서 UserChat의 상태를 LEAVE로 변경
        redisUserChat = redisUserChat.toBuilder().redisUserChatStatus(UserChatStatus.LEAVED).build();
        redisTemplate.opsForHash().put("userlist:" + chatroomId, nickname, redisUserChat);
        stringRedisTemplate.opsForSet().add("updatedUserChat", chatroomId + ":" + nickname);
        // 레디스채팅룸에서 멤버수 1명 감소
        redisTemplate.opsForHash().put("chatroom", chatroomId.toString(), redisChatroom.toBuilder().memberNum(redisChatroom.getMemberNum() - 1).build());
        //퇴장 메시지 전송
        return chatroomId;
    }

    @Transactional
    public void closeChatroom(Long chatroomId) {
        // 0. 삭제합니다 공지
        sendMessageToChatroom(chatroomId, "SERVER", "채팅방이 종료됩니다. 이후 종료된 채팅방 목록에서 찾아보실 수 있습니다.");
        // 1. 다 LEAVED 만들기
        for (Map.Entry<Object, Object> entry : redisTemplate.opsForHash().entries("userlist:" + chatroomId).entrySet())
        {
            if (((RedisUserChat) entry.getValue()).getRedisUserChatStatus() != UserChatStatus.JOINED) continue;
            leaveChatroom(chatroomId, (String)entry.getKey());
        }
        // 2. 백업
        backupToDatabase();
        // 3. Redis에서 "userlist:{chatroomId}" 키를 삭제
        redisTemplate.delete("userlist:" + chatroomId);
        // 4. Redis에서 채팅방 데이터 삭제
        redisTemplate.opsForHash().delete("chatroom", chatroomId.toString());
        // 5. Postgres DB에서 chatroom 테이블의 isdelete 필드 변경
        chatroomRepository.updateIsDeleteById(chatroomId, true);
    }

}
