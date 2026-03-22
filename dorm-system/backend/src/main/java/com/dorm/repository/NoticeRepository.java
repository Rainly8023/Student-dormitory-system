package com.dorm.repository;

import com.dorm.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
        SELECT n FROM Notice n LEFT JOIN FETCH n.author
        ORDER BY n.isPinned DESC, n.createdAt DESC
        """)
    Page<Notice> findAllWithAuthor(Pageable pageable);
}
