package com.dorm.repository;

import com.dorm.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// ==================== UserRepository ====================
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndIsActiveTrue(String username);
    boolean existsByUsername(String username);
}
