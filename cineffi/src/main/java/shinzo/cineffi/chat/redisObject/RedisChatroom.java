package shinzo.cineffi.chat.redisObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
@RedisHash("chatroom")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Builder(toBuilder = true)
@Getter
public class RedisChatroom { //    private Long id;
    private String title;
    private List<String> tags;
    private Integer memberNum;
    private String createdAt;
    private String closedAt;
    private Long ownerId;
    @JsonCreator public RedisChatroom(
            @JsonProperty("title") String title,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("memberNum") Integer memberNum,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("closedAt") String closedAt,
            @JsonProperty("ownerId") Long ownerId
    ) {
        this.title = title;
        this.tags = tags;
        this.memberNum = memberNum;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
        this.ownerId=ownerId;
    }
}