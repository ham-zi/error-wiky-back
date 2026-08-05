package com.errorwiky;
import com.errorwiky.ai.*; import com.errorwiky.post.ErrorCategory; import tools.jackson.databind.ObjectMapper; import org.junit.jupiter.api.*;
class AiFallbackTest{
 @Test void fallbackWorksWhenOllamaIsDown(){AiGateway down=p->{throw new IllegalStateException("down");};AiRecommendService s=new AiRecommendService(down,new ObjectMapper());AiRecommendResponse r=s.recommend(new AiRecommendRequest("Spring BeanCreationException", "빈 충돌", "Qualifier 추가"));Assertions.assertEquals("FALLBACK",r.source());Assertions.assertEquals(ErrorCategory.SPRING,r.category());Assertions.assertFalse(r.title().isBlank());}
}
