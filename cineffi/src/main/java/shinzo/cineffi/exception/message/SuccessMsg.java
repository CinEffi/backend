package shinzo.cineffi.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessMsg {

    SUCCESS(HttpStatus.OK, "처리완료");


    private final HttpStatus httpStatus;
    private final String detail;

}
