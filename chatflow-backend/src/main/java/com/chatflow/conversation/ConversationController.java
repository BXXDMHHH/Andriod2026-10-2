package com.chatflow.conversation;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/conversations")
public class ConversationController {
 private final ConversationService service;public ConversationController(ConversationService service){this.service=service;}
 @GetMapping public List<ConversationDtos.ConversationResponse> list(@AuthenticationPrincipal String username){return service.list(username);}
 @PostMapping public ConversationDtos.ConversationResponse create(@AuthenticationPrincipal String username,@Valid @RequestBody ConversationDtos.CreateRequest request){return service.create(username,request.title());}
 @GetMapping("/{id}") public ConversationDtos.ConversationResponse get(@AuthenticationPrincipal String username,@PathVariable Long id){return service.get(id,username);}
}