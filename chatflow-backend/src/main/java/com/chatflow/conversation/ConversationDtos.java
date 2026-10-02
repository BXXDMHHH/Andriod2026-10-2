package com.chatflow.conversation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
public final class ConversationDtos {
 private ConversationDtos(){}
 public record CreateRequest(@NotBlank @Size(max=160)String title){}
 public record ConversationResponse(Long id,String title,LocalDateTime createdAt,LocalDateTime updatedAt){
  static ConversationResponse from(ConversationEntity e){return new ConversationResponse(e.getId(),e.getTitle(),e.getCreatedAt(),e.getUpdatedAt());}
 }
}