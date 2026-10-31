package com.medibook.repository;
import com.medibook.entity.Specialty; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface SpecialtyRepository extends JpaRepository<Specialty,Long>{Optional<Specialty> findByNameIgnoreCase(String name);}
