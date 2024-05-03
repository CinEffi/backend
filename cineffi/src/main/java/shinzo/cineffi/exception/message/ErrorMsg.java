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
        ACCOUNT_MISMATCH(BAD_REQUEST, "계정정보가 일치하지 않습니다."),
        REFRESH_TOKEN_INCORRECT(BAD_REQUEST,"잘못된 리프레시 토큰입니다"),
        EMPTY_USER(BAD_REQUEST, "없는 사용자입니다."),
        EMPTY_FOLLOWING_USER(BAD_REQUEST, "팔로우 요청하신 유저는 없는 유저입니다."),
        EMPTY_FOLLOW(BAD_REQUEST, "팔로우하고 있지 않습니다."),
        FILE_NOT_IMAGE(BAD_REQUEST, "이미지 파일이 아닙니다."),
        INVALID_SCORE_VALUE(BAD_REQUEST, "평점 값이 유효하지 않습니다."),

        /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
        UNAUTHORIZED_MEMBER(UNAUTHORIZED, "인증된 사용자가 아닙니다."),
        NOT_LOGGED_ID(UNAUTHORIZED, "로그인이 되어있지 않습니다."),

        /* 403 FORBIDDEN : 권한 없음 */
        Invalid_token(FORBIDDEN,"잘못된 토큰입니다."),
        NOT_LOGGED_IN(FORBIDDEN, "로그인이 되어있지 않습니다."),
        UNAUTHORIZED_TOKEN(FORBIDDEN,"잘못된 Refresh 토큰 입니다"),
        /* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
        USER_NOT_FOUND(NOT_FOUND, "사용자가 존재하지 않습니다."),
        MOVIE_NOT_FOUND(NOT_FOUND, "영화를 찾을 수 없습니다."),
        REVIEW_NOT_FOUND(NOT_FOUND, "리뷰를 찾을 수 없습니다." ),
        TOKEN_NOT_FOUND(NOT_FOUND,"토큰을 찾을 수 없습니다."),
        /* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
        DUPLICATE_USER(CONFLICT,"이미 가입된 사용자입니다."),
        DUPLICATE_NICKNAME(CONFLICT,"중복 닉네임입니다."),
        DUPLICATE_EMAIL(CONFLICT,"중복된 이메일입니다."),
        DUPLICATE_FOLLOW(CONFLICT, "이미 팔로우하고 있습니다."),
        REVIEW_LIKE_EXIST(CONFLICT, "이미 좋아요가 존재합니다."),
        REVIEW_LIKE_NOT_EXIST(CONFLICT, "좋아요가 존재하지 않습니다."),
        /*410*/
        ACCESS_TOKEN_EXPIRED(GONE,"액세스 토큰이 만료되었습니다."),
        REFRESH_TOKEN_EXPIRED(GONE,"리프레쉬 토큰이 만료되었습니다."),



        /* 500 INTERNAL SERVER ERROR : 그 외 서버 에러 (컴파일 관련) */
        FAILED_TO_EXECUTE_FILE(INTERNAL_SERVER_ERROR, "파일 실행에 실패했습니다."),
        FAILED_TO_COMPILE_FILE(INTERNAL_SERVER_ERROR, "파일 컴파일에 실패했습니다."),
        FAILED_TO_MOVIE_PROCESS(INTERNAL_SERVER_ERROR, "영화 데이터 가공에 실패했습니다."),

        FAIDED_TO_CONVERT_IMAGE(INTERNAL_SERVER_ERROR, "이미지 파일 업로드에 실패했습니다.");







        private final HttpStatus httpStatus;
        private final String detail;


        }



