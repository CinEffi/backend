package shinzo.cineffi.chat.redisObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("redisUsers")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@Builder
@Getter
public class RedisUser {
//    private String nickname; redis에서는 nickname이 식별자인데, key로 담길것 같음
    private Boolean isBad;
    private Boolean isCertified;
    private Long id;
    private Integer level;
    @JsonCreator
    public RedisUser(
            @JsonProperty("isBad") Boolean idBad,
            @JsonProperty("isCertified") Boolean isCertified,
            @JsonProperty("id") Long id,
            @JsonProperty("level") Integer level
    ) {
        this.isBad = idBad;
        this.isCertified = isCertified;
        this.id = id;
        this.level = level;
    }
}