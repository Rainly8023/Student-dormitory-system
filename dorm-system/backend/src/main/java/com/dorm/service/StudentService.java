package com.dorm.service;

import com.dorm.dto.PageResult;
import com.dorm.entity.Room;
import com.dorm.entity.Student;
import com.dorm.entity.User;
import com.dorm.repository.RoomRepository;
import com.dorm.repository.StudentRepository;
import com.dorm.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResult<Map<String, Object>> list(String keyword, String status, Long roomId, int page, int size) {
        Student.Status statusEnum = status != null ? Student.Status.valueOf(status) : Student.Status.ACTIVE;
        var pageable = PageRequest.of(page - 1, size);
        var pageData = studentRepository.findByFilter(statusEnum, keyword, roomId, pageable);
        var records = pageData.getContent().stream().map(this::toMap).collect(Collectors.toList());
        return new PageResult<>(records, pageData.getTotalElements(), page, size, pageData.getTotalPages());
    }

    public Map<String, Object> getOne(Long id) {
        Student s = studentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        return toMap(s);
    }

    @Transactional
    public Student create(StudentDto dto) {
        if (studentRepository.existsByStudentNo(dto.getStudentNo())) {
            throw new IllegalArgumentException("学号已存在");
        }
        Student s = new Student();
        fillStudent(s, dto);
        s.setStatus(Student.Status.ACTIVE);

        // 自动创建登录账号
        if (!userRepository.existsByUsername(dto.getStudentNo())) {
            User u = new User();
            u.setUsername(dto.getStudentNo());
            u.setPassword(passwordEncoder.encode("student123"));
            u.setRole(User.Role.STUDENT);
            u.setRealName(dto.getRealName());
            u.setPhone(dto.getPhone());
            u.setIsActive(true);
            u = userRepository.save(u);
            s.setUser(u);
        }

        Student saved = studentRepository.save(s);
        if (dto.getRoomId() != null) {
            updateRoomCount(dto.getRoomId(), 1);
        }
        return saved;
    }

    @Transactional
    public Student update(Long id, StudentDto dto) {
        Student s = studentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        fillStudent(s, dto);
        return studentRepository.save(s);
    }

    @Transactional
    public void checkout(Long id, String checkOutDate) {
        Student s = studentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        if (s.getRoom() != null) {
            updateRoomCount(s.getRoom().getId(), -1);
        }
        s.setStatus(Student.Status.CHECKED_OUT);
        s.setCheckOutDate(LocalDate.parse(checkOutDate != null ? checkOutDate : LocalDate.now().toString()));
        s.setRoom(null);
        studentRepository.save(s);
    }

    @Transactional
    public void transfer(Long id, Long newRoomId, Integer bedNumber) {
        Student s = studentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        Room newRoom = roomRepository.findById(newRoomId)
            .orElseThrow(() -> new IllegalArgumentException("目标房间不存在"));
        if (newRoom.getCurrentCount() >= newRoom.getCapacity()) {
            throw new IllegalArgumentException("目标房间已满");
        }
        if (s.getRoom() != null) {
            updateRoomCount(s.getRoom().getId(), -1);
        }
        s.setRoom(newRoom);
        s.setBedNumber(bedNumber);
        studentRepository.save(s);
        updateRoomCount(newRoomId, 1);
    }

    private void fillStudent(Student s, StudentDto dto) {
        s.setStudentNo(dto.getStudentNo());
        s.setRealName(dto.getRealName());
        s.setGender(Student.Gender.valueOf(dto.getGender()));
        s.setMajor(dto.getMajor());
        s.setClassName(dto.getClassName());
        s.setPhone(dto.getPhone());
        s.setBedNumber(dto.getBedNumber());
        if (dto.getCheckInDate() != null) s.setCheckInDate(LocalDate.parse(dto.getCheckInDate()));
        if (dto.getRoomId() != null) {
            s.setRoom(roomRepository.findById(dto.getRoomId()).orElse(null));
        }
    }

    private void updateRoomCount(Long roomId, int delta) {
        roomRepository.findById(roomId).ifPresent(room -> {
            int newCount = Math.max(0, room.getCurrentCount() + delta);
            room.setCurrentCount(newCount);
            room.setStatus(newCount >= room.getCapacity() ? Room.Status.FULL
                : room.getStatus() == Room.Status.FULL ? Room.Status.AVAILABLE : room.getStatus());
            roomRepository.save(room);
        });
    }

    private Map<String, Object> toMap(Student s) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("studentNo", s.getStudentNo());
        m.put("realName", s.getRealName());
        m.put("gender", s.getGender().name());
        m.put("major", s.getMajor());
        m.put("className", s.getClassName());
        m.put("phone", s.getPhone());
        m.put("bedNumber", s.getBedNumber());
        m.put("checkInDate", s.getCheckInDate());
        m.put("checkOutDate", s.getCheckOutDate());
        m.put("status", s.getStatus().name());
        m.put("roomId", s.getRoom() != null ? s.getRoom().getId() : null);
        m.put("roomNumber", s.getRoom() != null ? s.getRoom().getRoomNumber() : null);
        m.put("buildingName", s.getRoom() != null && s.getRoom().getBuilding() != null ? s.getRoom().getBuilding().getName() : null);
        return m;
    }

    @Data
    public static class StudentDto {
        private String studentNo;
        private String realName;
        private String gender;
        private String major;
        private String className;
        private String phone;
        private Long roomId;
        private Integer bedNumber;
        private String checkInDate;
    }

    @Data
    public static class TransferDto {
        private Long newRoomId;
        private Integer bedNumber;
    }
}
