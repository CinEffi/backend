package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.ChatroomCreateDTO;
import shinzo.cineffi.domain.dto.ChatroomSearchDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
   private final ChatService chatService;
   // 리.. 리건.. 명세 다 만족시키려면 다른 전문가가 와야함..// 그리고 현재 인원수 때문에 Redis와 추후 필히 연동되야함
   @GetMapping("/chatroom?q={q}&page={page}&size={size}")
   public ResponseEntity<ResponseDTO<?>> searchChatroom(
           @RequestParam(required = false) String q,
           @RequestParam(defaultValue = "0") int page,
           @RequestParam(defaultValue = "10") int size
   ) {
      List <ChatroomSearchDTO> chatroomSearchDTOList
              = chatService.searchChatroom(q, page, size);
      ResponseDTO<ChatroomSearchDTO[]> responseDto = ResponseDTO.<ChatroomSearchDTO[]>builder()
              .message(SuccessMsg.SUCCESS.getDetail())
              .result(chatroomSearchDTOList.toArray(new ChatroomSearchDTO[0]))
              .build();
      return ResponseEntity.ok(responseDto);
   }

   @PostMapping("/create")
   public ResponseEntity<ResponseDTO<?>> createChatroom(
           @RequestBody ChatroomCreateDTO chatroomCreateDTO) {
//      Long user_id = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
      Long user_id = 1L;
      Long chatroom_id = chatService.createChatroom(chatroomCreateDTO, user_id);
      ResponseDTO<Long> responseDto = ResponseDTO.<Long>builder()
          .message(SuccessMsg.SUCCESS.getDetail()).result(chatroom_id).build();
      return ResponseEntity.ok(responseDto);
   }

   @DeleteMapping("/{chatroomId}/delete")
   public ResponseEntity<ResponseDTO<?>> deleteChatroom(
           @PathVariable Long chatroomId) {
//      Long user_id = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
      Long user_id = 1L;
      chatService.deleteChatroom(chatroomId, user_id);
      ResponseDTO<Long> responseDto = ResponseDTO.<Long>builder()
              .message(SuccessMsg.SUCCESS.getDetail())
              .result(chatroomId)
              .build();
      return ResponseEntity.ok(responseDto);
   }

   @PostMapping("/{chatroomId}/join")
   public ResponseEntity<ResponseDTO<?>> joinChatroom(
           @PathVariable Long chatroomId) {
//      Long user_id = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
      Long user_id = 1L;
      Long userChat_id = chatService.joinChatroom(chatroomId, user_id);
      ResponseDTO<Long> responseDto = ResponseDTO.<Long>builder()
              .message(SuccessMsg.SUCCESS.getDetail())
              .result(userChat_id)
              .build();
      return ResponseEntity.ok(responseDto);
   }

   @PostMapping("/{chatroomId}/exit")
   public ResponseEntity<ResponseDTO<?>> exitChatroom(
           @PathVariable Long chatroomId) {
//      Long user_id = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
      Long user_id = 1L;
      Long userChat_id = chatService.exitChatroom(chatroomId, user_id);
      ResponseDTO<Long> responseDto = ResponseDTO.<Long>builder()
              .message(SuccessMsg.SUCCESS.getDetail())
              .result(userChat_id)
              .build();
      return ResponseEntity.ok(responseDto);
   }

}