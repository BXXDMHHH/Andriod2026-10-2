package com.chatflow.config;
import com.chatflow.websocket.JwtStompChannelInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
 private final JwtStompChannelInterceptor interceptor; private final String[] allowedOrigins;
 public WebSocketConfig(JwtStompChannelInterceptor interceptor,@Value("${chatflow.websocket.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*,http://10.0.2.2:*}") String[] allowedOrigins){this.interceptor=interceptor;this.allowedOrigins=allowedOrigins;}
 @Override public void configureMessageBroker(MessageBrokerRegistry registry){registry.enableSimpleBroker("/topic");registry.setApplicationDestinationPrefixes("/app");registry.setUserDestinationPrefix("/user");}
 @Override public void registerStompEndpoints(StompEndpointRegistry registry){registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins);}
 @Override public void configureClientInboundChannel(ChannelRegistration registration){registration.interceptors(interceptor);}
}
