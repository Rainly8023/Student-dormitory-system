package com.dorm.repository;

import com.dorm.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNo(String studentNo);

    Optional<Student> findByUserIdAndStatus(Long userId, Student.Status status);

    boolean existsByStudentNo(String studentNo);

    @Query("""
        SELECT s FROM Student s
        LEFT JOIN FETCH s.room r
        LEFT JOIN FETCH r.building
        WHERE s.status = :status
          AND (:keyword IS NULL OR s.realName LIKE %:keyword%
               OR s.studentNo LIKE %:keyword%
               OR s.major LIKE %:keyword%)
          AND (:roomId IS NULL OR r.id = :roomId)
        ORDER BY s.id DESC
        """)
    Page<Student> findByFilter(
        @Param("status") Student.Status status,
        @Param("keyword") String keyword,
        @Param("roomId") Long roomId,
        Pageable pageable
    );

    long countByStatus(Student.Status status);

    long countByRoomId(Long roomId);
}
