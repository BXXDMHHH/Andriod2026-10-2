package com.chatflow.workflow;
import com.chatflow.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="workflows",indexes=@Index(name="idx_workflows_creator_updated",columnList="created_by, updated_at"))
public class WorkflowEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=120) private String name;
 @Column(length=500) private String description;
 @Column(nullable=false) private Integer version=1;
 @Lob @Column(name="definition_json",nullable=false,columnDefinition="LONGTEXT") private String definitionJson;
 @Column(nullable=false,length=20) private String status="ACTIVE";
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="created_by",nullable=false) private UserEntity createdBy;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 protected WorkflowEntity(){}
 public WorkflowEntity(String name,String description,String definitionJson,UserEntity createdBy){this.name=name;this.description=description;this.definitionJson=definitionJson;this.createdBy=createdBy;}
 @PrePersist void onCreate(){LocalDateTime n=LocalDateTime.now();createdAt=n;updatedAt=n;} @PreUpdate void onUpdate(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getName(){return name;} public String getDescription(){return description;} public Integer getVersion(){return version;} public String getDefinitionJson(){return definitionJson;} public String getStatus(){return status;} public UserEntity getCreatedBy(){return createdBy;} public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
