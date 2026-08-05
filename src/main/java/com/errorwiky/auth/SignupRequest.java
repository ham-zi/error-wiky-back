package com.errorwiky.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_]{4,30}$", message = "영문, 숫자, 밑줄 4~30자여야 합니다.") String loginId,
        @NotBlank @Size(min = 2, max = 30) String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100) String password
) {}
