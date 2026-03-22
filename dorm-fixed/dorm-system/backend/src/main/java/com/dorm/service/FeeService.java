package com.dorm.service;

import com.dorm.dto.PageResult;
import com.dorm.entity.*;
import com.dorm.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeRepository feeRepository;
    private final RoomRepository roomRepository;

    public PageResult<Map<String, Object>> list(String status, String month, Long roomId, int page, int size) {
        Fee.Status s = status != null ? Fee.Status.valueOf(status) : null;
        var paged = feeRepository.findByFilter(s, month, roomId, PageRequest.of(page - 1, size));
        return PageResult.of(paged.map(this::toMap));
    }

    public Fee create(FeeDto dto) {
        if (feeRepository.existsByRoomIdAndBillingMonth(dto.getRoomId(), dto.getBillingMonth())) {
            throw new IllegalArgumentException("该房间该月账单已存在");
        }
        Fee f = new Fee();
        f.setRoom(roomRepository.findById(dto.getRoomId()).orElseThrow(() -> new IllegalArgumentException("房间不存在")));
        f.setBillingMonth(dto.getBillingMonth());
        BigDecimal waterUsage = BigDecimal.valueOf(dto.getWaterUsage() != null ? dto.getWaterUsage() : 0);
        BigDecimal elecUsage  = BigDecimal.valueOf(dto.getElectricityUsage() != null ? dto.getElectricityUsage() : 0);
        BigDecimal other      = BigDecimal.valueOf(dto.getOtherFee() != null ? dto.getOtherFee() : 0);
        BigDecimal waterFee   = waterUsage.multiply(BigDecimal.valueOf(2.0));
        BigDecimal elecFee    = elecUsage.multiply(BigDecimal.valueOf(0.5));
        f.setWaterUsage(waterUsage); f.setWaterFee(waterFee);
        f.setElectricityUsage(elecUsage); f.setElectricityFee(elecFee);
        f.setOtherFee(other);
        f.setTotalFee(waterFee.add(elecFee).add(other));
        f.setStatus(Fee.Status.UNPAID);
        return feeRepository.save(f);
    }

    public Fee pay(Long id) {
        Fee f = feeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("账单不存在"));
        f.setStatus(Fee.Status.PAID);
        f.setPaidAt(LocalDateTime.now());
        return feeRepository.save(f);
    }

    private Map<String, Object> toMap(Fee f) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", f.getId());
        m.put("roomId", f.getRoom().getId());
        m.put("roomNumber", f.getRoom().getRoomNumber());
        m.put("buildingName", f.getRoom().getBuilding() != null ? f.getRoom().getBuilding().getName() : null);
        m.put("billingMonth", f.getBillingMonth());
        m.put("waterUsage", f.getWaterUsage());
        m.put("waterFee", f.getWaterFee());
        m.put("electricityUsage", f.getElectricityUsage());
        m.put("electricityFee", f.getElectricityFee());
        m.put("otherFee", f.getOtherFee());
        m.put("totalFee", f.getTotalFee());
        m.put("status", f.getStatus().name());
        m.put("paidAt", f.getPaidAt());
        m.put("createdAt", f.getCreatedAt());
        return m;
    }

    @Data public static class FeeDto { private Long roomId; private String billingMonth; private Double waterUsage; private Double electricityUsage; private Double otherFee; }
}
