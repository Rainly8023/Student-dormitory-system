package com.dorm.service;

import com.dorm.dto.PageResult;
import com.dorm.entity.*;
import com.dorm.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RepairService {

    private final RepairRepository repairRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public PageResult<Map<String, Object>> list(String status, String type, Long studentId, int page, int size) {
        Repair.Status s = status != null ? Repair.Status.valueOf(status) : null;
        Repair.RepairType t = type != null ? Repair.RepairType.valueOf(type) : null;
        var paged = repairRepository.findByFilter(s, t, studentId, PageRequest.of(page - 1, size));
        return PageResult.of(paged.map(this::toMap));
    }

    public Repair create(RepairDto dto) {
        Repair r = new Repair();
        r.setRoom(roomRepository.findById(dto.getRoomId()).orElseThrow(() -> new IllegalArgumentException("房间不存在")));
        if (dto.getStudentId() != null) r.setStudent(studentRepository.findById(dto.getStudentId()).orElse(null));
        r.setTitle(dto.getTitle());
        r.setDescription(dto.getDescription());
        r.setRepairType(Repair.RepairType.valueOf(dto.getRepairType()));
        r.setPriority(dto.getPriority() != null ? Repair.Priority.valueOf(dto.getPriority()) : Repair.Priority.NORMAL);
        r.setStatus(Repair.Status.PENDING);
        return repairRepository.save(r);
    }

    @Transactional
    public Repair update(Long id, UpdateDto dto, Long handlerId) {
        Repair r = repairRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("报修记录不存在"));
        r.setStatus(Repair.Status.valueOf(dto.getStatus()));
        r.setHandlerNote(dto.getHandlerNote());
        r.setHandler(userRepository.findById(handlerId).orElse(null));
        if (dto.getStatus().equals("RESOLVED")) r.setResolvedAt(LocalDateTime.now());
        return repairRepository.save(r);
    }

    private Map<String, Object> toMap(Repair r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("title", r.getTitle());
        m.put("description", r.getDescription());
        m.put("repairType", r.getRepairType().name());
        m.put("status", r.getStatus().name());
        m.put("priority", r.getPriority().name());
        m.put("roomId", r.getRoom().getId());
        m.put("roomNumber", r.getRoom().getRoomNumber());
        m.put("buildingName", r.getRoom().getBuilding() != null ? r.getRoom().getBuilding().getName() : null);
        m.put("studentId", r.getStudent() != null ? r.getStudent().getId() : null);
        m.put("studentName", r.getStudent() != null ? r.getStudent().getRealName() : null);
        m.put("handlerName", r.getHandler() != null ? r.getHandler().getRealName() : null);
        m.put("handlerNote", r.getHandlerNote());
        m.put("createdAt", r.getCreatedAt());
        m.put("resolvedAt", r.getResolvedAt());
        return m;
    }

    @Data public static class RepairDto { private Long roomId; private Long studentId; private String title; private String description; private String repairType; private String priority; }
    @Data public static class UpdateDto { private String status; private String handlerNote; }
}
