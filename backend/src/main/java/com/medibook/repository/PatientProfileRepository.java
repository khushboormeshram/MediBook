package com.medibook.repository;
import com.medibook.entity.PatientProfile; import org.springframework.data.jpa.repository.JpaRepository;
public interface PatientProfileRepository extends JpaRepository<PatientProfile,Long>{}
