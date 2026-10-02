package com.chatflow.message;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/conversations/{conversationId}/messages")
public class MessageController {
 private final MessageService service;public MessageController(MessageService service){this.service=service;}
 @GetMapping public List<MessageDtos.MessageResponse> history(@AuthenticationPrincipal String username,@PathVariable Long conversationId,@RequestParam(required=false)Long before,@RequestParam(defaultValue="50")int limit){return service.history(username,conversationId,before,limit);}
 @PostMapping public MessageDtos.MessageResponse send(@AuthenticationPrincipal String username,@PathVariable Long conversationId,@Valid @RequestBody MessageDtos.SendRequest request){return service.send(username,conversationId,request);}
}