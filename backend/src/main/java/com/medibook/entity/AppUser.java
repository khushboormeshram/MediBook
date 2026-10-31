package com.medibook.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="app_user", indexes=@Index(name="idx_user_email", columnList="email", unique=true))
public class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=254) private String email;
    @Column(nullable=false) private String passwordHash;
    @Column(nullable=false, length=80) private String fullName;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private Role role;
    @Column(nullable=false) private boolean active=true;
    @Column(nullable=false, updatable=false) private Instant createdAt=Instant.now();
    @Column(nullable=false) private Instant updatedAt=Instant.now();
    @PreUpdate void updateTimestamp(){updatedAt=Instant.now();}
    protected AppUser() {}
    public AppUser(String email,String passwordHash,String fullName,Role role){this.email=email.toLowerCase().trim();this.passwordHash=passwordHash;this.fullName=fullName;this.role=role;}
    public Long getId(){return id;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;} public Role getRole(){return role;} public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
