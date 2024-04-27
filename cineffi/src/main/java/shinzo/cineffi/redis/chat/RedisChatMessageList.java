package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class RedisChatMessageList {

    private Long userId;
    private String data;
    private Long ms;
    @JsonCreator
    public RedisChatMessageList (
            @JsonProperty("userId") Long userId,
            @JsonProperty("data") String data,
            @JsonProperty("ms") Long ms
    ) {
        this.userId = userId;
        this.data = data;
        this.ms = ms;
    }
}
