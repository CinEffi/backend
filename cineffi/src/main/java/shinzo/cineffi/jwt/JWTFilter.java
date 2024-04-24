package shinzo.cineffi.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.user.repository.UserAccountRepository;

import java.io.IOException;

import static shinzo.cineffi.exception.message.ErrorMsg.*;
import static shinzo.cineffi.jwt.JWTUtil.REFRESH_PERIOD;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTProvider jwtProvider;
    private final UserAccountRepository userAccountRepository;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {

            if (request.getRequestURI().equals("/api/auth/signup")||request.getRequestURI().equals("/api/verify/email/check")||request.getRequestURI().equals("/api/auth/login/email")||request.getRequestURI().equals("/api/verify/email/check")||request.getRequestURI().equals("/api/auth/**")) {//회원가입,로그인페이지,메인페이지 건너뛰기
                filterChain.doFilter(request, response);
                return;
            }
            System.out.println("string access");
            String access = JWTUtil.resolveAccessToken(request);
            System.out.println("string refresh");//없으면 에러 throw
            String refresh = JWTUtil.resolveRefreshToken(request);

            if (JWTUtil.isValidToken(access)) { // ACCESS 토큰 유효기간 안지남.
                System.out.println("valid clear");
                Authentication authentication = jwtProvider.getAuthentication(access); // 정상 토큰이면 SecurityContext 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println(SecurityContextHolder.getContext().getAuthentication());
            } else if (refresh == null) {// ACCESS 토큰 유효기간 지남.+ REFRESH 없으면 -> 토큰 만료 되었습니다.
                throw new CustomException(ACCESS_TOKEN_EXPIRED);
            } else { // ACCESS 토큰 유효기간 지남.+ REFRESH 있음
                if (!JWTUtil.isValidToken(refresh))
                    throw new CustomException(REFRESH_TOKEN_EXPIRED);

                String userSequence = JWTUtil.getClaimAttribute(refresh, "sequence");
                Long userSequenceValue = Long.parseLong(userSequence);

                String dbToken = String.valueOf(userAccountRepository.findByIdAndFetchUserToken(userSequenceValue)
                        .orElseThrow(() -> new CustomException(USER_NOT_FOUND)));//검증 필요

                if (dbToken.equals(refresh)) {
                    String newAccessToken = JWTUtil.changeAccessToken(userSequenceValue, "ROLE_USER");
                    // User user = userRepository.findBySequence(userSequenceValue);


                    Cookie acessCookie = new Cookie("Access", newAccessToken);
                    acessCookie.setMaxAge((int) REFRESH_PERIOD);
                    acessCookie.setPath("/");
                    response.addCookie(acessCookie);


                    Authentication authentication = jwtProvider.getAuthentication(newAccessToken); // 정상 토큰이면 SecurityContext 저장

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    throw new CustomException(REFRESH_TOKEN_INCORRECT);
                }
            }

        } catch (Exception e) {
//
            return;
        }
        doFilter(request, response, filterChain);
    }


}
