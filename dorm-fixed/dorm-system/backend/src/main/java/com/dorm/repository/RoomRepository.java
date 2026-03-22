package com.dorm.repository;

import com.dorm.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("""
        SELECT r FROM Room r JOIN FETCH r.building b
        WHERE (:buildingId IS NULL OR b.id = :buildingId)
          AND (:status IS NULL OR r.status = :status)
        ORDER BY b.id, r.floor, r.roomNumber
        """)
    Page<Room> findByFilter(
        @Param("buildingId") Long buildingId,
        @Param("status") Room.Status status,
        Pageable pageable
    );

    @Query("SELECT r FROM Room r JOIN FETCH r.building WHERE r.status = 'AVAILABLE' AND r.currentCount < r.capacity ORDER BY r.id")
    List<Room> findAvailableRooms();

    long countByBuildingId(Long buildingId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.building.id = :bid AND r.status = 'AVAILABLE'")
    long countAvailableByBuilding(@Param("bid") Long buildingId);
}
