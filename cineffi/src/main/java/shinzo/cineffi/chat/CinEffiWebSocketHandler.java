package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.auth.AuthService;
import shinzo.cineffi.domain.dto.CreateChatroomDTO;
import shinzo.cineffi.domain.dto.SendChatMessageDTO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class CinEffiWebSocketHandler extends TextWebSocketHandler {
    private final ChatController chatController;
    private final EncryptUtil encryptUtil;
    // 웹소켓 연결 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        HttpHeaders headers = session.getHandshakeHeaders();
        String userId = headers.getFirst("userId");
        Long loginUserId = encryptUtil.LongDecrypt(userId);
        session.getAttributes().put("userId", loginUserId);
        chatController.chatSessionInit(loginUserId, session);
        // JWT로 유저정보 어떻게 가져올지 코드 적어야함. @제욱
//        Long userId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());;
    }
    // 웹소켓 연결 종료 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("CinEffiWebSocketHandler.afterConnectionClosed [status] : " + status);
        chatController.chatSessionQuit(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {

        WebSocketMessage webSocketMessage = CinEffiUtils.getObject(textMessage.getPayload());

        String type = webSocketMessage.getType();
        String nickname = ChatController.getNicknameFromSession(session);
        switch(type) {
            case "LIST" :
                sendToSession(session, chatController.chatroomListSend((Boolean)webSocketMessage.getData()));
                break;
            case "CREATE" :
                sendToSession(session, chatController.chatroomCreate(nickname, (CreateChatroomDTO)webSocketMessage.getData()));
                break;
            case "SEND" :
                SendChatMessageDTO messageDTO = (SendChatMessageDTO) webSocketMessage.getData();
                chatController.messageToChatroom(messageDTO.getChatroomId(), nickname, messageDTO.getMessage());
                break;
            case "JOIN" :
                Long joinChatroomId = (Long)webSocketMessage.getData();
                sendToSession(session, chatController.chatroomJoin(nickname, joinChatroomId));
                chatController.messageToChatroom(joinChatroomId, "SERVER", "[notice] : " + nickname + " 님이 입장하셨습니다.");
                break;
            case "EXIT" :
                Long exitChatroomId = (Long)webSocketMessage.getData();
                sendToSession(session, chatController.chatroomLeave(exitChatroomId, nickname));
                break;
            case "BACKUP" :
                break;
            default :
                System.out.println("[FATAL ERROR] Unknown type from Client [type] : " + type);
        }
    }

    public static void sendToSession(WebSocketSession session, WebSocketMessage message) throws Exception {
        if (ChatController.isSessionOK(session)) {session.sendMessage(new TextMessage(CinEffiUtils.getString(message)));}
        else {ChatController.sessionDelete(session);
            System.out.println("[FATAL_ERROR] = sendToSession Failed, session " + session.getId() + " deleted.");
            System.out.println("message.type = " + message.getType() + ", message.data = " + message.getData());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {} //TODO:
}
