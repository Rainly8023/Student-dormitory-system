package com.dorm.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fees", uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "billing_month"}))
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "billing_month", nullable = false, length = 7)
    private String billingMonth;

    @Column(name = "water_usage", precision = 10, scale = 2)
    private BigDecimal waterUsage = BigDecimal.ZERO;

    @Column(name = "water_fee", precision = 10, scale = 2)
    private BigDecimal waterFee = BigDecimal.ZERO;

    @Column(name = "electricity_usage", precision = 10, scale = 2)
    private BigDecimal electricityUsage = BigDecimal.ZERO;

    @Column(name = "electricity_fee", precision = 10, scale = 2)
    private BigDecimal electricityFee = BigDecimal.ZERO;

    @Column(name = "other_fee", precision = 10, scale = 2)
    private BigDecimal otherFee = BigDecimal.ZERO;

    @Column(name = "total_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.UNPAID;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Status { UNPAID, PAID, OVERDUE }
}
