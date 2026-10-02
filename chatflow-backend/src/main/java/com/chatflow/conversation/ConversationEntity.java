package com.chatflow.conversation;
import com.chatflow.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="conversations",indexes=@Index(name="idx_conversation_owner_updated",columnList="owner_id, updated_at"))
public class ConversationEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="owner_id",nullable=false) private UserEntity owner;
 @Column(nullable=false,length=160) private String title;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 protected ConversationEntity(){} public ConversationEntity(UserEntity owner,String title){this.owner=owner;this.title=title;}
 @PrePersist void onCreate(){LocalDateTime n=LocalDateTime.now();createdAt=n;updatedAt=n;} @PreUpdate void onUpdate(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;}public UserEntity getOwner(){return owner;}public String getTitle(){return title;}public LocalDateTime getCreatedAt(){return createdAt;}public LocalDateTime getUpdatedAt(){return updatedAt;}
}