package com.dorm.repository;

import com.dorm.entity.Visitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    @Query("""
        SELECT v FROM Visitor v
        JOIN FETCH v.student s
        LEFT JOIN FETCH s.room r
        LEFT JOIN FETCH r.building
        WHERE (:status IS NULL OR v.status = :status)
          AND (:date IS NULL OR v.visitDate = :date)
          AND (:studentId IS NULL OR v.student.id = :studentId)
        ORDER BY v.createdAt DESC
        """)
    Page<Visitor> findByFilter(
        @Param("status") Visitor.Status status,
        @Param("date") LocalDate date,
        @Param("studentId") Long studentId,
        Pageable pageable
    );

    long countByVisitDateAndStatus(LocalDate date, Visitor.Status status);

    long countByStatus(Visitor.Status status);
}
