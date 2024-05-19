package shinzo.cineffi.chat.redisObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RedisChatMessage {
    private String sender;
    //    private Long userId;
    private String content;
    private String timestamp;

    @JsonCreator
    public RedisChatMessage(
            @JsonProperty("sender") String sender,
            @JsonProperty("content") String content,
            @JsonProperty("timestamp") String timestamp
    ) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
}