package com.errorwiky.ai;

import com.errorwiky.post.ErrorCategory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class AiRecommendService {

    private final AiGateway gateway;
    private final ObjectMapper mapper;

    public AiRecommendService(AiGateway gateway, ObjectMapper mapper) {
        this.gateway = gateway;
        this.mapper = mapper;
    }

    public AiRecommendResponse recommend(AiRecommendRequest request) {
        String prompt = """
                다음 개발 오류를 분석하여 JSON만 반환하세요.

                반드시 아래 형식을 지키세요.
                {"title":"40자 이내 제목","category":"카테고리"}

                category는 반드시 다음 중 하나만 사용하세요.
                LANGUAGE
                FRONTEND
                BACKEND
                DATABASE
                MOBILE
                CLOUD_INFRA
                DEVOPS_CICD
                NETWORK
                SECURITY
                TESTING
                BUILD_DEPENDENCY
                OS_ENVIRONMENT
                VERSION_CONTROL
                AI_DATA
                ETC

                분류 기준:
                - LANGUAGE: Java, Python, JavaScript 등 언어 문법, 컴파일, 런타임 오류
                - FRONTEND: React, Vue, Angular, CSS, 브라우저 오류
                - BACKEND: Spring, Node.js, Django, API, 서버 로직 오류
                - DATABASE: SQL, Oracle, MySQL, PostgreSQL, JPA, ORM 오류
                - MOBILE: Android, iOS, Flutter, React Native 오류
                - CLOUD_INFRA: AWS, Azure, GCP, EC2, S3, DNS, 클라우드 인프라 오류
                - DEVOPS_CICD: Docker, Kubernetes, Nginx, Jenkins, GitHub Actions, 배포 오류
                - NETWORK: HTTP, HTTPS, CORS, 포트, 프록시, 연결 오류
                - SECURITY: 로그인, 인증, 인가, 세션, JWT, OAuth2, 권한 오류
                - TESTING: JUnit, Mockito, Cypress 등 테스트 오류
                - BUILD_DEPENDENCY: Gradle, Maven, npm, 의존성, 빌드 오류
                - OS_ENVIRONMENT: Windows, Linux, 권한, 경로, 환경변수 오류
                - VERSION_CONTROL: Git, GitHub, 브랜치, 병합, commit, push 오류
                - AI_DATA: Ollama, LLM, 머신러닝, AI 모델, 데이터 처리 오류
                - ETC: 위 항목에 명확히 해당하지 않는 오류

                여러 기술이 관련된 경우 직접적인 오류 원인이 발생한 영역을 선택하세요.

                오류 메시지:
                %s

                원인:
                %s

                해결 방법:
                %s
                """
                .formatted(
                        request.errorMessage(),
                        safe(request.cause()),
                        safe(request.solution())
                );

        try {
            String rawResponse = gateway.recommend(prompt);
            JsonNode json = mapper.readTree(rawResponse);

            String title = json.path("title").asText("").trim();
            ErrorCategory category =
                    parseCategory(json.path("category").asText(""));

            if (title.isBlank()) {
                throw new IllegalArgumentException(
                        "AI가 제목을 반환하지 않았습니다."
                );
            }

            return new AiRecommendResponse(
                    limitTitle(title),
                    category,
                    "OLLAMA",
                    null
            );

        } catch (Exception e) {
            String fullText = String.join(
                    " ",
                    safe(request.errorMessage()),
                    safe(request.cause()),
                    safe(request.solution())
            );

            ErrorCategory category = guess(fullText);
            String title = makeTitle(request.errorMessage());

            return new AiRecommendResponse(
                    title,
                    category,
                    "FALLBACK",
                    "Ollama 응답을 처리하지 못해 규칙 기반 추천을 사용했습니다."
            );
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private ErrorCategory parseCategory(String value) {
        if (value == null || value.isBlank()) {
            return ErrorCategory.ETC;
        }

        String normalized = value
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace("-", "_")
                .replace("/", "_")
                .replace(" ", "_");

        try {
            return ErrorCategory.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return ErrorCategory.ETC;
        }
    }

    private ErrorCategory guess(String text) {
        String value = safe(text).toLowerCase(Locale.ROOT);

        // AI 및 데이터
        if (containsAny(
                value,
                "ollama",
                "llm",
                "openai",
                "gemma",
                "deepseek",
                "machine learning",
                "머신러닝",
                "인공지능"
        )) {
            return ErrorCategory.AI_DATA;
        }

        // 버전 관리
        if (containsAny(
                value,
                "git",
                "github",
                "commit",
                "push",
                "pull request",
                "merge",
                "branch",
                "repository"
        )) {
            return ErrorCategory.VERSION_CONTROL;
        }

        // 보안 및 인증
        if (containsAny(
                value,
                "security",
                "authentication",
                "authorization",
                "unauthorized",
                "forbidden",
                "oauth",
                "jwt",
                "session",
                "login",
                "로그인",
                "인증",
                "인가",
                "csrf"
        )) {
            return ErrorCategory.SECURITY;
        }

        // 빌드 및 의존성
        if (containsAny(
                value,
                "gradle",
                "maven",
                "npm",
                "yarn",
                "pnpm",
                "dependency",
                "dependencies",
                "build failed",
                "could not resolve",
                "의존성",
                "빌드 실패"
        )) {
            return ErrorCategory.BUILD_DEPENDENCY;
        }

        // 테스트
        if (containsAny(
                value,
                "junit",
                "mockito",
                "cypress",
                "jest",
                "vitest",
                "test failed",
                "tests failed",
                "테스트 실패"
        )) {
            return ErrorCategory.TESTING;
        }

        // 데이터베이스
        if (containsAny(
                value,
                "sql",
                "oracle",
                "mysql",
                "postgresql",
                "postgres",
                "database",
                "hibernate",
                "jdbc",
                "jpa",
                "entitymanager",
                "ora-",
                "데이터베이스"
        )) {
            return ErrorCategory.DATABASE;
        }

        // 프론트엔드
        if (containsAny(
                value,
                "react",
                "vue",
                "angular",
                "vite",
                "jsx",
                "tsx",
                "css",
                "browser",
                "frontend",
                "프론트엔드"
        )) {
            return ErrorCategory.FRONTEND;
        }

        // 모바일
        if (containsAny(
                value,
                "android",
                "ios",
                "flutter",
                "react native",
                "swift",
                "kotlin mobile"
        )) {
            return ErrorCategory.MOBILE;
        }

        // DevOps 및 배포
        if (containsAny(
                value,
                "docker",
                "container",
                "docker compose",
                "kubernetes",
                "nginx",
                "jenkins",
                "github actions",
                "pipeline",
                "ci/cd",
                "deployment",
                "deploy",
                "컨테이너",
                "배포"
        )) {
            return ErrorCategory.DEVOPS_CICD;
        }

        // 클라우드 및 인프라
        if (containsAny(
                value,
                "aws",
                "ec2",
                "s3",
                "route 53",
                "route53",
                "iam",
                "azure",
                "gcp",
                "cloud",
                "elastic ip",
                "클라우드"
        )) {
            return ErrorCategory.CLOUD_INFRA;
        }

        // 네트워크
        if (containsAny(
                value,
                "cors",
                "http",
                "https",
                "network",
                "connection refused",
                "connection timed out",
                "timeout",
                "port",
                "proxy",
                "dns",
                "socket",
                "네트워크",
                "연결 거부"
        )) {
            return ErrorCategory.NETWORK;
        }

        // 운영체제 및 환경
        if (containsAny(
                value,
                "windows",
                "linux",
                "ubuntu",
                "permission denied",
                "access denied",
                "environment variable",
                "path",
                "file not found",
                "환경변수",
                "권한 거부",
                "경로"
        )) {
            return ErrorCategory.OS_ENVIRONMENT;
        }

        // 백엔드
        if (containsAny(
                value,
                "spring",
                "spring boot",
                "bean",
                "controller",
                "service",
                "repository",
                "rest api",
                "backend",
                "백엔드"
        )) {
            return ErrorCategory.BACKEND;
        }

        // 프로그래밍 언어
        if (containsAny(
                value,
                "java",
                "python",
                "javascript",
                "typescript",
                "syntax error",
                "compile error",
                "nullpointerexception",
                "classcastexception",
                "illegalargumentexception"
        )) {
            return ErrorCategory.LANGUAGE;
        }

        return ErrorCategory.ETC;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private String makeTitle(String errorMessage) {
        String title = safe(errorMessage)
                .replaceAll("\\s+", " ")
                .trim();

        if (title.isBlank()) {
            return "개발 오류 해결 기록";
        }

        return limitTitle(title);
    }

    private String limitTitle(String title) {
        String normalized = safe(title)
                .replaceAll("\\s+", " ")
                .trim();

        return normalized.length() > 40
                ? normalized.substring(0, 40) + "…"
                : normalized;
    }
}