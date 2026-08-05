package com.errorwiky.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByProviderAndProviderId(AuthProvider provider, String providerId);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
}
