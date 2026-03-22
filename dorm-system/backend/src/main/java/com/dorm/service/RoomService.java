package com.dorm.service;

import com.dorm.dto.PageResult;
import com.dorm.entity.Building;
import com.dorm.entity.Room;
import com.dorm.entity.Student;
import com.dorm.repository.BuildingRepository;
import com.dorm.repository.RoomRepository;
import com.dorm.repository.StudentRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BuildingRepository buildingRepository;
    private final StudentRepository studentRepository;

    public PageResult<Map<String, Object>> list(Long buildingId, String status, int page, int size) {
        Room.Status statusEnum = status != null ? Room.Status.valueOf(status) : null;
        var pageable = PageRequest.of(page - 1, size);
        var pageData = roomRepository.findByFilter(buildingId, statusEnum, pageable);
        var records = pageData.getContent().stream().map(this::toMap).collect(Collectors.toList());
        return new PageResult<>(records, pageData.getTotalElements(), page, size, pageData.getTotalPages());
    }

    public Map<String, Object> detail(Long id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("房间不存在"));
        Map<String, Object> map = toMap(room);
        List<Student> students = studentRepository.findByFilter(Student.Status.ACTIVE, null, id, PageRequest.of(0, 20)).getContent();
        map.put("students", students.stream().map(s -> {
            Map<String, Object> sm = new java.util.LinkedHashMap<>();
            sm.put("id", s.getId());
            sm.put("studentNo", s.getStudentNo());
            sm.put("realName", s.getRealName());
            sm.put("gender", s.getGender());
            sm.put("major", s.getMajor());
            sm.put("phone", s.getPhone());
            sm.put("bedNumber", s.getBedNumber());
            sm.put("checkInDate", s.getCheckInDate());
            return sm;
        }).collect(Collectors.toList()));
        return map;
    }

    public List<Map<String, Object>> listAvailable() {
        return roomRepository.findAvailableRooms().stream().map(this::toMap).collect(Collectors.toList());
    }

    public Room save(RoomDto dto) {
        Room room = dto.getId() != null
            ? roomRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("房间不存在"))
            : new Room();
        Building building = buildingRepository.findById(dto.getBuildingId())
            .orElseThrow(() -> new IllegalArgumentException("楼栋不存在"));
        room.setBuilding(building);
        room.setFloor(dto.getFloor());
        room.setRoomNumber(dto.getRoomNumber());
        room.setCapacity(dto.getCapacity() != null ? dto.getCapacity() : 4);
        room.setRoomType(dto.getRoomType() != null ? dto.getRoomType() : "标准间");
        room.setStatus(dto.getStatus() != null ? Room.Status.valueOf(dto.getStatus()) : Room.Status.AVAILABLE);
        return roomRepository.save(room);
    }

    private Map<String, Object> toMap(Room r) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", r.getId());
        m.put("buildingId", r.getBuilding().getId());
        m.put("buildingName", r.getBuilding().getName());
        m.put("floor", r.getFloor());
        m.put("roomNumber", r.getRoomNumber());
        m.put("capacity", r.getCapacity());
        m.put("currentCount", r.getCurrentCount());
        m.put("roomType", r.getRoomType());
        m.put("status", r.getStatus().name());
        return m;
    }

    @Data
    public static class RoomDto {
        private Long id;
        private Long buildingId;
        private Integer floor;
        private String roomNumber;
        private Integer capacity;
        private String roomType;
        private String status;
    }
}
