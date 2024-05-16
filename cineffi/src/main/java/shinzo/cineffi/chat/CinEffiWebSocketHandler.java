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
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.dto.SendChatMessageDTO;
import shinzo.cineffi.exception.CustomException;

import java.net.URI;
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
        try {
            URI uri = session.getUri();
            String userId = uri.getQuery().split("=")[1];
            System.out.println("userID!!!!!" + userId);
            Long loginUserId = encryptUtil.LongDecrypt(userId);
            session.getAttributes().put("userId", loginUserId);
            chatController.chatSessionInit(loginUserId, session);
        } catch (CustomException e) {
            sendToSession(session, WebSocketMessage.builder().type("ERROR").sender("SERVER").data(ResponseDTO
                    .builder().isSuccess(false).message(e.getErrorMsg().getDetail()).build()).build());
        }
    }

    // 웹소켓 연결 종료 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            System.out.println("CinEffiWebSocketHandler.afterConnectionClosed [status] : " + status);
            chatController.chatSessionQuit(session);
        } catch (CustomException e) {
            sendToSession(session, WebSocketMessage.builder().type("ERROR").sender("SERVER").data(ResponseDTO
                    .builder().isSuccess(false).message(e.getErrorMsg().getDetail()).build()).build());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        try {
            String payload = textMessage.getPayload();
            String type = CinEffiUtils.getObject(payload, "type", String.class);
            String nickname = ChatController.getNicknameFromSession(session);

            System.out.println("session.getUri() = " + session.getUri());
            System.out.println("session.getId() = " + session.getId());
            System.out.println("session.getPrincipal() = " + session.getPrincipal());

            if (type.equals("LIST")) {
                Boolean isOpen = CinEffiUtils.getObject(payload, "data", Boolean.class);
                sendToSession(session, chatController.chatroomListSend(isOpen));
            } else if (type.equals("CREATE")) {
                CreateChatroomDTO dto = CinEffiUtils.getObject(payload, "data", CreateChatroomDTO.class);
                sendToSession(session, chatController.chatroomCreate(nickname, dto));
            } else if (type.equals("JOIN")) {
                Long joinChatroomId = CinEffiUtils.getObject(payload, "data", Long.class);
                sendToSession(session, chatController.chatroomJoin(nickname, joinChatroomId));
                chatController.messageToChatroom(joinChatroomId, "SERVER", "[notice] : " + nickname + " 님이 입장하셨습니다.");
            } else if (type.equals("SEND")) {
                SendChatMessageDTO dto = CinEffiUtils.getObject(payload, "data", SendChatMessageDTO.class);
                chatController.messageToChatroom(dto.getChatroomId(), nickname, dto.getMessage());
            } else if (type.equals("EXIT")) {
                Long exitChatroomId = CinEffiUtils.getObject(payload, "data", Long.class);
                sendToSession(session, chatController.chatroomLeave(exitChatroomId, nickname));
            } else if (type.equals("BACKUP")) {
                System.out.println("Backup");
            } else {
                System.out.println("[FATAL ERROR] Unknown type from Client [type] : " + type);
            }
        } catch (CustomException e) {
            sendToSession(session, WebSocketMessage.builder().type("ERROR").sender("SERVER").data(ResponseDTO
                    .builder().isSuccess(false).message(e.getErrorMsg().getDetail()).build()).build());
        }
    }

    public static void sendToSession(WebSocketSession session, WebSocketMessage message) throws Exception {
        if (ChatController.isSessionOK(session)) {
            session.sendMessage(new TextMessage(CinEffiUtils.getString(message)));
        } else {
            ChatController.sessionDelete(session);
            System.out.println("[FATAL_ERROR] = sendToSession Failed, session " + session.getId() + " deleted.");
            System.out.println("message.type = " + message.getType() + ", message.data = " + message.getData());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {} //TODO:

}
