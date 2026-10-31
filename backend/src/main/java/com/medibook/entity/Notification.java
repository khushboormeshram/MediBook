package com.medibook.entity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="notification") public class Notification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="user_id",nullable=false) private Long userId;
 @Column(nullable=false,length=160) private String title; @Column(nullable=false,length=500) private String message; @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now(); @Column(name="is_read",nullable=false) private boolean read=false;
 protected Notification(){} public Notification(Long userId,String title,String message){this.userId=userId;this.title=title;this.message=message;} public Long getId(){return id;} public Long getUserId(){return userId;} public String getTitle(){return title;} public String getMessage(){return message;} public Instant getCreatedAt(){return createdAt;} public boolean isRead(){return read;} public void markRead(){read=true;}
}
