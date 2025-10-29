package com.antonio.infrastructure.security.config;

import com.antonio.infrastructure.security.filter.IpWhitelistFilter;
import com.antonio.infrastructure.security.filter.RateLimitFilter;
import com.antonio.infrastructure.security.filter.TwoFactorAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    prePostEnabled = true,     
    securedEnabled = true,      
    jsr250Enabled = true       
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final RateLimitFilter rateLimitFilter;
    private final IpWhitelistFilter ipWhitelistFilter;
    private final TwoFactorAuthFilter twoFactorAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())

                .xssProtection(xss -> xss.headerValue("1; mode=block"))

                .contentTypeOptions(content -> {})

                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000) // 1 ano
                )

                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self'; " +
                        "connect-src 'self' https://dummyjson.com; " +
                        "frame-ancestors 'none';"
                    )
                )

                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )

                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(self), camera=(), microphone=(), payment=()")
                )
            )

            .authorizeHttpRequests(auth -> auth
                
                .requestMatchers("/api/public/**", "/api/auth/**", "/login", "/register").permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN")
                
                .requestMatchers("/api/todos/**").authenticated()
                
                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session

                .sessionFixation().newSession()
                
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
                .expiredUrl("/login?expired=true")
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .logoutSuccessUrl("/login?logout=true")
            )

            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable()) 
            
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            
            .addFilterBefore(ipWhitelistFilter, UsernamePasswordAuthenticationFilter.class)
            
            .addFilterAfter(twoFactorAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(6); 
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false); 
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ⚠️ ATENÇÃO: Em produção, especifique domínios exatos
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://myapp.com"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("X-Total-Count", "X-Page-Number"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}