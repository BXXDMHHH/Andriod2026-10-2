package com.chatflow.user;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="users")
public class UserEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=64) private String username;
 @Column(name="password_hash",nullable=false,length=100) private String passwordHash;
 @Column(length=80) private String nickname;
 @Column(nullable=false,length=20) private String status="ACTIVE";
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @Column(name="updated_at",nullable=false) private LocalDateTime updatedAt;
 protected UserEntity(){}
 public UserEntity(String username,String passwordHash,String nickname){this.username=username;this.passwordHash=passwordHash;this.nickname=nickname==null||nickname.isBlank()?username:nickname;}
 @PrePersist void onCreate(){LocalDateTime now=LocalDateTime.now();createdAt=now;updatedAt=now;}
 @PreUpdate void onUpdate(){updatedAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getUsername(){return username;} public String getPasswordHash(){return passwordHash;} public String getNickname(){return nickname;} public String getStatus(){return status;}
}