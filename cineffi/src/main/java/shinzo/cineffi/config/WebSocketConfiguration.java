package shinzo.cineffi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import shinzo.cineffi.chat.CinEffiWebSocketHandler;

import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import shinzo.cineffi.chat.ChatController;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final ChatController chatController;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(signalingSocketHandler(chatController), "/chat")
                .setAllowedOrigins("*");
    }

    @Bean
    public org.springframework.web.socket.WebSocketHandler signalingSocketHandler(ChatController chatController) {
        return new CinEffiWebSocketHandler(chatController);
    }

}
