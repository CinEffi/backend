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
import shinzo.cineffi.domain.dto.ChatroomDTO;
import shinzo.cineffi.domain.dto.CreateChatroomDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.dto.SendChatMessageDTO;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class CinEffiWebSocketHandler extends TextWebSocketHandler {
    private final ChatController chatController;

    // 웹소켓 연결 시

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try {
            URI uri = session.getUri();
            String userId = uri.getQuery().split("=")[1];
            Long loginUserId = EncryptUtil.LongDecrypt(userId);
            session.getAttributes().put("userId", loginUserId);
            chatController.chatSessionInit(loginUserId, session);

        } catch (CustomException e) {
            sendToSession(session, WebSocketMessage.builder().type("ERROR").sender("[SERVER]").data(ResponseDTO
                    .builder().isSuccess(false).message(e.getErrorMsg().getDetail()).build()).build());
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    // 웹소켓 연결 종료 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            System.out.println("CinEffiWebSocketHandler.afterConnectionClosed [status] : " + status);
            chatController.chatSessionQuit(session);
        } catch (CustomException e) {
            System.out.println("[CustomException Occurs] : " + e.getErrorMsg().getDetail());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        try {
            String payload = textMessage.getPayload();
            String type = CinEffiUtils.getObject(payload, "type", String.class);
            String nickname = ChatController.getNicknameFromSession(session);

            if (type.equals("LIST")) {
                Boolean isOpen = CinEffiUtils.getObject(payload, "data", Boolean.class);
                // LIST를 요청하는 경로는 chatList여야만 합니다.// 그리고 그 경우 chatLookers에 포함시킵니다.
                ChatController.getQueryLookers().put(nickname, ChatQuery.builder().queryType(QUERY_TYPE.NONE).build());
                sendToSession(session, chatController.chatroomListSend(isOpen));
            } else if (type.equals("CREATE")) {
                CreateChatroomDTO dto = CinEffiUtils.getObject(payload, "data", CreateChatroomDTO.class);
                sendToSession(session, chatController.chatroomCreate(nickname, dto));
            } else if (type.equals("JOIN")) {
                Long joinChatroomId = CinEffiUtils.getObject(payload, "data", Long.class);
                sendToSession(session, chatController.chatroomJoin(nickname, joinChatroomId));
                chatController.messageToChatroom(joinChatroomId, "[SERVER]:COME", nickname);
            } else if (type.equals("SEND")) {
                SendChatMessageDTO dto = CinEffiUtils.getObject(payload, "data", SendChatMessageDTO.class);
                chatController.messageToChatroom(dto.getChatroomId(), nickname, dto.getMessage());
            } else if (type.equals("EXIT")) {
                Long exitChatroomId = CinEffiUtils.getObject(payload, "data", Long.class);
                sendToSession(session, chatController.chatroomLeave(exitChatroomId, nickname));
                chatController.messageToChatroom(exitChatroomId, "[SERVER]:LEAVE", nickname);
                //chatController.messageToChatroom(exitChatroomId, "[SERVER]:EXIT", "[notice] : " + nickname + " 님이 퇴장하셨습니다.");
            } else if (type.equals("READ")) {
                Long readChatroomId = CinEffiUtils.getObject(payload, "data", Long.class);
                sendToSession(session, chatController.closedChatroomRead(nickname, readChatroomId));
            } else {
                throw new CustomException(ErrorMsg.INVALID_TYPE_CALL);
            }
//            else if (type.equals("CLOSE")) { // [TMP]
//                Long chatroomId = CinEffiUtils.getObject(payload, "data", Long.class);
//                chatController.chatroomClose(chatroomId); // [TMP]
//            }

        } catch (CustomException e) {
            sendToSession(session, WebSocketMessage.builder().type("ERROR").sender("[SERVER]").data(ResponseDTO
                    .builder().isSuccess(false).message(e.getErrorMsg().getDetail()).build()).build());
        } catch (Exception e) {
            sendToSession(session, WebSocketMessage.builder().type("ERROR").sender("[SERVER]").data("[Unhandled Error]" + e.getMessage()).build());
        }
    }

    public static void sendToSession(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
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
