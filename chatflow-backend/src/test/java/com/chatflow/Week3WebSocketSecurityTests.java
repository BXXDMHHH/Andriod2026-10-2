package com.chatflow;

import com.chatflow.websocket.JwtStompChannelInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest @AutoConfigureMockMvc
class Week3WebSocketSecurityTests {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;
 @Autowired JwtStompChannelInterceptor interceptor;
 private final MessageChannel channel=new ExecutorSubscribableChannel();

 @Test void validJwtConnectAndOwnedConversationSubscriptionAreAccepted() throws Exception {
  String username="wsowner_"+System.nanoTime();String token=register(username);long conversationId=createConversation(token);
  StompHeaderAccessor connect=StompHeaderAccessor.create(StompCommand.CONNECT);
  connect.setNativeHeader("Authorization","Bearer "+token);connect.setLeaveMutable(true);
  Message<byte[]> connectMessage=MessageBuilder.createMessage(new byte[0],connect.getMessageHeaders());
  interceptor.preSend(connectMessage,channel);
  assertThat(connect.getUser()).isNotNull();assertThat(connect.getUser().getName()).isEqualTo(username);

  StompHeaderAccessor subscribe=StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
  subscribe.setUser(connect.getUser());subscribe.setDestination("/topic/conversations/"+conversationId);subscribe.setLeaveMutable(true);
  Message<byte[]> subscribeMessage=MessageBuilder.createMessage(new byte[0],subscribe.getMessageHeaders());
  interceptor.preSend(subscribeMessage,channel);
 }

 @Test void missingJwtAndCrossUserConversationSubscriptionAreRejected() throws Exception {
  StompHeaderAccessor connect=StompHeaderAccessor.create(StompCommand.CONNECT);connect.setLeaveMutable(true);
  Message<byte[]> noToken=MessageBuilder.createMessage(new byte[0],connect.getMessageHeaders());
  assertThatThrownBy(()->interceptor.preSend(noToken,channel)).isInstanceOf(MessageDeliveryException.class);

  String ownerToken=register("wsowner2_"+System.nanoTime());long conversationId=createConversation(ownerToken);
  String other=register("wsother_"+System.nanoTime());
  StompHeaderAccessor subscribe=StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
  subscribe.setUser(new UsernamePasswordAuthenticationToken(other,null,List.of()));
  subscribe.setDestination("/topic/conversations/"+conversationId);subscribe.setLeaveMutable(true);
  Message<byte[]> message=MessageBuilder.createMessage(new byte[0],subscribe.getMessageHeaders());
  assertThatThrownBy(()->interceptor.preSend(message,channel)).isInstanceOf(MessageDeliveryException.class);
 }

 private String register(String username)throws Exception {
  MvcResult result=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
    .content("{\"username\":\""+username+"\",\"password\":\"strong-pass-123\"}")).andExpect(status().isOk()).andReturn();
  return mapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
 }
 private long createConversation(String token)throws Exception {
  MvcResult result=mvc.perform(post("/api/v1/conversations").header("Authorization","Bearer "+token)
    .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"WebSocket test\"}")).andExpect(status().isOk()).andReturn();
  return mapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
 }
}
