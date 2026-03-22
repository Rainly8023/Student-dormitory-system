package com.dorm.service;

import com.dorm.dto.PageResult;
import com.dorm.entity.*;
import com.dorm.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

// ==================== RepairService ====================
@Service
@RequiredArgsConstructor
class RepairService {

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

// ==================== VisitorService ====================
@Service
@RequiredArgsConstructor
class VisitorService {

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

// ==================== FeeService ====================
@Service
@RequiredArgsConstructor
class FeeService {

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

// ==================== NoticeService ====================
@Service
@RequiredArgsConstructor
class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public PageResult<Map<String, Object>> list(int page, int size) {
        var paged = noticeRepository.findAllWithAuthor(PageRequest.of(page - 1, size));
        return PageResult.of(paged.map(this::toMap));
    }

    public Notice create(NoticeDto dto, Long authorId) {
        Notice n = new Notice();
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setCategory(dto.getCategory() != null ? dto.getCategory() : "NOTICE");
        n.setIsPinned(Boolean.TRUE.equals(dto.getIsPinned()));
        n.setAuthor(userRepository.findById(authorId).orElse(null));
        return noticeRepository.save(n);
    }

    public Notice update(Long id, NoticeDto dto) {
        Notice n = noticeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("公告不存在"));
        n.setTitle(dto.getTitle());
        n.setContent(dto.getContent());
        n.setCategory(dto.getCategory());
        n.setIsPinned(Boolean.TRUE.equals(dto.getIsPinned()));
        n.setUpdatedAt(LocalDateTime.now());
        return noticeRepository.save(n);
    }

    public void delete(Long id) {
        noticeRepository.deleteById(id);
    }

    private Map<String, Object> toMap(Notice n) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", n.getId());
        m.put("title", n.getTitle());
        m.put("content", n.getContent());
        m.put("category", n.getCategory());
        m.put("isPinned", n.getIsPinned());
        m.put("authorName", n.getAuthor() != null ? n.getAuthor().getRealName() : null);
        m.put("createdAt", n.getCreatedAt());
        m.put("updatedAt", n.getUpdatedAt());
        return m;
    }

    @Data public static class NoticeDto { private String title; private String content; private String category; private Boolean isPinned; }
}

// ==================== StatsService ====================
@Service
@RequiredArgsConstructor
class StatsService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final RepairRepository repairRepository;
    private final FeeRepository feeRepository;
    private final VisitorRepository visitorRepository;
    private final BuildingRepository buildingRepository;

    public Map<String, Object> overview() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalStudents",   studentRepository.countByStatus(Student.Status.ACTIVE));
        m.put("totalRooms",      roomRepository.count());
        m.put("availableRooms",  roomRepository.findAvailableRooms().size());
        m.put("totalBuildings",  buildingRepository.count());
        m.put("pendingRepairs",  repairRepository.countByStatus(Repair.Status.PENDING));
        m.put("processingRepairs", repairRepository.countByStatus(Repair.Status.PROCESSING));
        m.put("unpaidFees",      feeRepository.countByStatus(Fee.Status.UNPAID));
        m.put("visitingNow",     visitorRepository.countByStatus(Visitor.Status.VISITING));
        m.put("todayVisitors",   visitorRepository.countByVisitDateAndStatus(LocalDate.now(), Visitor.Status.VISITING)
                                 + visitorRepository.countByVisitDateAndStatus(LocalDate.now(), Visitor.Status.LEFT));
        return m;
    }
}
