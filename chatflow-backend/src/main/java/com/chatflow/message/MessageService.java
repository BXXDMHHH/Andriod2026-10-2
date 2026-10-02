package com.chatflow.message;
import com.chatflow.conversation.ConversationEntity;
import com.chatflow.conversation.ConversationService;
import com.chatflow.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Service
public class MessageService {
 private final MessageRepository messages;private final ConversationService conversations;private final UserRepository users;private final SimpMessagingTemplate broker;
 public MessageService(MessageRepository m,ConversationService c,UserRepository u,SimpMessagingTemplate broker){messages=m;conversations=c;users=u;this.broker=broker;}
 @Transactional(readOnly=true) public List<MessageDtos.MessageResponse> history(String username,Long conversationId,Long before,int requestedLimit){
  ConversationEntity c=conversations.requireOwned(conversationId,username);int limit=Math.max(1,Math.min(requestedLimit,100));var page=PageRequest.of(0,limit,Sort.by(Sort.Direction.DESC,"id"));
  List<MessageEntity> found=before==null?messages.findByConversation_IdOrderByIdDesc(c.getId(),page):messages.findByConversation_IdAndIdLessThanOrderByIdDesc(c.getId(),before,page);
  Collections.reverse(found);return found.stream().map(MessageDtos.MessageResponse::from).toList();
 }
 @Transactional public MessageDtos.MessageResponse send(String username,Long conversationId,MessageDtos.SendRequest request){
  ConversationEntity c=conversations.requireOwned(conversationId,username);
  var sender=users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User no longer exists"));
  MessageEntity saved=messages.save(new MessageEntity(c,sender.getId(),request.content().trim(),request.clientMsgId()));
  publishMessage(saved);return MessageDtos.MessageResponse.from(saved);
 }
 @Transactional public MessageDtos.MessageResponse sendWorkflowMessage(ConversationEntity conversation,Long senderId,String senderType,String content){
  MessageEntity saved=messages.save(new MessageEntity(conversation,senderId,senderType,content,null));publishMessage(saved);return MessageDtos.MessageResponse.from(saved);
 }
 private void publishMessage(MessageEntity entity){
  Map<String,Object> event=new LinkedHashMap<>();event.put("type","message.created");event.put("data",MessageDtos.MessageResponse.from(entity));
  broker.convertAndSend("/topic/conversations/"+entity.getConversation().getId(),event);
 }
}
