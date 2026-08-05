package com.errorwiky.auth;

import com.errorwiky.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final AuthenticationManager authenticationManager;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();
    private final boolean googleEnabled;

    public AuthController(AuthService authService, CurrentUserService currentUserService,
                          AuthenticationManager authenticationManager,
                          @Value("${spring.security.oauth2.client.registration.google.client-id:local-disabled}") String googleClientId) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.authenticationManager = authenticationManager;
        this.googleEnabled = googleClientId != null && !googleClientId.isBlank() && !"local-disabled".equals(googleClientId);
    }

    @PostMapping("/signup")
    ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok(authService.signup(request)));
    }

    @PostMapping("/login")
    ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request,
                                    HttpServletRequest servletRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.loginId(), request.password()));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, servletRequest, response);
        return ApiResponse.ok(UserResponse.from(currentUserService.require(authentication)));
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        SecurityContextHolder.clearContext();
        return ApiResponse.ok("로그아웃되었습니다.");
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> me(Authentication authentication) {
        return ApiResponse.ok(UserResponse.from(currentUserService.require(authentication)));
    }

    @GetMapping("/config")
    ApiResponse<Map<String, Boolean>> config() {
        return ApiResponse.ok(Map.of("googleEnabled", googleEnabled));
    }
}
