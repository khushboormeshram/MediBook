package com.medibook.entity;
import jakarta.persistence.*;
import java.time.*;
import java.time.Instant;
@Entity @Table(name="appointment", indexes={@Index(name="idx_appointment_doctor_start",columnList="doctor_id,start_at"),@Index(name="idx_appointment_patient_start",columnList="patient_id,start_at")}) public class Appointment {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="patient_id",nullable=false) private Long patientId; @Column(name="doctor_id",nullable=false) private Long doctorId;
 @Column(name="start_at",nullable=false) private Instant startAt; @Column(name="end_at",nullable=false) private Instant endAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private AppointmentStatus status;
 @Column(length=500) private String reason;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now(); @Column(nullable=false) private Instant updatedAt=Instant.now();
 @Version private long version;
 @PreUpdate void update(){updatedAt=Instant.now();}
 protected Appointment(){} public Appointment(Long patientId,Long doctorId,Instant start,Instant end,AppointmentStatus status,String reason){this.patientId=patientId;this.doctorId=doctorId;this.startAt=start;this.endAt=end;this.status=status;this.reason=reason;}
 public Long getId(){return id;} public Long getPatientId(){return patientId;} public Long getDoctorId(){return doctorId;} public Instant getStartAt(){return startAt;} public void setStartAt(Instant v){startAt=v;} public Instant getEndAt(){return endAt;} public void setEndAt(Instant v){endAt=v;} public AppointmentStatus getStatus(){return status;} public void setStatus(AppointmentStatus s){status=s;} public String getReason(){return reason;}
}
