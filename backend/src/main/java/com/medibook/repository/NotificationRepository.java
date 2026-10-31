package com.medibook.repository;
import com.medibook.entity.Notification; import org.springframework.data.jpa.repository.JpaRepository; import java.util.List;
public interface NotificationRepository extends JpaRepository<Notification,Long>{List<Notification> findTop50ByUserIdOrderByCreatedAtDesc(Long userId);boolean existsByUserIdAndTitleAndMessageAndCreatedAtAfter(Long userId,String title,String message,java.time.Instant after);}
