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

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<?>> createChatroom(
            @RequestBody CreateRedisChatroomDTO createRedisChatroomDTO) {
        RedisChatroom redisChatroom = redisChatService.createChatroom(createRedisChatroomDTO.getTitle());
        System.out.println("createRedisChatroomDTO = " + createRedisChatroomDTO);
        System.out.println("title : " + createRedisChatroomDTO.getTitle());
        System.out.println("title : " + createRedisChatroomDTO.getOwnerId());
        System.out.println("title : " + createRedisChatroomDTO.getOwnerNickname());
        redisChatService.joinChatroom(redisChatroom.getId()
                , createRedisChatroomDTO.getOwnerId()
                , createRedisChatroomDTO.getOwnerNickname());
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(RedisChatService.parseObjectToJSON(redisChatroom))
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/listAll")
    public ResponseEntity<ResponseDTO<?>> listChatroom() {
        List<RedisChatroom> redisChatroomList = redisChatService.listChatroom();
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(RedisChatService.parseObjectToJSON(redisChatroomList))
                .build();
        return ResponseEntity.ok(responseDTO);
    }


    @PostMapping("/{chatroomId}/join")
    public ResponseEntity<ResponseDTO<?>> joinChatroom(
            @PathVariable Long chatroomId,
            @RequestBody JoinRedisChatroomDTO joinRedisChatroomDTO) {
        Long userId = joinRedisChatroomDTO.getUserId();
        String nickname = joinRedisChatroomDTO.getNickname();
        RedisUserChat redisUserChat = redisChatService.joinChatroom(chatroomId, userId, nickname);
        ResponseDTO<String> responseDto = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(RedisChatService.parseObjectToJSON(redisUserChat))
                .build();
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{chatroomId}/userlist")
    public ResponseEntity<ResponseDTO<?>> listUserChat(@PathVariable Long chatroomId) {
        List<RedisUserChat> redisChatroomList = redisChatService.listUserChat(chatroomId);
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(RedisChatService.parseObjectToJSON(redisChatroomList))
                .build();
        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/{chatroomId}/leave")
    public ResponseEntity<ResponseDTO<?>> leaveChatroom(
            @PathVariable Long chatroomId, @RequestBody Long userId) {
        RedisUserChat redisUserChat = redisChatService.leaveChatroom(chatroomId, userId);
        ResponseDTO<String> responseDto = ResponseDTO.<String>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(RedisChatService.parseObjectToJSON(redisUserChat))
                .build();
        return ResponseEntity.ok(responseDto);
    }
    // 이렇게 변수가 하나인 경우는 {} 로 묶지 않고 바로 값을 전송해도 된다.
    // 사실 단일값을 객체로 받으면 오히려 오류가 발생한다.. 이유는 모르겠음

    @PostMapping("/{chatroomId}/send")
    public ResponseEntity<ResponseDTO<?>> sendMessage(
            @PathVariable Long chatroomId, @RequestBody SendRedisMessageDTO sendRedisMessageDTO) {
        RedisChatMessage redisChatMessage
                = redisChatService.sendMaeesage(chatroomId, sendRedisMessageDTO.getUserId(), sendRedisMessageDTO.getData());
        ResponseDTO<RedisChatMessage> responseDto = ResponseDTO.<RedisChatMessage>builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(redisChatMessage)
                .build();
        return ResponseEntity.ok(responseDto);
    }
}