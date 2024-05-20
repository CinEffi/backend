package shinzo.cineffi.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize
public class WebSocketMessage<T> {
    @JsonProperty
    private String sender;
    @JsonProperty
    private String type;
    @JsonProperty
    private T data;
    @JsonProperty
    private String path;

    public void setSender(String sender) {
        this.sender = sender;
    }
}
