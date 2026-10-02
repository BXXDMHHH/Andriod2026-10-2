package com.chatflow.config;
import com.chatflow.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
public class SecurityConfig {
 @Bean SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthenticationFilter jwtFilter)throws Exception{
  return http.csrf(c->c.disable()).httpBasic(c->c.disable()).formLogin(c->c.disable())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .exceptionHandling(e->e.authenticationEntryPoint((req,res,ex)->{res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);res.setContentType("application/json");res.getWriter().write("{\"code\":2001,\"message\":\"Authentication required\"}");}))
   .authorizeHttpRequests(a->a.requestMatchers(HttpMethod.POST,"/api/v1/auth/register","/api/v1/auth/login").permitAll().requestMatchers("/actuator/health","/actuator/info","/ws","/ws/**").permitAll().anyRequest().authenticated())
   .addFilterBefore(jwtFilter,UsernamePasswordAuthenticationFilter.class).build();
 }
 @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
}
