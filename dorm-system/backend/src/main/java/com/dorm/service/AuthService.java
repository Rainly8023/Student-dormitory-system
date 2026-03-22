package com.dorm.service;

import com.dorm.dto.AuthDto;
import com.dorm.entity.User;
import com.dorm.repository.UserRepository;
import com.dorm.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthDto.LoginResponse login(AuthDto.LoginRequest req) {
        User user = userRepository.findByUsernameAndIsActiveTrue(req.getUsername())
            .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());

        AuthDto.UserInfo info = new AuthDto.UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setRole(user.getRole().name());
        info.setRealName(user.getRealName());
        info.setPhone(user.getPhone());
        info.setEmail(user.getEmail());

        AuthDto.LoginResponse resp = new AuthDto.LoginResponse();
        resp.setToken(token);
        resp.setUser(info);
        return resp;
    }

    public void changePassword(Long userId, AuthDto.ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }
}
