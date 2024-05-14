package shinzo.cineffi.chat.redisObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RedisMessage {
    private Long userId;
    private String content;
    private String timestamp;

    @JsonCreator
    public RedisMessage(
            @JsonProperty("userId") Long userId,
            @JsonProperty("content") String content,
            @JsonProperty("timestamp") String timestamp
    ) {
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
    }
}