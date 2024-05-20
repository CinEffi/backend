package shinzo.cineffi.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import shinzo.cineffi.auth.AuthService;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.UserAccountRepository;

import java.io.IOException;

import static shinzo.cineffi.jwt.JWTUtil.ACCESS_PERIOD;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTProvider jwtProvider;
    private final UserAccountRepository userAccountRepository;
    @Override@Order(1)
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (
            //검사하긴하는데 있어도되고없어도되는애들+ 검사하면 안되는 애들
                (request.getRequestURI().startsWith("/api/auth") ||
                        request.getRequestURI().equals("/api/movies/init") ||
                        request.getRequestURI().equals("/api/movies/boxOffice") ||
                        request.getRequestURI().equals("/api/movies/genre") ||
                        request.getRequestURI().equals("/api/movies/upcoming") ||
                        request.getRequestURI().equals("/api/movies/search")||
                        request.getRequestURI().startsWith("/chat")||
                        request.getRequestURI().matches("/api/users/[^/]+")||
                        request.getRequestURI().matches("/api/users/[^/]+/followers")||
                        request.getRequestURI().matches("/api/users/[^/]+/followings")||
                        request.getRequestURI().matches("/api/users/[^/]+/reviews")||
                        request.getRequestURI().matches("/api/users/[^/]+/scrap")||
                        request.getRequestURI().matches("/api/reviews/\\d+")||
                        request.getRequestURI().equals("/api/reviews/hot")||
                        request.getRequestURI().equals("/api/reviews/new")||
                        request.getRequestURI().matches("/api/movies/\\d+"))
                        && !request.getRequestURI().equals("/api/auth/user/check")
                        && !request.getRequestURI().equals("/api/auth/userInfo")
                        && !request.getRequestURI().equals("/api/auth/logout")
                        && !request.getRequestURI().equals("/api/user/delete")
        ) {
            //토큰없어
            if(!JWTUtil.isValidToken(JWTUtil.resolveAccessToken(request))) {//검사안당함 로직
                doFilter(request, response, filterChain);
            }
            else{
                String access = JWTUtil.resolveAccessToken(request);
                Authentication authentication = jwtProvider.getAuthentication(access); // 정상 토큰이면 SecurityContext 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                doFilter(request, response, filterChain);
            }
        }
        else{
            jwtFiltering(request, response);
            doFilter(request, response, filterChain);
        }
    }


    private void jwtFiltering(HttpServletRequest request, HttpServletResponse response) {
        String access = JWTUtil.resolveAccessToken(request);
        String refresh = JWTUtil.resolveRefreshToken(request);
        if (JWTUtil.isValidToken(access)) { // ACCESS 토큰 유효기간 안지남.
            Authentication authentication = jwtProvider.getAuthentication(access); // 정상 토큰이면 SecurityContext 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (refresh == null) {// ACCESS 토큰 유효기간 지남.+ REFRESH 없으면 -> 토큰 만료 되었습니다.
            throw new CustomException(ErrorMsg.ACCESS_TOKEN_EXPIRED);

        } else { // ACCESS 토큰 유효기간 지남.+ REFRESH 있음
            if (!JWTUtil.isValidToken(refresh))
                throw new CustomException(ErrorMsg.REFRESH_TOKEN_EXPIRED);

            String userSequence = JWTUtil.getClaimAttribute(refresh, "sequence");
            Long userSequenceValue = Long.parseLong(userSequence);


            String dbToken = userAccountRepository.selectTokenBymemberNo(userSequenceValue)
                    .orElseThrow(()-> new CustomException(ErrorMsg.USER_NOT_FOUND));

            if (dbToken.equals(refresh)) {
                String newAccessToken = JWTUtil.changeAccessToken(userSequenceValue, "ROLE_USER");

                Cookie accessCookie = new Cookie("access", newAccessToken);
                accessCookie.setMaxAge((int) ACCESS_PERIOD);
                accessCookie.setPath("/");
                accessCookie.setHttpOnly(true);
                response.addCookie(accessCookie);

                Authentication authentication = jwtProvider.getAuthentication(newAccessToken); // 정상 토큰이면 SecurityContext 저장

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                throw new CustomException(ErrorMsg.REFRESH_TOKEN_INCORRECT);
            }
        }
    }

}