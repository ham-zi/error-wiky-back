package com.errorwiky.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EW_USERS", uniqueConstraints = {
        @UniqueConstraint(name = "UK_EW_USER_LOGIN", columnNames = "login_id"),
        @UniqueConstraint(name = "UK_EW_USER_EMAIL", columnNames = "email"),
        @UniqueConstraint(name = "UK_EW_USER_PROVIDER", columnNames = {"provider", "provider_id"})
})
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "login_id", nullable = false, length = 80)
    private String loginId;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 180)
    private String email;
    @Column(length = 200)
    private String password;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AuthProvider provider;
    @Column(name = "provider_id", length = 180)
    private String providerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private UserRole role = UserRole.USER;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected UserEntity() {}

    public static UserEntity local(String loginId, String name, String email, String encodedPassword) {
        UserEntity user = new UserEntity();
        user.loginId = loginId;
        user.name = name;
        user.email = email;
        user.password = encodedPassword;
        user.provider = AuthProvider.LOCAL;
        return user;
    }

    public static UserEntity google(String subject, String name, String email) {
        UserEntity user = new UserEntity();
        user.loginId = "google_" + subject.substring(0, Math.min(subject.length(), 32));
        user.name = name == null || name.isBlank() ? "Google 사용자" : name;
        user.email = email;
        user.provider = AuthProvider.GOOGLE;
        user.providerId = subject;
        return user;
    }

    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
    public void updateGoogleProfile(String name, String email) {
        if (name != null && !name.isBlank()) this.name = name;
        if (email != null && !email.isBlank()) this.email = email;
    }

    public Long getId() { return id; }
    public String getLoginId() { return loginId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public AuthProvider getProvider() { return provider; }
    public String getProviderId() { return providerId; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
}
