package shinzo.cineffi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import shinzo.cineffi.exception.message.ErrorMsg;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException{
    private final ErrorMsg errorMsg;
}
