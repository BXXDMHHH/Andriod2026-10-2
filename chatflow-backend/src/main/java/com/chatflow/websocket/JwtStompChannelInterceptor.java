package com.chatflow.websocket;
import com.chatflow.conversation.ConversationService;
import com.chatflow.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {
 private static final Pattern TOPIC=Pattern.compile("^/topic/conversations/(\\d+)$");
 private final JwtService jwtService; private final ConversationService conversations;
 public JwtStompChannelInterceptor(JwtService jwtService,ConversationService conversations){this.jwtService=jwtService;this.conversations=conversations;}
 @Override public Message<?> preSend(Message<?> message,MessageChannel channel){
  StompHeaderAccessor accessor=MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);
  if(accessor==null||accessor.getCommand()==null)return message;
  if(StompCommand.CONNECT.equals(accessor.getCommand())){
   String header=accessor.getFirstNativeHeader("Authorization");
   if(header==null||!header.startsWith("Bearer "))throw new MessageDeliveryException(message,"STOMP CONNECT requires a Bearer token");
   try{String username=jwtService.username(header.substring(7));accessor.setUser(new UsernamePasswordAuthenticationToken(username,null,List.of()));}
   catch(JwtException|IllegalArgumentException ex){throw new MessageDeliveryException(message,"Invalid STOMP access token");}
  }
  if(StompCommand.SUBSCRIBE.equals(accessor.getCommand())){
   if(accessor.getUser()==null)throw new MessageDeliveryException(message,"STOMP subscription requires authentication");
   String destination=accessor.getDestination();Matcher matcher=destination==null?null:TOPIC.matcher(destination);
   if(matcher==null||!matcher.matches())throw new MessageDeliveryException(message,"Subscription destination is not allowed");
   try{conversations.requireOwned(Long.parseLong(matcher.group(1)),accessor.getUser().getName());}
   catch(RuntimeException ex){throw new MessageDeliveryException(message,"Conversation subscription is not allowed");}
  }
  return message;
 }
}
