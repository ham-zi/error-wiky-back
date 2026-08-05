package com.errorwiky.ai;
import com.errorwiky.post.ErrorCategory; import tools.jackson.databind.*; import java.util.Locale; import org.springframework.stereotype.Service;
@Service
public class AiRecommendService{
 private final AiGateway gateway; private final ObjectMapper mapper;
 public AiRecommendService(AiGateway g,ObjectMapper m){gateway=g;mapper=m;}
 public AiRecommendResponse recommend(AiRecommendRequest r){
  String prompt="""
   다음 개발 오류를 분석해 JSON만 반환하세요. 형식: {"title":"40자 이내 제목","category":"JAVA|SPRING|DATABASE|REACT|NETWORK|DEPLOYMENT|GIT|ETC"}
   오류: %s\n원인: %s\n해결: %s
   """.formatted(r.errorMessage(),safe(r.cause()),safe(r.solution()));
  try{JsonNode n=mapper.readTree(gateway.recommend(prompt));String title=n.path("title").asText();ErrorCategory cat=parse(n.path("category").asText());if(title.isBlank())throw new IllegalArgumentException();return new AiRecommendResponse(title,cat,"OLLAMA",null);}catch(Exception e){
   ErrorCategory cat=guess(r.errorMessage()+" "+safe(r.cause())+" "+safe(r.solution())); String title=makeTitle(r.errorMessage());
   return new AiRecommendResponse(title,cat,"FALLBACK","Ollama에 연결하지 못해 규칙 기반 추천을 사용했습니다.");}
 }
 private String safe(String v){return v==null?"":v;} private ErrorCategory parse(String v){try{return ErrorCategory.valueOf(v.toUpperCase(Locale.ROOT));}catch(Exception e){return ErrorCategory.ETC;}}
 private ErrorCategory guess(String text){String t=text.toLowerCase(Locale.ROOT);if(t.contains("spring")||t.contains("bean")||t.contains("security"))return ErrorCategory.SPRING;if(t.contains("java")||t.contains("exception")||t.contains("nullpointer"))return ErrorCategory.JAVA;if(t.contains("sql")||t.contains("oracle")||t.contains("database"))return ErrorCategory.DATABASE;if(t.contains("react")||t.contains("vite")||t.contains("jsx"))return ErrorCategory.REACT;if(t.contains("git")||t.contains("commit")||t.contains("push"))return ErrorCategory.GIT;if(t.contains("docker")||t.contains("aws")||t.contains("deploy"))return ErrorCategory.DEPLOYMENT;if(t.contains("cors")||t.contains("http")||t.contains("network"))return ErrorCategory.NETWORK;return ErrorCategory.ETC;}
 private String makeTitle(String error){String one=error.replaceAll("\\s+"," ").trim();return one.length()>45?one.substring(0,45)+"…":one;}
}
