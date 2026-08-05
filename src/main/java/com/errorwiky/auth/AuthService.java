package com.errorwiky.auth;

import com.errorwiky.common.BusinessException;
import com.errorwiky.user.UserEntity;
import com.errorwiky.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    public AuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users; this.encoder = encoder;
    }

    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (users.existsByLoginId(request.loginId()))
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
        if (users.existsByEmail(request.email()))
            throw new BusinessException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
        UserEntity saved = users.save(UserEntity.local(request.loginId(), request.name(), request.email(),
                encoder.encode(request.password())));
        return UserResponse.from(saved);
    }
}
