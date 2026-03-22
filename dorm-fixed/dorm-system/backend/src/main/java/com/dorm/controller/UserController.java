package com.dorm.controller;

import com.dorm.dto.Result;
import com.dorm.entity.User;
import com.dorm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<?> list() {
        var users = userRepository.findAll().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("role", u.getRole().name());
            m.put("realName", u.getRealName());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("isActive", u.getIsActive());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList();
        return Result.success(users);
    }

    @PostMapping
    public Result<?> create(@RequestBody Map<String, String> body) {
        if (userRepository.existsByUsername(body.get("username"))) {
            throw new IllegalArgumentException("用户名已存在");
        }
        User u = new User();
        u.setUsername(body.get("username"));
        u.setPassword(passwordEncoder.encode(body.getOrDefault("password", "123456")));
        u.setRole(User.Role.valueOf(body.get("role")));
        u.setRealName(body.get("realName"));
        u.setPhone(body.get("phone"));
        u.setEmail(body.get("email"));
        u.setIsActive(true);
        userRepository.save(u);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        User u = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (body.get("role") != null) u.setRole(User.Role.valueOf(body.get("role").toString()));
        if (body.get("realName") != null) u.setRealName(body.get("realName").toString());
        if (body.get("phone") != null) u.setPhone(body.get("phone").toString());
        if (body.get("isActive") != null) u.setIsActive((Boolean) body.get("isActive"));
        userRepository.save(u);
        return Result.success();
    }
}
