package com.medibook.entity;
import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="audit_log",indexes=@Index(name="idx_audit_actor_created",columnList="actor_id,created_at")) public class AuditLog {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(name="actor_id") private Long actorId; @Column(nullable=false,length=80) private String action; @Column(length=500) private String details; @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();
 protected AuditLog(){} public AuditLog(Long actorId,String action,String details){this.actorId=actorId;this.action=action;this.details=details;} public Long getId(){return id;} public Long getActorId(){return actorId;} public String getAction(){return action;} public String getDetails(){return details;} public Instant getCreatedAt(){return createdAt;}
}
