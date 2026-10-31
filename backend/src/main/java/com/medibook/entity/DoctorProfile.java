package com.medibook.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name="doctor_profile") public class DoctorProfile {
 @Id private Long userId;
 @OneToOne(fetch=FetchType.LAZY) @MapsId @JoinColumn(name="user_id") private AppUser user;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="specialty_id") private Specialty specialty;
 @Column(length=120) private String qualifications;
 private Integer experienceYears;
 @Column(precision=10,scale=2) private BigDecimal consultationFee;
 @Column(length=100) private String location;
 @Column(length=1000) private String bio;
 private boolean verified=false; private boolean manualConfirmation=false;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();@Column(nullable=false) private Instant updatedAt=Instant.now();@PreUpdate void update(){updatedAt=Instant.now();}
 protected DoctorProfile(){} public DoctorProfile(AppUser user,Specialty specialty,String qualifications,Integer experienceYears,BigDecimal fee,String location,String bio){this.user=user;this.specialty=specialty;this.qualifications=qualifications;this.experienceYears=experienceYears;this.consultationFee=fee;this.location=location;this.bio=bio;}
 public Long getUserId(){return userId;} public AppUser getUser(){return user;} public Specialty getSpecialty(){return specialty;} public void setSpecialty(Specialty v){specialty=v;} public String getQualifications(){return qualifications;} public void setQualifications(String v){qualifications=v;} public Integer getExperienceYears(){return experienceYears;} public void setExperienceYears(Integer v){experienceYears=v;} public BigDecimal getConsultationFee(){return consultationFee;} public void setConsultationFee(BigDecimal v){consultationFee=v;} public String getLocation(){return location;} public void setLocation(String v){location=v;} public String getBio(){return bio;} public void setBio(String v){bio=v;} public boolean isVerified(){return verified;} public void setVerified(boolean v){verified=v;} public boolean isManualConfirmation(){return manualConfirmation;} public void setManualConfirmation(boolean v){manualConfirmation=v;}
}
