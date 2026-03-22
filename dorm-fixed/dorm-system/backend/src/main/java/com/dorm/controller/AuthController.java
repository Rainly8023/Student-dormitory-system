package com.dorm.controller;

import com.dorm.dto.AuthDto;
import com.dorm.dto.Result;
import com.dorm.entity.User;
import com.dorm.repository.UserRepository;
import com.dorm.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @GetMapping("/profile")
    public Result<?> profile(Authentication auth) {
        return Result.success(auth.getPrincipal());
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@Valid @RequestBody AuthDto.ChangePasswordRequest req, Authentication auth) {
        authService.changePassword((Long) auth.getPrincipal(), req);
        return Result.success();
    }
}
