package shinzo.cineffi.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import shinzo.cineffi.exception.CustomException;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;

import static shinzo.cineffi.exception.message.ErrorMsg.*;

public class JWTUtil {

    //jwt 키 발급
    public static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public static final long ACCESS_PERIOD = 1000L * 60L * 60L;
    //refresh 토큰 2주
    public static final long REFRESH_PERIOD = 1000L * 60L * 60L * 24L * 14L;

    //토큰 발급
    public static JWToken allocateToken(Long userSequence, String role) throws RuntimeException {
        try {
            //JWT 헤더
            JwtBuilder jwtBuilder = Jwts.builder()
                    .setHeaderParam("alg", "HS256")
                    .setHeaderParam("typ", "JWT");
            //JWT 페이로드
            jwtBuilder.claim("sequence", userSequence);                                    //JWT의 body
            jwtBuilder.claim("role", role);                                          //JWT의 body

            Date now = new Date();

                    String accessToken = jwtBuilder.setIssuedAt(now)
                            .setExpiration(new Date(now.getTime() + ACCESS_PERIOD))
                            .signWith(key, SignatureAlgorithm.HS256)                //암호화. JWT에는 권한까지 되어있기 때문에 중요.
                            .compact();

                    System.out.println("key: "+key);

                    String refreshToken = jwtBuilder.setIssuedAt(now)
                            .setExpiration(new Date(now.getTime() + REFRESH_PERIOD))        //암호화. JWT에는 권한까지 되어있기 때문에 중요.
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();

            return new JWToken(accessToken,refreshToken);

        } catch (Exception e) {
            throw new CustomException(Invalid_token);
        }
    }
    //리프레시 토큰으로 액세스 토큰을 재발급
    public static String changeAccessToken(Long userSequence, String role) throws RuntimeException {
        try {
            JwtBuilder jwtBuilder = Jwts.builder()
                    .setHeaderParam("alg", "HS256")
                    .setHeaderParam("typ", "JWT");

            jwtBuilder.claim("sequence", userSequence);                                    //JWT의 body
            jwtBuilder.claim("role", role);                                          //JWT의 body

            Date now = new Date();
            return jwtBuilder.setIssuedAt(now)
                    .setExpiration(new Date(now.getTime() + ACCESS_PERIOD))
                    .signWith(key, SignatureAlgorithm.HS256)                //암호화. JWT에는 권한까지 되어있기 때문에 중요.
                    .compact();
        } catch (Exception e) {
            throw new CustomException(Invalid_token);
        }
    }
    //토큰의 모든 정보 추출
    public static Claims getClaims(String token) throws RuntimeException {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new CustomException(Invalid_token);
        }
    }
    //토큰의 정보에서 일부 정보만 추출하기
    public static String getClaimAttribute(String token, String key) throws RuntimeException {
        return getClaims(token).getOrDefault(key, null).toString();
    }
    public static boolean isValidToken(String token) {//throws RuntimeException
        try {

            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);

            return !Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return false;
        } catch (Exception e) {
            throw new CustomException(Invalid_token);
        }
    }



    public static String resolveAccessToken(HttpServletRequest req) throws RuntimeException {
        System.out.println("resolveAccessToken start");
        Cookie[] cookies = req.getCookies();
        if (null == cookies) throw new CustomException(NOT_LOGGED_ID);;
        Cookie accessToken = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("Refresh"))
                .findAny()
                .orElse(new Cookie("void", null));
        return accessToken.getValue();
    }

    public static String resolveRefreshToken(HttpServletRequest req) throws RuntimeException {
        System.out.println("refresh token");
        Cookie[] cookies = req.getCookies();
        if (null == cookies) return null;
        Cookie refreshToken = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("Refresh"))
                .findAny()
                .orElse(new Cookie("void", null));

        return refreshToken.getValue();
    }


}
