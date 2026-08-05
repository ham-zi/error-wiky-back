package com.errorwiky.auth;

import com.errorwiky.common.BusinessException;
import com.errorwiky.user.AuthProvider;
import com.errorwiky.user.UserEntity;
import com.errorwiky.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository users;
    public CurrentUserService(UserRepository users) { this.users = users; }

    public UserEntity require(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserPrincipal local) {
            return users.findById(local.getUserId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        }
        if (principal instanceof OidcUser oidc) {
            return users.findByProviderAndProviderId(AuthProvider.GOOGLE, oidc.getSubject())
                    .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "소셜 사용자를 찾을 수 없습니다."));
        }
        throw new BusinessException(HttpStatus.UNAUTHORIZED, "지원하지 않는 인증 정보입니다.");
    }

    public UserEntity optional(Authentication authentication) {
        try { return require(authentication); } catch (BusinessException e) { return null; }
    }
}
