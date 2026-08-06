package com.errorwiky;

import com.errorwiky.ai.*;
import com.errorwiky.post.ErrorCategory;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

class AiFallbackTest {

    @Test
    void fallbackWorksWhenOllamaIsDown() {
        AiGateway down = prompt -> {
            throw new IllegalStateException("down");
        };

        AiRecommendService service =
                new AiRecommendService(down, new ObjectMapper());

        AiRecommendResponse response = service.recommend(
                new AiRecommendRequest(
                        "Spring BeanCreationException",
                        "빈 충돌",
                        "Qualifier 추가"
                )
        );

        Assertions.assertEquals("FALLBACK", response.source());
        Assertions.assertEquals(
                ErrorCategory.BACKEND,
                response.category()
        );
        Assertions.assertFalse(response.title().isBlank());
    }
}