package com.dorm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @NotBlank(message = "用户名不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private String tokenType = "Bearer";
        private UserInfo user;
    }

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String role;
        private String realName;
        private String phone;
        private String email;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
