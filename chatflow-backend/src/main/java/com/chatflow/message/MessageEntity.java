package com.chatflow.message;
import com.chatflow.conversation.ConversationEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="messages",indexes=@Index(name="idx_message_conversation_id",columnList="conversation_id, id"))
public class MessageEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conversation_id",nullable=false) private ConversationEntity conversation;
 @Column(name="sender_type",nullable=false,length=20) private String senderType;
 @Column(name="sender_id",nullable=false) private Long senderId;
 @Column(name="content_type",nullable=false,length=20) private String contentType="TEXT";
 @Column(nullable=false,columnDefinition="TEXT") private String content;
 @Column(name="client_msg_id",length=100) private String clientMsgId;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 protected MessageEntity(){}
 public MessageEntity(ConversationEntity c,Long senderId,String content,String clientMsgId){this(c,senderId,"USER",content,clientMsgId);}
 public MessageEntity(ConversationEntity c,Long senderId,String senderType,String content,String clientMsgId){this.conversation=c;this.senderId=senderId;this.senderType=senderType;this.contentType="TEXT";this.content=content;this.clientMsgId=clientMsgId;}
 @PrePersist void onCreate(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public ConversationEntity getConversation(){return conversation;} public String getSenderType(){return senderType;} public Long getSenderId(){return senderId;} public String getContentType(){return contentType;} public String getContent(){return content;} public String getClientMsgId(){return clientMsgId;} public LocalDateTime getCreatedAt(){return createdAt;}
}
