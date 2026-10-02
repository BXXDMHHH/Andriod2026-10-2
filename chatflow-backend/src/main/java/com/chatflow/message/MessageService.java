package com.chatflow.message;
import com.chatflow.conversation.ConversationEntity;
import com.chatflow.conversation.ConversationService;
import com.chatflow.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collections;
import java.util.List;
@Service
public class MessageService {
 private final MessageRepository messages;private final ConversationService conversations;private final UserRepository users;
 public MessageService(MessageRepository m,ConversationService c,UserRepository u){messages=m;conversations=c;users=u;}
 @Transactional(readOnly=true) public List<MessageDtos.MessageResponse> history(String username,Long conversationId,Long before,int requestedLimit){
  ConversationEntity c=conversations.requireOwned(conversationId,username);int limit=Math.max(1,Math.min(requestedLimit,100));var page=PageRequest.of(0,limit,Sort.by(Sort.Direction.DESC,"id"));
  List<MessageEntity> found=before==null?messages.findByConversation_IdOrderByIdDesc(c.getId(),page):messages.findByConversation_IdAndIdLessThanOrderByIdDesc(c.getId(),before,page);
  Collections.reverse(found);return found.stream().map(MessageDtos.MessageResponse::from).toList();
 }
 @Transactional public MessageDtos.MessageResponse send(String username,Long conversationId,MessageDtos.SendRequest request){
  ConversationEntity c=conversations.requireOwned(conversationId,username);
  var sender=users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User no longer exists"));
  return MessageDtos.MessageResponse.from(messages.save(new MessageEntity(c,sender.getId(),request.content().trim(),request.clientMsgId())));
 }
}