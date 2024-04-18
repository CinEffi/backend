package shinzo.cineffi.exception.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
@AllArgsConstructor
public enum ErrorMsg {

        /* 400 BAD_REQUEST : 잘못된 요청 */
        PASSWORD_INCORRECT(BAD_REQUEST,"비밀번호가 옳지 않습니다."),
        PASSWORD_INCORRECT_MISMATCH(BAD_REQUEST, "입력하신 비밀번호와 비밀번호 확인이 일치하지 않습니다."),

        /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
        UNAUTHORIZED_MEMBER(UNAUTHORIZED, "인증된 사용자가 아닙니다."),
        NOT_LOGGED_ID(UNAUTHORIZED, "로그인이 되어있지 않습니다."),

        /* 403 FORBIDDEN : 권한 없음 */

        /* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
        USER_NOT_FOUND(NOT_FOUND, "사용자가 존재하지 않습니다."),
        MOVIE_NOT_FOUND(NOT_FOUND, "영화를 찾을 수 없습니다."),
        REVIEW_NOT_FOUND(NOT_FOUND, "리뷰를 찾을 수 없습니다." ),

        /* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
        DUPLICATE_USER(CONFLICT,"이미 가입된 사용자입니다."),
        DUPLICATE_EMAIL(CONFLICT,"중복된 이메일입니다."),


        /* 500 INTERNAL SERVER ERROR : 그 외 서버 에러 (컴파일 관련) */
        FAILED_TO_EXECUTE_FILE(INTERNAL_SERVER_ERROR, "파일 실행에 실패했습니다."),
        FAILED_TO_COMPILE_FILE(INTERNAL_SERVER_ERROR, "파일 컴파일에 실패했습니다.");






        private final HttpStatus httpStatus;
        private final String detail;
    }
