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
import java.util.List;


@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final UserAccountRepository userAccountRepository;
    private final JWTProvider jwtProvider;

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
                        configuration.setAllowedOrigins(List.of("http://localhost:3000",
                                "http://localhost:3003",
                                "https://48fb-175-197-204-117.ngrok-free.app",
                                "https://9e54-180-70-193-179.ngrok-free.app",
                                "https://k7f10638b4382a.user-app.krampoline.com"));
                        configuration.setAllowedMethods(Collections.singletonList("*"));
                        configuration.setAllowedHeaders(Collections.singletonList("*"));
                        configuration.setMaxAge(60L);

                        return configuration;
                    }
                })));

        http
                .csrf(AbstractHttpConfigurer::disable)//csrf 비활성화
                .formLogin(FormLoginConfigurer::disable)//기본로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)//httpBasic(헤더에 사용자 이름과 비밀번호 추가) 비활성화
                .logout(customizer -> customizer
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/")
                .permitAll()
                );

        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/favicon.ico",
                               "/api/auth/**",
                                "/api/auth/*",
                                "/api/movies/**",
                                "/api/reviews/hot",
                                "/api/reviews/new",
                                "/api/reviews/{movie-id}",
                                "/api/users/{user-id}",
                                "/api/users/{user-id}/followers",
                                "/api/users/{user-id}/followings",
                                "/api/users/{user-id}/reviews",
                                "/api/users/{user-id}/scrap",
                                "/api/movies/encrypt-test/**",
                                "/api/users/{user-id}/scrap",
                                "/api/test"
                        ).permitAll()//토큰 없이 동작해야하는 사이트
                        .anyRequest().authenticated());
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
