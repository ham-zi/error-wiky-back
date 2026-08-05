package com.errorwiky.ai;
import tools.jackson.databind.JsonNode; import java.util.Map; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Component; import org.springframework.web.client.RestClient;
@Component
public class OllamaAiGateway implements AiGateway{
 private final RestClient client; private final String model;
 public OllamaAiGateway(RestClient.Builder builder,@Value("${app.ollama.base-url}") String url,@Value("${app.ollama.model}") String model){this.client=builder.baseUrl(url).build();this.model=model;}
 @Override public String recommend(String prompt){
  JsonNode node=client.post().uri("/api/generate").body(Map.of("model",model,"prompt",prompt,"stream",false,"format","json"))
   .retrieve().body(JsonNode.class); if(node==null||node.path("response").asText().isBlank()) throw new IllegalStateException("Ollama 응답 없음"); return node.path("response").asText();
 }
}
