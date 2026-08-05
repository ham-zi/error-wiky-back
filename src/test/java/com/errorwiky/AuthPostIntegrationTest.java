package com.errorwiky;

import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthPostIntegrationTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 private HttpSession session;

 @BeforeEach void login() throws Exception{
  String suffix=String.valueOf(System.nanoTime());
  mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content("""
   {"loginId":"user%s","name":"테스터","email":"u%s@test.local","password":"password123"}
  """.formatted(suffix.substring(suffix.length()-8),suffix))).andExpect(status().isCreated());
  MvcResult result=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
   {"loginId":"user%s","password":"password123"}
  """.formatted(suffix.substring(suffix.length()-8)))).andExpect(status().isOk()).andReturn();
  session=result.getRequest().getSession(false); Assertions.assertNotNull(session);
 }

 @Test void completeFlow() throws Exception{
  String json="""
   {"boardType":"ERROR_WIKI","title":"빈 생성 테스트","content":"상세 내용","category":"SPRING",
    "errorMessage":"BeanCreationException","environment":"Java 21","cause":"빈 충돌","solution":"Qualifier 추가"}
  """;
  MockMultipartFile data=new MockMultipartFile("data","","application/json",json.getBytes());
  MockMultipartFile file=new MockMultipartFile("files","error.log","text/plain","error log content".getBytes());
  String body=mvc.perform(multipart("/api/posts").file(data).file(file).session((org.springframework.mock.web.MockHttpSession)session))
   .andExpect(status().isCreated()).andExpect(jsonPath("$.data.postId").isNumber()).andReturn().getResponse().getContentAsString();
  long id=mapper.readTree(body).path("data").path("postId").asLong();
  mvc.perform(get("/api/posts").param("boardType","ERROR_WIKI")).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(1));
  mvc.perform(get("/api/posts/{id}",id)).andExpect(status().isOk()).andExpect(jsonPath("$.data.viewCount").value(1));
  mvc.perform(post("/api/posts/{id}/like",id).session((org.springframework.mock.web.MockHttpSession)session)).andExpect(status().isOk()).andExpect(jsonPath("$.data.liked").value(true));
  mvc.perform(post("/api/posts/{id}/comments",id).session((org.springframework.mock.web.MockHttpSession)session).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"도움됐어요\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.content").value("도움됐어요"));
 }
}
