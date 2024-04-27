package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder
@Getter
@Setter
public class RedisChatMessage {
    private Long userId;
    private String data;

    @JsonCreator
    public RedisChatMessage (
            @JsonProperty("userId") Long userId,
            @JsonProperty("data") String data
    ) {
        this.userId = userId;
        this.data = data;
    }

}
