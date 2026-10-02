package com.chatflow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class Week2ApiIntegrationTests {
 @Autowired MockMvc mvc;@Autowired ObjectMapper mapper;
 @Test void registerLoginCreateConversationSendAndReadMessage()throws Exception{
  String username="tester_"+System.nanoTime();
  MvcResult reg=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+username+"\",\"password\":\"strong-pass-123\",\"nickname\":\"Tester\"}")).andExpect(status().isOk()).andReturn();
  String token=mapper.readTree(reg.getResponse().getContentAsString()).get("accessToken").asText();assertThat(token).isNotBlank();
  mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+username+"\",\"password\":\"strong-pass-123\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value(username));
  MvcResult created=mvc.perform(post("/api/v1/conversations").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Integration test\"}")).andExpect(status().isOk()).andReturn();
  long id=mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
  mvc.perform(post("/api/v1/conversations/"+id+"/messages").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"Hello ChatFlow\",\"clientMsgId\":\"test-1\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.content").value("Hello ChatFlow"));
  mvc.perform(get("/api/v1/conversations/"+id+"/messages").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$[0].content").value("Hello ChatFlow"));
  mvc.perform(get("/api/v1/conversations")).andExpect(status().isUnauthorized());
 }
 @Test void userCannotReadAnotherUsersConversation()throws Exception{
  String a="owner_"+System.nanoTime(),b="other_"+System.nanoTime();String ta=register(a),tb=register(b);
  MvcResult created=mvc.perform(post("/api/v1/conversations").header("Authorization","Bearer "+ta).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Private\"}")).andExpect(status().isOk()).andReturn();
  long id=mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
  mvc.perform(get("/api/v1/conversations/"+id).header("Authorization","Bearer "+tb)).andExpect(status().isNotFound());
 }
 @Test void loginRejectsWrongPassword()throws Exception{
  String u="login_"+System.nanoTime();register(u);
  mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+u+"\",\"password\":\"wrong-password\"}")).andExpect(status().isUnauthorized());
 }
 private String register(String u)throws Exception{
  MvcResult r=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+u+"\",\"password\":\"strong-pass-123\"}")).andExpect(status().isOk()).andReturn();
  return mapper.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
 }
}