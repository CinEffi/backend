package shinzo.cineffi.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import shinzo.cineffi.domain.dto.ResponseDTO;

import java.io.IOException;

import static shinzo.cineffi.exception.message.ErrorMsg.NOT_LOGGED_ID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    //CustomException error
    @ExceptionHandler(value = {CustomException.class})
    protected ResponseEntity<ResponseDTO> handleCustomException(CustomException e) {
        log.error("handleCustomException throw CustomException : {}", e.getErrorMsg());
        return ResponseDTO.toExceptionResponseEntity(e.getErrorMsg());
    }

    //정규식 error
    @ExceptionHandler({BindException.class})
    public ResponseEntity<ResponseDTO<?>> bindException(BindException e) {
        return ResponseDTO.toAllExceptionResponseEntity(HttpStatus.BAD_REQUEST,
                e.getFieldError().getDefaultMessage());
    }

    //토큰 없을 시 error
    @ExceptionHandler({MissingRequestHeaderException.class})
    public ResponseEntity<ResponseDTO<?>> missingRequestHeaderException(MissingRequestHeaderException e) {
        return ResponseDTO.toAllExceptionResponseEntity(NOT_LOGGED_ID.getHttpStatus(), NOT_LOGGED_ID.getDetail());
    }

    // 500 error
    @ExceptionHandler({Exception.class})
    public ResponseEntity<ResponseDTO<?>> handleAll(final Exception ex) {
        return ResponseDTO.toAllExceptionResponseEntity(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(value = {IOException.class})
    public ResponseEntity<ResponseDTO<?>> handleIOException(IOException ex) {
        return ResponseDTO.toAllExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
