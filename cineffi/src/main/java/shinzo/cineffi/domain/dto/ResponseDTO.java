package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import shinzo.cineffi.exception.message.ErrorMsg;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO<T> {

    @Builder.Default
    private Boolean isSuccess = true;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) //@JsonInclude: 메시지가 null인 경우 출력을 안한다라는 뜻
    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY) //@JsonInclude: data가 null인 경우 출력을 안한다라는 뜻
    private T result;



    public static ResponseEntity<ResponseDTO> toExceptionResponseEntity(ErrorMsg errorMsg) {
        return ResponseEntity
                .status(errorMsg.getHttpStatus())
                .body(ResponseDTO.builder()
                        .message(errorMsg.getDetail())
                        .isSuccess(false)
                        .build()
                );
    }


    public static ResponseEntity<ResponseDTO<?>> toAllExceptionResponseEntity(HttpStatus httpStatus, String errorMsg) {
        return ResponseEntity
                .status(httpStatus.value())
                .body(ResponseDTO.builder()
                        .message(errorMsg)
                        .isSuccess(false)
                        .build()
                );
    }
}

