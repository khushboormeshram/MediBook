package com.medibook.repository;
import com.medibook.entity.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.Optional;
public interface UserRepository extends JpaRepository<AppUser,Long>{Optional<AppUser> findByEmail(String email); long countByRole(Role role);}
