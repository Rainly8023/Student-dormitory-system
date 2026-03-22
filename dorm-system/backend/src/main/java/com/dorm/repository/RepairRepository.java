package com.dorm.repository;

import com.dorm.entity.Repair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepairRepository extends JpaRepository<Repair, Long> {

    @Query("""
        SELECT rp FROM Repair rp
        LEFT JOIN FETCH rp.room r
        LEFT JOIN FETCH r.building
        LEFT JOIN FETCH rp.student
        LEFT JOIN FETCH rp.handler
        WHERE (:status IS NULL OR rp.status = :status)
          AND (:type IS NULL OR rp.repairType = :type)
          AND (:studentId IS NULL OR rp.student.id = :studentId)
        ORDER BY rp.createdAt DESC
        """)
    Page<Repair> findByFilter(
        @Param("status") Repair.Status status,
        @Param("type") Repair.RepairType type,
        @Param("studentId") Long studentId,
        Pageable pageable
    );

    long countByStatus(Repair.Status status);
}
