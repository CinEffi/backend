package shinzo.cineffi.domain.entity.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter@Setter@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Entity
public class AuthCode {
    @Id
    Long id;
    String email;
    int code;
    LocalDateTime time;
}
