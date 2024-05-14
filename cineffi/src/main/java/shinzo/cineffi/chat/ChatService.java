package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.chat.redisObject.RedisChatroom;
import shinzo.cineffi.chat.repository.ChatroomRepository;
import shinzo.cineffi.chat.repository.ChatroomTagRepository;
import shinzo.cineffi.chat.repository.UserChatRepository;
import shinzo.cineffi.domain.entity.chat.Chatroom;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.user.repository.UserRepository;

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



    public void test() {
        redisTemplate.opsForValue().set("a", "apple");
        redisTemplate.opsForValue().set("b", "banana");
        redisTemplate.opsForValue().set("c", "orange");
    }

    public Chatroom redisToChatroom(Long chatroomId, RedisChatroom redisChatroom) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow();
        Long ownerId =redisChatroom.getOwnerId();
        User newOwner = chatroom.getOwner().getId() == ownerId ? null : userRepository.findById(ownerId).get();
        return chatroomRepository.save(chatroom.fromRedisChatroom(redisChatroom, newOwner);
    }
}