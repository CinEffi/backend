package shinzo.cineffi.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;

public class JWTUtil {

    //jwt 키 발급
    public static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

//    public static final long ACCESS_PERIOD = 1000L * 60L * 60L;
    //refresh 토큰 2주
    public static final long ACCESS_PERIOD = 300000;//5분
    public static final long REFRESH_PERIOD =  3600000;//1시간

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
                    System.out.println("the time is : " + now.getTime());
                    String accessToken = jwtBuilder.setIssuedAt(now)
                            .setExpiration(new Date(now.getTime() + ACCESS_PERIOD))
                            .signWith(key, SignatureAlgorithm.HS256)                //암호화. JWT에는 권한까지 되어있기 때문에 중요.
                            .compact();

                    String refreshToken = jwtBuilder.setIssuedAt(now)
                            .setExpiration(new Date(now.getTime() + REFRESH_PERIOD))        //암호화. JWT에는 권한까지 되어있기 때문에 중요.
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();

            return new JWToken(accessToken,refreshToken);

        } catch (Exception e) {
            throw new CustomException(ErrorMsg.Invalid_token);
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
            throw new CustomException(ErrorMsg.Invalid_token);
        }
    }
    //토큰의 모든 정보 추출
    public static Claims getClaims(String token) throws RuntimeException {
        try {

            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorMsg.Invalid_token);
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
        } catch (Exception e) {
            return false;
        }
    }
    public static String resolveAccessToken(HttpServletRequest req) throws RuntimeException {

        Cookie[] cookies = req.getCookies();
        if ( cookies == null) throw new CustomException(ErrorMsg.NOT_LOGGED_ID);
        Cookie accessToken = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("access"))
                .findAny()
                .orElse(new Cookie("void", null));
        return accessToken.getValue();
    }

    public static String resolveRefreshToken(HttpServletRequest req) throws RuntimeException {
        Cookie[] cookies = req.getCookies();
        if (cookies== null) throw new CustomException(ErrorMsg.NOT_LOGGED_ID);
        Cookie refreshToken = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("refresh"))
                .findAny()
                .orElse(new Cookie("void", null));

        return refreshToken.getValue();
    }


}
