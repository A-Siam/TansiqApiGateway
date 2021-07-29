package com.example.routingandfilteringgateway.config;

import com.example.routingandfilteringgateway.filters.JWTAuthenticationFilter;
import com.example.routingandfilteringgateway.filters.JWTAuthorizationFilter;
import com.example.routingandfilteringgateway.services.JWTService;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final Dotenv dotenv;
    private final JWTService jwtService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        JWTAuthenticationFilter jwtAuthenticationFilter = new JWTAuthenticationFilter(authenticationManagerBean(), dotenv, jwtService);
        jwtAuthenticationFilter.setFilterProcessesUrl("/api/login");
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        http.authorizeRequests().antMatchers("/api/login/**").permitAll();
        http.authorizeRequests().antMatchers("/api/refreshToken/**").permitAll();
        http.authorizeRequests().antMatchers("/api/core/tansiq/**").permitAll();
        http.authorizeRequests().antMatchers(POST, "/api/core/studentFiles").hasAuthority("ROLE_ADMIN");
        http.authorizeRequests().antMatchers(GET, "/api/core/admins/**").hasAuthority("ROLE_ADMIN");
        http.authorizeRequests().antMatchers("/api/core/students/**").hasAuthority("ROLE_STUDENT");
        http.addFilter(jwtAuthenticationFilter);
        http.addFilterBefore(new JWTAuthorizationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
