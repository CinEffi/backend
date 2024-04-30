package shinzo.cineffi.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import shinzo.cineffi.jwt.JWTFilter;
import shinzo.cineffi.jwt.JWTProvider;
import shinzo.cineffi.user.repository.UserAccountRepository;

import java.util.Collections;


@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserAccountRepository userAccountRepository;
    private final JWTProvider jwtProvider;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CORS 설정
        http
                .cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource(){
                    final CorsConfiguration configuration = new CorsConfiguration();
                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                        configuration.setAllowCredentials(true);
                        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(60L);

                        return configuration;
                    }
                })));
        //
        http
                .csrf(AbstractHttpConfigurer::disable)//csrf 비활성화
                .formLogin(FormLoginConfigurer::disable)//기본로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable);//httpBasic(헤더에 사용자 이름과 비밀번호 추가) 비활성화
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/api/auth/login/kakao",
                                "/api/auth/signup",
                                "/api/auth/email/check",
                                "api/auth/login/email",
                                "/api/auth/verify/email",
                                "/api/auth/verify/email/check",
                                "/api/auth/nickname/check"
                                ).permitAll()//토큰 없이 동작해야하는 사이트
                        .anyRequest().authenticated());
//                        .anyRequest().permitAll());
        http
                .requestCache(RequestCacheConfigurer::disable);
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));//JWT 방식에서는 스프링시큐리티가 세션을 만들 이유가 없다.
        http
                .addFilterBefore(new JWTFilter(jwtProvider, userAccountRepository), SecurityContextHolderAwareRequestFilter.class);
        http
                .logout(LogoutConfigurer::disable);
        return http.build();


    }

}
