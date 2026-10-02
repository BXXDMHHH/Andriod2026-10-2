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
class Week3WorkflowIntegrationTests {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 @Test void workflowWaitsForInputBranchesAndPersistsRunHistory() throws Exception {
  String token=register("week3_"+System.nanoTime());long conversationId=createConversation(token);
  String definition="""
   {"startNodeId":"start","nodes":[
    {"id":"start","type":"START"},{"id":"welcome","type":"SEND_MESSAGE","content":"欢迎！请输入你的需求。"},
    {"id":"wait","type":"WAIT_INPUT","variable":"userInput"},{"id":"branch","type":"CONDITION"},
    {"id":"human","type":"SEND_MESSAGE","content":"正在转人工"},{"id":"auto","type":"SEND_MESSAGE","content":"已收到：${userInput}"},
    {"id":"endHuman","type":"END"},{"id":"endAuto","type":"END"}],
    "edges":[{"from":"start","to":"welcome"},{"from":"welcome","to":"wait"},{"from":"wait","to":"branch"},
    {"from":"branch","to":"human","condition":"userInput contains '人工'"},{"from":"branch","to":"auto","condition":"default"},
    {"from":"human","to":"endHuman"},{"from":"auto","to":"endAuto"}]}
   """;
  var payload=mapper.createObjectNode();payload.put("name","Human handoff");payload.put("description","Week 3 integration test");payload.set("definition",mapper.readTree(definition));
  MvcResult created=mvc.perform(post("/api/v1/workflows").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(payload))).andExpect(status().isOk()).andReturn();
  long workflowId=mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
  MvcResult started=mvc.perform(post("/api/v1/workflows/"+workflowId+"/run").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"conversationId\":"+conversationId+"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("WAITING")).andReturn();
  JsonNode startedJson=mapper.readTree(started.getResponse().getContentAsString());long runId=startedJson.get("id").asLong();
  assertThat(startedJson.get("nodes").size()).isEqualTo(3);assertThat(startedJson.get("currentNodeId").asText()).isEqualTo("wait");
  mvc.perform(post("/api/v1/workflow-runs/"+runId+"/input").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"input\":\"我需要人工客服\"}"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS")).andExpect(jsonPath("$.variables.userInput").value("我需要人工客服")).andExpect(jsonPath("$.nodes.length()").value(6));
  mvc.perform(get("/api/v1/conversations/"+conversationId+"/messages").header("Authorization","Bearer "+token)).andExpect(status().isOk())
   .andExpect(jsonPath("$[0].senderType").value("ASSISTANT")).andExpect(jsonPath("$[0].content").value("欢迎！请输入你的需求。")).andExpect(jsonPath("$[1].content").value("正在转人工"));
  mvc.perform(get("/api/v1/workflow-runs/"+runId).header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS")).andExpect(jsonPath("$.nodes[0].status").value("SUCCESS"));
 }
 @Test void workflowCannotBeReadByAnotherUser() throws Exception {
  String owner=register("wfowner_"+System.nanoTime());String other=register("wfother_"+System.nanoTime());
  String body="""
   {"name":"Private workflow","definition":{"startNodeId":"start","nodes":[{"id":"start","type":"START"},{"id":"end","type":"END"}],"edges":[{"from":"start","to":"end"}]}}
   """;
  MvcResult created=mvc.perform(post("/api/v1/workflows").header("Authorization","Bearer "+owner).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andReturn();
  long id=mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
  mvc.perform(get("/api/v1/workflows/"+id).header("Authorization","Bearer "+other)).andExpect(status().isNotFound());
 }
 private String register(String username)throws Exception {
  MvcResult r=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"username\":\""+username+"\",\"password\":\"strong-pass-123\"}")).andExpect(status().isOk()).andReturn();
  return mapper.readTree(r.getResponse().getContentAsString()).get("accessToken").asText();
 }
 private long createConversation(String token)throws Exception {
  MvcResult r=mvc.perform(post("/api/v1/conversations").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Workflow integration\"}")).andExpect(status().isOk()).andReturn();
  return mapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();
 }
}
