package com.medibook.repository;
import com.medibook.entity.DoctorAvailability; import org.springframework.data.jpa.repository.JpaRepository; import java.time.DayOfWeek; import java.util.*;
public interface AvailabilityRepository extends JpaRepository<DoctorAvailability,Long>{List<DoctorAvailability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId); List<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId,DayOfWeek day); void deleteByDoctorId(Long doctorId);}
