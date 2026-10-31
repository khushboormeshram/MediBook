package com.medibook.entity;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="doctor_availability", indexes=@Index(name="idx_availability_doctor_day", columnList="doctor_id,day_of_week")) public class DoctorAvailability {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="doctor_id",nullable=false) private Long doctorId;
 @Enumerated(EnumType.STRING) @Column(name="day_of_week",nullable=false) private DayOfWeek dayOfWeek;
 @Column(name="start_time",nullable=false) private LocalTime startTime; @Column(name="end_time",nullable=false) private LocalTime endTime;
 @Column(nullable=false) private int slotMinutes; @Column(name="break_start") private LocalTime breakStart; @Column(name="break_end") private LocalTime breakEnd;
 @Column(nullable=false,updatable=false) private Instant createdAt=Instant.now();@Column(nullable=false) private Instant updatedAt=Instant.now();@PreUpdate void update(){updatedAt=Instant.now();}
 protected DoctorAvailability(){} public DoctorAvailability(Long doctorId,DayOfWeek day,LocalTime start,LocalTime end,int minutes,LocalTime breakStart,LocalTime breakEnd){this.doctorId=doctorId;this.dayOfWeek=day;this.startTime=start;this.endTime=end;this.slotMinutes=minutes;this.breakStart=breakStart;this.breakEnd=breakEnd;}
 public Long getId(){return id;} public Long getDoctorId(){return doctorId;} public DayOfWeek getDayOfWeek(){return dayOfWeek;} public LocalTime getStartTime(){return startTime;} public LocalTime getEndTime(){return endTime;} public int getSlotMinutes(){return slotMinutes;} public LocalTime getBreakStart(){return breakStart;} public LocalTime getBreakEnd(){return breakEnd;}
}
