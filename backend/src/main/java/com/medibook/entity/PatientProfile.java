package com.medibook.entity;
import jakarta.persistence.*;import java.time.Instant;
@Entity @Table(name="patient_profile") public class PatientProfile {
 @Id private Long userId; @OneToOne(fetch=FetchType.LAZY) @MapsId @JoinColumn(name="user_id") private AppUser user;
 @Column(length=30) private String phone; @Column(length=120) private String location;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();@Column(nullable=false) private Instant updatedAt=Instant.now();@PreUpdate void update(){updatedAt=Instant.now();}
 protected PatientProfile(){} public PatientProfile(AppUser user){this.user=user;} public Long getUserId(){return userId;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getLocation(){return location;} public void setLocation(String v){location=v;}
}
