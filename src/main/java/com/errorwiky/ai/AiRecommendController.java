package com.errorwiky.ai;

import com.errorwiky.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiRecommendController {
	private final AiRecommendService service;

	public AiRecommendController(AiRecommendService s) {
		service = s;
	}

	@PostMapping("/recommend")
	ApiResponse<AiRecommendResponse> recommend(@Valid @RequestBody AiRecommendRequest r) {
		return ApiResponse.ok(service.recommend(r));
	}
}
