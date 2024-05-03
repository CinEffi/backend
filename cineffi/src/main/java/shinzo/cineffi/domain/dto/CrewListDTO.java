package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CrewListDTO {

    private String name;
    private byte[] profile;
    private String job;
    private String character;
}
