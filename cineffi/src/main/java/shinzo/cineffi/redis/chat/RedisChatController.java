package shinzo.cineffi.redis.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.SuccessMsg;
import shinzo.cineffi.redis.chat.dto.CreateRedisChatroomDTO;
import shinzo.cineffi.redis.chat.dto.JoinRedisChatroomDTO;
import shinzo.cineffi.redis.chat.dto.SendRedisMessageDTO;

import java.util.List;

@RestController
@RequestMapping("/api/redis/chat")
@RequiredArgsConstructor
public class RedisChatController {
    final private RedisChatService redisChatService;


    @PostMapping("/tmp/create/user")
    public ResponseEntity<ResponseDTO<?>> tmpCreateUser(
            @RequestBody String nickname) {
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(redisChatService.tmpCreateUser(nickname))
                        .build()
        );
    }


    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<?>> createChatroom(
            @RequestBody CreateRedisChatroomDTO createRedisChatroomDTO) {
        Long ownerId = createRedisChatroomDTO.getOwnerId();
        RedisChatroom redisChatroom = redisChatService.createChatroom(createRedisChatroomDTO.getTitle(), ownerId);
        redisChatService.joinChatroom(redisChatroom.getId()
                , ownerId
                , createRedisChatroomDTO.getOwnerNickname());
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(redisChatroom)
                        .build()
        );
    }

    @GetMapping("/listAll")
    public ResponseEntity<ResponseDTO<?>> listChatroom() {
        List<RedisChatroom> redisChatroomList = redisChatService.listChatroom();
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(redisChatroomList)
                .build()
        );
    }
    @PostMapping("/{chatroomId}/join")
    public ResponseEntity<ResponseDTO<?>> joinChatroom(
            @PathVariable Long chatroomId,
            @RequestBody JoinRedisChatroomDTO joinRedisChatroomDTO) {
        Long userId = joinRedisChatroomDTO.getUserId();
        String nickname = joinRedisChatroomDTO.getNickname();
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(redisChatService.joinChatroom(chatroomId, userId, nickname)).build()
        );
    }

    @GetMapping("/{chatroomId}/userlist")
    public ResponseEntity<ResponseDTO<?>> listUserChat(@PathVariable Long chatroomId) {
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(redisChatService.listUserChat(chatroomId))
                .build());
    }

    @PostMapping("/{chatroomId}/leave")
    public ResponseEntity<ResponseDTO<?>> leaveChatroom(
            @PathVariable Long chatroomId, @RequestBody Long userId) {
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(redisChatService.leaveChatroom(chatroomId, userId))
                .build()
        );
    }

    @PostMapping("/{chatroomId}/send")
    public ResponseEntity<ResponseDTO<?>> sendMessage(
            @PathVariable Long chatroomId, @RequestBody SendRedisMessageDTO sendRedisMessageDTO) {
        return ResponseEntity.ok(ResponseDTO.builder().message(SuccessMsg.SUCCESS.getDetail())
                .result(redisChatService.sendMessage(chatroomId,
                        sendRedisMessageDTO.getUserId(),
                        sendRedisMessageDTO.getData())).build());
    }

    @GetMapping("/backup")
    public ResponseEntity<ResponseDTO<?>> backupChatroom() {
        redisChatService.backupToDatabase();
        return ResponseEntity.ok(ResponseDTO.builder().message(SuccessMsg.SUCCESS.getDetail()).build());
    }
}