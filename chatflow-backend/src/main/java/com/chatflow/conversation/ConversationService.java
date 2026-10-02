package com.chatflow.conversation;
import com.chatflow.user.UserEntity;
import com.chatflow.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
@Service
public class ConversationService {
 private final ConversationRepository conversations;private final UserRepository users;
 public ConversationService(ConversationRepository c,UserRepository u){conversations=c;users=u;}
 @Transactional(readOnly=true) public List<ConversationDtos.ConversationResponse> list(String username){return conversations.findByOwner_UsernameOrderByUpdatedAtDesc(username).stream().map(ConversationDtos.ConversationResponse::from).toList();}
 @Transactional public ConversationDtos.ConversationResponse create(String username,String title){UserEntity owner=users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User no longer exists"));return ConversationDtos.ConversationResponse.from(conversations.save(new ConversationEntity(owner,title.trim())));}
 @Transactional(readOnly=true) public ConversationEntity requireOwned(Long id,String username){return conversations.findByIdAndOwner_Username(id,username).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Conversation not found"));}
 @Transactional(readOnly=true) public ConversationDtos.ConversationResponse get(Long id,String username){return ConversationDtos.ConversationResponse.from(requireOwned(id,username));}
}