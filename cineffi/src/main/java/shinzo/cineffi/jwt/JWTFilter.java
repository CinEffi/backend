package shinzo.cineffi.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.UserAccountRepository;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static shinzo.cineffi.jwt.JWTUtil.ACCESS_PERIOD;

@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private final JWTProvider jwtProvider;
    private final UserAccountRepository userAccountRepository;
    @Override@Order(1)
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        Pattern pattern;
//        Matcher matcher;
//
//        System.out.println("filter enter!!!");
//        String[] patterns = {
//                  "^/api/users/\\d+$", // users/{user-id}
//                "/users/\\d+/followers$",// users/{user-id}/follwers
//                "/users/\\d+/followings$",// users/{user-id}/followings
//                "/users/\\d+/reviews$",// users/{user-id}/reviews
//                "/users/\\d+/scrap$",// users/{user-id}/scrap
//                "/reviews/\\d+$",// riviews/{movie-id}
//                "/api/movies+$",
//                "/api/auth+$"
//        };
//        String requestURI = request.getRequestURI();
//
//
//
//        for(String p : patterns){
//            pattern = Pattern.compile(p);
//            matcher = pattern.matcher(requestURI);
//            if (matcher.matches()) {
//                System.out.println("find!!!!!!");
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//        }

//        if (
//                request.getRequestURI().startsWith("/api/auth")||//auth로 시작하는 모든 URL
//                        request.getRequestURI().startsWith("/api/movies")||//movies로 시작하는 모든 URL
//                        request.getRequestURI().equals("/api/reviews/hot")||
//                        request.getRequestURI().equals("/api/reviews/new")
//        )
//        {
//            filterChain.doFilter(request, response);
//            return;}
    try{

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

        } catch (Exception e) {
             e.printStackTrace();
        }
        doFilter(request, response, filterChain);
    }


}

