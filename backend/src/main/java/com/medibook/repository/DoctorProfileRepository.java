package com.medibook.repository;
import com.medibook.entity.DoctorProfile; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.*;
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile,Long>, JpaSpecificationExecutor<DoctorProfile>{
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select d from DoctorProfile d where d.userId=:id") Optional<DoctorProfile> lockById(@Param("id") Long id);
}
