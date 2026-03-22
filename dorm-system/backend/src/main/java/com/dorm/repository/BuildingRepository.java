package com.dorm.repository;

import com.dorm.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BuildingRepository extends JpaRepository<Building, Long> {

    @Query("""
        SELECT b FROM Building b
        LEFT JOIN FETCH b.manager
        ORDER BY b.id
        """)
    List<Building> findAllWithManager();
}
