package com.dorm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rooms", uniqueConstraints = @UniqueConstraint(columnNames = {"building_id", "room_number"}))
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(nullable = false)
    private Integer floor;

    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Column(nullable = false)
    private Integer capacity = 4;

    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0;

    @Column(name = "room_type", length = 30)
    private String roomType = "标准间";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.AVAILABLE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Status {
        AVAILABLE, FULL, MAINTENANCE, CLOSED
    }
}
