package com.chatflow.message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
public final class MessageDtos {
 private MessageDtos(){}
 public record SendRequest(@NotBlank @Size(max=10000)String content,@Size(max=100)String clientMsgId){}
 public record MessageResponse(Long id,Long conversationId,String senderType,Long senderId,String contentType,String content,String clientMsgId,LocalDateTime createdAt){
  static MessageResponse from(MessageEntity e){return new MessageResponse(e.getId(),e.getConversation().getId(),e.getSenderType(),e.getSenderId(),e.getContentType(),e.getContent(),e.getClientMsgId(),e.getCreatedAt());}
 }
}