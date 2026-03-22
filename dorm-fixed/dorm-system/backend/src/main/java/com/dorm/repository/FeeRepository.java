package com.dorm.repository;

import com.dorm.entity.Fee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeeRepository extends JpaRepository<Fee, Long> {

    boolean existsByRoomIdAndBillingMonth(Long roomId, String billingMonth);

    @Query("""
        SELECT f FROM Fee f
        JOIN FETCH f.room r
        JOIN FETCH r.building
        WHERE (:status IS NULL OR f.status = :status)
          AND (:month IS NULL OR f.billingMonth = :month)
          AND (:roomId IS NULL OR f.room.id = :roomId)
        ORDER BY f.billingMonth DESC, r.id
        """)
    Page<Fee> findByFilter(
        @Param("status") Fee.Status status,
        @Param("month") String month,
        @Param("roomId") Long roomId,
        Pageable pageable
    );

    long countByStatus(Fee.Status status);
}
