package com.codepost.CodePost.config;


import com.codepost.CodePost.security.JwtAuthenticationEntryPoint;
import com.codepost.CodePost.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint point;

    @Autowired
    private JwtAuthenticationFilter filter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
//        csrf is in functional interface so we can provide a lambda function to disable it.
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/auth/login").permitAll() // Allow public access to login endpoint
                                .requestMatchers("/code/post").hasRole("ADMIN") // Only ADMIN can POST to /code/
                                .requestMatchers("/code/get", "/code/getLatestVersion", "/code/getAllLatestCode", "/code/current-user").hasAnyRole("USER") // USER and ADMIN can access GET endpoints
                                .anyRequest().authenticated()


                        )
                .exceptionHandling(ex->ex.authenticationEntryPoint(point))
                .sessionManagement(session-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        //Stateless policy so we will not be storing anything on the server


        http.addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
