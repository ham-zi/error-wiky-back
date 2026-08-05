package com.errorwiky.auth;

import com.errorwiky.user.AuthProvider;
import com.errorwiky.user.UserEntity;
import com.errorwiky.user.UserRepository;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository users;
    public CustomOidcUserService(UserRepository users) { this.users = users; }

    @Override @Transactional
    public OidcUser loadUser(OidcUserRequest request) {
        OidcUser oidc = super.loadUser(request);
        String email = oidc.getEmail();
        String subject = oidc.getSubject();
        String name = oidc.getFullName();
        UserEntity user = users.findByProviderAndProviderId(AuthProvider.GOOGLE, subject)
                .orElseGet(() -> {
                    if (users.existsByEmail(email)) {
                        throw new IllegalStateException("이미 일반 계정으로 가입된 이메일입니다.");
                    }
                    return UserEntity.google(subject, name, email);
                });
        user.updateGoogleProfile(name, email);
        users.save(user);
        return oidc;
    }
}
