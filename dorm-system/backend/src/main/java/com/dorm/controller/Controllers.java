package com.dorm.controller;

import com.dorm.dto.AuthDto;
import com.dorm.dto.Result;
import com.dorm.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// ==================== AuthController ====================
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @GetMapping("/profile")
    public Result<?> profile(Authentication auth) {
        Long uid = (Long) auth.getPrincipal();
        return Result.success(uid);
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@Valid @RequestBody AuthDto.ChangePasswordRequest req, Authentication auth) {
        authService.changePassword((Long) auth.getPrincipal(), req);
        return Result.success();
    }
}

// ==================== BuildingController ====================
@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
class BuildingController {
    private final BuildingService buildingService;

    @GetMapping
    public Result<?> list() {
        return Result.success(buildingService.listAll());
    }

    @PostMapping
    public Result<?> create(@RequestBody BuildingService.BuildingDto dto) {
        return Result.success(buildingService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody BuildingService.BuildingDto dto) {
        dto.setId(id);
        return Result.success(buildingService.save(dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        buildingService.delete(id);
        return Result.success();
    }
}

// ==================== RoomController ====================
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
class RoomController {
    private final RoomService roomService;

    @GetMapping
    public Result<?> list(
        @RequestParam(required = false) Long buildingId,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "1")  int page,
        @RequestParam(defaultValue = "20") int size) {
        return Result.success(roomService.list(buildingId, status, page, size));
    }

    @GetMapping("/available")
    public Result<?> available() {
        return Result.success(roomService.listAvailable());
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        return Result.success(roomService.detail(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody RoomService.RoomDto dto) {
        return Result.success(roomService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody RoomService.RoomDto dto) {
        dto.setId(id);
        return Result.success(roomService.save(dto));
    }
}

// ==================== StudentController ====================
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
class StudentController {
    private final StudentService studentService;

    @GetMapping
    public Result<?> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long roomId,
        @RequestParam(defaultValue = "1")  int page,
        @RequestParam(defaultValue = "20") int size) {
        return Result.success(studentService.list(keyword, status, roomId, page, size));
    }

    @GetMapping("/{id}")
    public Result<?> getOne(@PathVariable Long id) {
        return Result.success(studentService.getOne(id));
    }

    @PostMapping
    public Result<?> create(@RequestBody StudentService.StudentDto dto) {
        return Result.success(studentService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody StudentService.StudentDto dto) {
        return Result.success(studentService.update(id, dto));
    }

    @PostMapping("/{id}/checkout")
    public Result<?> checkout(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, String> body) {
        String date = body != null ? body.get("checkOutDate") : null;
        studentService.checkout(id, date);
        return Result.success();
    }

    @PostMapping("/{id}/transfer")
    public Result<?> transfer(@PathVariable Long id, @RequestBody StudentService.TransferDto dto) {
        studentService.transfer(id, dto.getNewRoomId(), dto.getBedNumber());
        return Result.success();
    }
}

// ==================== RepairController ====================
@RestController
@RequestMapping("/repairs")
@RequiredArgsConstructor
class RepairController {
    private final RepairService repairService;

    @GetMapping
    public Result<?> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String repairType,
        @RequestParam(required = false) Long studentId,
        @RequestParam(defaultValue = "1")  int page,
        @RequestParam(defaultValue = "20") int size) {
        return Result.success(repairService.list(status, repairType, studentId, page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody RepairService.RepairDto dto) {
        return Result.success(repairService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody RepairService.UpdateDto dto, Authentication auth) {
        return Result.success(repairService.update(id, dto, (Long) auth.getPrincipal()));
    }
}

// ==================== VisitorController ====================
@RestController
@RequestMapping("/visitors")
@RequiredArgsConstructor
class VisitorController {
    private final VisitorService visitorService;

    @GetMapping
    public Result<?> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String date,
        @RequestParam(required = false) Long studentId,
        @RequestParam(defaultValue = "1")  int page,
        @RequestParam(defaultValue = "20") int size) {
        return Result.success(visitorService.list(status, date, studentId, page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody VisitorService.VisitorDto dto, Authentication auth) {
        return Result.success(visitorService.create(dto, (Long) auth.getPrincipal()));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        return Result.success(visitorService.update(id, body.get("status"), body.get("leaveTime")));
    }
}

// ==================== FeeController ====================
@RestController
@RequestMapping("/fees")
@RequiredArgsConstructor
class FeeController {
    private final FeeService feeService;

    @GetMapping
    public Result<?> list(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String month,
        @RequestParam(required = false) Long roomId,
        @RequestParam(defaultValue = "1")  int page,
        @RequestParam(defaultValue = "20") int size) {
        return Result.success(feeService.list(status, month, roomId, page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody FeeService.FeeDto dto) {
        return Result.success(feeService.create(dto));
    }

    @PostMapping("/{id}/pay")
    public Result<?> pay(@PathVariable Long id) {
        return Result.success(feeService.pay(id));
    }
}

// ==================== NoticeController ====================
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
class NoticeController {
    private final NoticeService noticeService;

    @GetMapping
    public Result<?> list(
        @RequestParam(defaultValue = "1")  int page,
        @RequestParam(defaultValue = "10") int size) {
        return Result.success(noticeService.list(page, size));
    }

    @PostMapping
    public Result<?> create(@RequestBody NoticeService.NoticeDto dto, Authentication auth) {
        return Result.success(noticeService.create(dto, (Long) auth.getPrincipal()));
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody NoticeService.NoticeDto dto) {
        return Result.success(noticeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.success();
    }
}

// ==================== StatsController ====================
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
class StatsController {
    private final StatsService statsService;

    @GetMapping("/overview")
    public Result<?> overview() {
        return Result.success(statsService.overview());
    }
}

// ==================== UserController ====================
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {
    private final com.dorm.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<?> list() {
        var users = userRepository.findAll().stream().map(u -> {
            var m = new java.util.LinkedHashMap<String, Object>();
            m.put("id", u.getId()); m.put("username", u.getUsername());
            m.put("role", u.getRole().name()); m.put("realName", u.getRealName());
            m.put("phone", u.getPhone()); m.put("email", u.getEmail());
            m.put("isActive", u.getIsActive()); m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();
        return Result.success(users);
    }

    @PostMapping
    public Result<?> create(@RequestBody java.util.Map<String, String> body) {
        if (userRepository.existsByUsername(body.get("username"))) throw new IllegalArgumentException("用户名已存在");
        var u = new com.dorm.entity.User();
        u.setUsername(body.get("username"));
        u.setPassword(passwordEncoder.encode(body.getOrDefault("password", "123456")));
        u.setRole(com.dorm.entity.User.Role.valueOf(body.get("role")));
        u.setRealName(body.get("realName")); u.setPhone(body.get("phone")); u.setEmail(body.get("email")); u.setIsActive(true);
        userRepository.save(u);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        var u = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (body.get("role") != null) u.setRole(com.dorm.entity.User.Role.valueOf(body.get("role").toString()));
        if (body.get("realName") != null) u.setRealName(body.get("realName").toString());
        if (body.get("phone") != null) u.setPhone(body.get("phone").toString());
        if (body.get("isActive") != null) u.setIsActive((Boolean) body.get("isActive"));
        userRepository.save(u);
        return Result.success();
    }
}
