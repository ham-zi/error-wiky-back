package com.errorwiky.post;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotNull BoardType boardType,
        @NotBlank @Size(max=200) String title,
        @NotBlank @Size(max=20000) String content,
        ErrorCategory category,
        @Size(max=10000) String errorMessage,
        @Size(max=10000) String environment,
        @Size(max=10000) String cause,
        @Size(max=20000) String solution
) {
    @AssertTrue(message="에러위키는 카테고리, 오류 메시지, 원인, 해결 방법이 필요합니다.")
    public boolean isErrorWikiFieldsValid() {
        if (boardType != BoardType.ERROR_WIKI) return true;
        return category != null && hasText(errorMessage) && hasText(cause) && hasText(solution);
    }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
