package com.chatflow.workflow;
import com.chatflow.conversation.ConversationEntity;
import com.chatflow.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="workflow_runs",indexes={@Index(name="idx_workflow_runs_user_started",columnList="user_id, started_at"),@Index(name="idx_workflow_runs_conversation",columnList="conversation_id, id")})
public class WorkflowRunEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="workflow_id",nullable=false) private WorkflowEntity workflow;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="conversation_id",nullable=false) private ConversationEntity conversation;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id",nullable=false) private UserEntity user;
 @Column(nullable=false,length=20) private String status="RUNNING";
 @Column(name="current_node_id",length=100) private String currentNodeId;
 @Lob @Column(name="variables_json",nullable=false,columnDefinition="LONGTEXT") private String variablesJson="{}";
 @Column(name="started_at",nullable=false) private LocalDateTime startedAt;
 @Column(name="ended_at") private LocalDateTime endedAt;
 @Column(name="error_message",length=2000) private String errorMessage;
 protected WorkflowRunEntity(){}
 public WorkflowRunEntity(WorkflowEntity workflow,ConversationEntity conversation,UserEntity user,String variablesJson){this.workflow=workflow;this.conversation=conversation;this.user=user;this.variablesJson=variablesJson;}
 @PrePersist void onCreate(){if(startedAt==null)startedAt=LocalDateTime.now();}
 public Long getId(){return id;} public WorkflowEntity getWorkflow(){return workflow;} public ConversationEntity getConversation(){return conversation;} public UserEntity getUser(){return user;} public String getStatus(){return status;} public String getCurrentNodeId(){return currentNodeId;} public String getVariablesJson(){return variablesJson;} public LocalDateTime getStartedAt(){return startedAt;} public LocalDateTime getEndedAt(){return endedAt;} public String getErrorMessage(){return errorMessage;}
 public void setStatus(String v){status=v;} public void setCurrentNodeId(String v){currentNodeId=v;} public void setVariablesJson(String v){variablesJson=v;} public void setEndedAt(LocalDateTime v){endedAt=v;} public void setErrorMessage(String v){errorMessage=v;}
}
