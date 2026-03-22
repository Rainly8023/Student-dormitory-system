package com.dorm.service;

import com.dorm.dto.PageResult;
import com.dorm.entity.*;
import com.dorm.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorRepository visitorRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public PageResult<Map<String, Object>> list(String status, String date, Long studentId, int page, int size) {
        Visitor.Status s = status != null ? Visitor.Status.valueOf(status) : null;
        LocalDate d = date != null ? LocalDate.parse(date) : null;
        var paged = visitorRepository.findByFilter(s, d, studentId, PageRequest.of(page - 1, size));
        return PageResult.of(paged.map(this::toMap));
    }

    public Visitor create(VisitorDto dto, Long registeredById) {
        Visitor v = new Visitor();
        v.setStudent(studentRepository.findById(dto.getStudentId()).orElseThrow(() -> new IllegalArgumentException("学生不存在")));
        v.setVisitorName(dto.getVisitorName());
        v.setVisitorPhone(dto.getVisitorPhone());
        v.setVisitorIdCard(dto.getVisitorIdCard());
        v.setVisitDate(LocalDate.parse(dto.getVisitDate()));
        v.setVisitTime(dto.getVisitTime());
        v.setPurpose(dto.getPurpose());
        v.setStatus(Visitor.Status.VISITING);
        v.setRegisteredBy(userRepository.findById(registeredById).orElse(null));
        return visitorRepository.save(v);
    }

    public Visitor update(Long id, String status, String leaveTime) {
        Visitor v = visitorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("访客记录不存在"));
        v.setStatus(Visitor.Status.valueOf(status));
        if (leaveTime != null) v.setLeaveTime(leaveTime);
        return visitorRepository.save(v);
    }

    private Map<String, Object> toMap(Visitor v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", v.getId());
        m.put("visitorName", v.getVisitorName());
        m.put("visitorPhone", v.getVisitorPhone());
        m.put("visitorIdCard", v.getVisitorIdCard());
        m.put("visitDate", v.getVisitDate());
        m.put("visitTime", v.getVisitTime());
        m.put("leaveTime", v.getLeaveTime());
        m.put("purpose", v.getPurpose());
        m.put("status", v.getStatus().name());
        m.put("studentId", v.getStudent().getId());
        m.put("studentName", v.getStudent().getRealName());
        m.put("roomNumber", v.getStudent().getRoom() != null ? v.getStudent().getRoom().getRoomNumber() : null);
        m.put("buildingName", v.getStudent().getRoom() != null && v.getStudent().getRoom().getBuilding() != null ? v.getStudent().getRoom().getBuilding().getName() : null);
        m.put("createdAt", v.getCreatedAt());
        return m;
    }

    @Data public static class VisitorDto { private Long studentId; private String visitorName; private String visitorPhone; private String visitorIdCard; private String visitDate; private String visitTime; private String purpose; }
}
