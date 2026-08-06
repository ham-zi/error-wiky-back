package com.errorwiky.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiRecommendRequest(@NotBlank @Size(max = 10000) String errorMessage, @Size(max = 10000) String cause,
		@Size(max = 20000) String solution) {
}
