package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Builder
@Getter
public class RedisChatMessage {

    private Long userId;
    private String data;
    private String ms;
    @JsonCreator
    public RedisChatMessage(
            @JsonProperty("userId") Long userId,
            @JsonProperty("data") String data,
            @JsonProperty("ms") String ms
    ) {
        this.userId = userId;
        this.data = data;
        this.ms = ms;
    }
}
