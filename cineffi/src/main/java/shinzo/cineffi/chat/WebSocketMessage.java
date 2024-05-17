package shinzo.cineffi.chat;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessage<T> {
    private String sender;
    private String type;
    private T data;
    private String path;

    public void setSender(String sender) {
        this.sender = sender;
    }
}
