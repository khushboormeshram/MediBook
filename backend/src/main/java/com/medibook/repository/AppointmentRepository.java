package com.medibook.repository;
import com.medibook.entity.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.time.Instant; import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment,Long>{
 @Query("select a from Appointment a where a.doctorId=:doctor and a.startAt < :end and a.endAt > :start and a.status in (com.medibook.entity.AppointmentStatus.PENDING,com.medibook.entity.AppointmentStatus.CONFIRMED)") List<Appointment> findConflicts(@Param("doctor") Long doctor,@Param("start") Instant start,@Param("end") Instant end);
 List<Appointment> findByPatientIdOrderByStartAtDesc(Long patientId); List<Appointment> findByDoctorIdOrderByStartAtAsc(Long doctorId); long countByStatus(AppointmentStatus status);
 List<Appointment> findByStartAtBetweenAndStatusIn(Instant start,Instant end,Collection<AppointmentStatus> statuses);
 @Query(value="select to_char(date_trunc('month',start_at),'YYYY-MM') as month, count(*) as total from appointment group by date_trunc('month',start_at) order by date_trunc('month',start_at)",nativeQuery=true) List<Map<String,Object>> monthlyTrends();
}
