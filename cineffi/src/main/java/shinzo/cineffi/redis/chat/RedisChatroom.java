package shinzo.cineffi.redis.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.*;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
@RedisHash("chatroom")
@Builder
@Getter
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY, property = "@class")
public class RedisChatroom {
    private Long id;
    private String title;
    // getters and setters

    @JsonCreator
    public RedisChatroom(@JsonProperty("id") Long id, @JsonProperty("title") String title) {
        this.id = id;
        this.title = title;
    }
}