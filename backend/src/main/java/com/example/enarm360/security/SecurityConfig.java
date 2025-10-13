package com.example.enarm360.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;

    public SecurityConfig(UserDetailsService userDetailsService,
                          AuthEntryPointJwt unauthorizedHandler,
                          AuthTokenFilter authTokenFilter) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.authTokenFilter = authTokenFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService); // ✅ sin deprecated
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

@Bean
public SecurityFilterChain filterChain(HttpSecurity http,
                                       DaoAuthenticationProvider provider) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
        .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Permitir acceso a archivos estáticos y frontend
            .requestMatchers("/", "/static/**", "/index.html", "/favicon.ico", "/manifest.json").permitAll()
            
            // AUTENTICACIÓN - URLs públicas
            .requestMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
            
            // REGISTRO - URLs NUEVAS (públicas)
            .requestMatchers("/api/registro/**").permitAll()  // ← TODO /api/registro/* es público

              .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/especialidades/**").permitAll() // 👈 acceso libre
                .requestMatchers("/api/subscription-plans").permitAll() // 👈 planes públicos
             
            
            // URLs de testing
            .requestMatchers("/api/test/**").permitAll()
            
            // URLs que requieren autenticación
            .requestMatchers("/api/auth/me", "/api/auth/logout", "/api/auth/logout-all").authenticated()
            .requestMatchers("/api/auth/sessions", "/api/auth/me/**").authenticated()
            .requestMatchers("/api/dashboard/**").authenticated()

            .requestMatchers("/api/examenes/**").authenticated()
            .requestMatchers("/estudiante/simulador/**").permitAll()
            .requestMatchers("/api/quejas-sugerencias/**").authenticated()

            .requestMatchers(
    "/", 
    "/index.html", 
    "/favicon.ico", 
    "/manifest.json", 
    "/static/**", 
    "/estudiante/**"   // todas las rutas de estudiante las maneja React
).permitAll()

            
            // Todo lo demás requiere autenticación
            .anyRequest().authenticated()
        )
        .authenticationProvider(provider);

    http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
}
