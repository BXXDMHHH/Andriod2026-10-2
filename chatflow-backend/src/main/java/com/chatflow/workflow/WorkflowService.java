package com.chatflow.workflow;
import com.chatflow.conversation.ConversationEntity;
import com.chatflow.conversation.ConversationService;
import com.chatflow.message.MessageService;
import com.chatflow.user.UserEntity;
import com.chatflow.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WorkflowService {
 private static final Set<String> NODE_TYPES=Set.of("START","SEND_MESSAGE","WAIT_INPUT","CONDITION","SET_VARIABLE","DELAY","END");
 private static final Pattern EXPRESSION=Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_.-]*)\\s*(==|!=|contains)\\s*'(.*?)'\\s*$");
 private static final Pattern PLACEHOLDER=Pattern.compile("\\$\\{([A-Za-z_][A-Za-z0-9_.-]*)}");
 private final WorkflowRepository workflows; private final WorkflowRunRepository runs; private final WorkflowNodeRunRepository nodeRuns;
 private final UserRepository users; private final ConversationService conversations; private final MessageService messages;
 private final ObjectMapper mapper; private final SimpMessagingTemplate broker;
 public WorkflowService(WorkflowRepository workflows,WorkflowRunRepository runs,WorkflowNodeRunRepository nodeRuns,UserRepository users,ConversationService conversations,MessageService messages,ObjectMapper mapper,SimpMessagingTemplate broker){
  this.workflows=workflows;this.runs=runs;this.nodeRuns=nodeRuns;this.users=users;this.conversations=conversations;this.messages=messages;this.mapper=mapper;this.broker=broker;
 }
 @Transactional public WorkflowDtos.WorkflowResponse create(String username,WorkflowDtos.CreateRequest request){
  validateDefinition(request.definition());
  UserEntity user=users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User no longer exists"));
  return toWorkflowResponse(workflows.save(new WorkflowEntity(request.name().trim(),request.description(),writeJson(request.definition()),user)));
 }
 @Transactional(readOnly=true) public List<WorkflowDtos.WorkflowResponse> list(String username){return workflows.findByCreatedBy_UsernameAndStatusOrderByUpdatedAtDesc(username,"ACTIVE").stream().map(this::toWorkflowResponse).toList();}
 @Transactional(readOnly=true) public WorkflowDtos.WorkflowResponse get(Long id,String username){return toWorkflowResponse(requireWorkflow(id,username));}
 @Transactional public WorkflowDtos.RunResponse start(String username,Long workflowId,WorkflowDtos.RunRequest request){
  WorkflowEntity workflow=requireWorkflow(workflowId,username);ConversationEntity conversation=conversations.requireOwned(request.conversationId(),username);
  UserEntity user=users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User no longer exists"));
  Map<String,Object> variables=request.variables()==null?new LinkedHashMap<>():new LinkedHashMap<>(request.variables());variables.putIfAbsent("conversationId",conversation.getId());
  WorkflowRunEntity run=runs.saveAndFlush(new WorkflowRunEntity(workflow,conversation,user,writeJson(variables)));
  publishRun(run,"workflow.started");JsonNode definition=readJson(workflow.getDefinitionJson());executeFrom(run,definition,definition.path("startNodeId").asText());return getRunResponse(run);
 }
 @Transactional(readOnly=true) public WorkflowDtos.RunResponse getRun(Long id,String username){return getRunResponse(requireRun(id,username));}
 @Transactional public WorkflowDtos.RunResponse submitInput(Long id,String username,WorkflowDtos.InputRequest request){
  WorkflowRunEntity run=requireRun(id,username);
  if(!"WAITING".equals(run.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Workflow run is not waiting for input");
  JsonNode definition=readJson(run.getWorkflow().getDefinitionJson());JsonNode waitingNode=findNode(definition,run.getCurrentNodeId());
  if(waitingNode==null||!"WAIT_INPUT".equals(nodeType(waitingNode)))throw new ResponseStatusException(HttpStatus.CONFLICT,"Current workflow node cannot accept input");
  Map<String,Object> variables=readVariables(run);variables.put(waitingNode.path("variable").asText("input"),request.input());
  run.setVariablesJson(writeJson(variables));run.setStatus("RUNNING");String waitingId=run.getCurrentNodeId();run.setCurrentNodeId(null);runs.save(run);publishRun(run,"workflow.resumed");
  executeFrom(run,definition,nextNode(definition,waitingId,variables,false));return getRunResponse(run);
 }
 @Transactional public WorkflowDtos.RunResponse cancel(Long id,String username){
  WorkflowRunEntity run=requireRun(id,username);if(!Set.of("RUNNING","WAITING").contains(run.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Only active runs can be cancelled");
  run.setStatus("CANCELED");run.setCurrentNodeId(null);run.setEndedAt(LocalDateTime.now());runs.save(run);publishRun(run,"workflow.canceled");return getRunResponse(run);
 }
 private void executeFrom(WorkflowRunEntity run,JsonNode definition,String firstNodeId){
  Map<String,Object> variables=readVariables(run);String currentId=firstNodeId;int steps=0;
  while(currentId!=null&&!currentId.isBlank()){
   if(++steps>100){failRun(run,"Workflow exceeded the 100-node safety limit");return;}
   JsonNode node=findNode(definition,currentId);if(node==null){failRun(run,"Node not found: "+currentId);return;}
   String type=nodeType(node);WorkflowNodeRunEntity nodeRun=nodeRuns.saveAndFlush(new WorkflowNodeRunEntity(run,currentId,type,writeJson(variables)));
   try{
    Map<String,Object> output=new LinkedHashMap<>();
    if("START".equals(type)){output.put("started",true);}
    else if("SEND_MESSAGE".equals(type)){
     String content=render(node.path("content").asText(""),variables);if(content.isBlank())throw new IllegalArgumentException("SEND_MESSAGE requires non-empty content");
     var sent=messages.sendWorkflowMessage(run.getConversation(),run.getUser().getId(),"ASSISTANT",content);output.put("messageId",sent.id());output.put("content",sent.content());
    }else if("SET_VARIABLE".equals(type)){
     String name=node.path("variable").asText("");if(name.isBlank())throw new IllegalArgumentException("SET_VARIABLE requires variable");
     variables.put(name,render(node.path("value").asText(""),variables));output.put(name,variables.get(name));
    }else if("WAIT_INPUT".equals(type)){
     String variable=node.path("variable").asText("input");run.setStatus("WAITING");run.setCurrentNodeId(currentId);run.setVariablesJson(writeJson(variables));
     output.put("waitingFor",variable);completeNode(nodeRun,"SUCCESS",output,null);runs.save(run);publishRun(run,"workflow.waiting");return;
    }else if("CONDITION".equals(type)){output.put("evaluated",true);}
    else if("DELAY".equals(type)){long delay=Math.max(0,Math.min(node.path("delayMs").asLong(0),5000));if(delay>0)Thread.sleep(delay);output.put("delayMs",delay);}
    else if("END".equals(type)){
     output.put("ended",true);completeNode(nodeRun,"SUCCESS",output,null);run.setVariablesJson(writeJson(variables));run.setStatus("SUCCESS");run.setCurrentNodeId(null);run.setEndedAt(LocalDateTime.now());runs.save(run);publishRun(run,"workflow.completed");return;
    }else throw new IllegalArgumentException("Unsupported node type: "+type);
    completeNode(nodeRun,"SUCCESS",output,null);run.setVariablesJson(writeJson(variables));
    currentId=nextNode(definition,currentId,variables,"CONDITION".equals(type));run.setCurrentNodeId(currentId);runs.save(run);publishRun(run,"workflow.node.completed");
    if(currentId==null)throw new IllegalArgumentException("No outgoing edge from node "+node.path("id").asText());
   }catch(Exception ex){
    String detail=ex.getMessage()==null?ex.getClass().getSimpleName():ex.getMessage();completeNode(nodeRun,"FAILED",Map.of(),detail);failRun(run,"Node "+currentId+" ("+type+") failed: "+detail);return;
   }
  }
  failRun(run,"Workflow ended without an END node");
 }
 private String nextNode(JsonNode definition,String from,Map<String,Object> variables,boolean evaluateConditions){
  JsonNode edges=definition.path("edges");String fallback=null;if(!edges.isArray())return null;
  for(JsonNode edge:edges){if(!from.equals(edge.path("from").asText()))continue;String to=edge.path("to").asText();String condition=edge.path("condition").asText("").trim();
   if(condition.isBlank()||"default".equalsIgnoreCase(condition)||"else".equalsIgnoreCase(condition)){if(fallback==null)fallback=to;continue;}
   if(!evaluateConditions||evaluateExpression(condition,variables))return to;
  }return fallback;
 }
 private boolean evaluateExpression(String expression,Map<String,Object> variables){
  Matcher matcher=EXPRESSION.matcher(expression);if(!matcher.matches())throw new IllegalArgumentException("Unsupported condition expression: "+expression);
  String left=Objects.toString(variables.get(matcher.group(1)),"");String right=matcher.group(3);
  return switch(matcher.group(2)){case "=="->left.equals(right);case "!="->!left.equals(right);case "contains"->left.contains(right);default->false;};
 }
 private String render(String text,Map<String,Object> variables){
  Matcher matcher=PLACEHOLDER.matcher(text);StringBuffer result=new StringBuffer();
  while(matcher.find())matcher.appendReplacement(result,Matcher.quoteReplacement(Objects.toString(variables.get(matcher.group(1)),"")));
  matcher.appendTail(result);return result.toString();
 }
 private void completeNode(WorkflowNodeRunEntity nodeRun,String status,Map<String,Object> output,String error){nodeRun.setStatus(status);nodeRun.setOutputJson(writeJson(output));nodeRun.setErrorMessage(error);nodeRun.setEndedAt(LocalDateTime.now());nodeRuns.save(nodeRun);}
 private void failRun(WorkflowRunEntity run,String error){run.setStatus("FAILED");run.setCurrentNodeId(null);run.setEndedAt(LocalDateTime.now());run.setErrorMessage(error);runs.save(run);publishRun(run,"workflow.failed");}
 private void publishRun(WorkflowRunEntity run,String type){
  Map<String,Object> event=new LinkedHashMap<>();event.put("type",type);event.put("runId",run.getId());event.put("workflowId",run.getWorkflow().getId());event.put("conversationId",run.getConversation().getId());
  event.put("status",run.getStatus());event.put("currentNodeId",run.getCurrentNodeId());event.put("variables",readVariables(run));event.put("error",run.getErrorMessage());
  broker.convertAndSend("/topic/conversations/"+run.getConversation().getId(),event);
 }
 private WorkflowEntity requireWorkflow(Long id,String username){return workflows.findByIdAndCreatedBy_Username(id,username).filter(w->"ACTIVE".equals(w.getStatus())).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Workflow not found"));}
 private WorkflowRunEntity requireRun(Long id,String username){return runs.findByIdAndUser_Username(id,username).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Workflow run not found"));}
 private WorkflowDtos.WorkflowResponse toWorkflowResponse(WorkflowEntity e){return new WorkflowDtos.WorkflowResponse(e.getId(),e.getName(),e.getDescription(),e.getVersion(),e.getStatus(),readJson(e.getDefinitionJson()),e.getCreatedAt(),e.getUpdatedAt());}
 private WorkflowDtos.RunResponse getRunResponse(WorkflowRunEntity run){
  List<WorkflowDtos.NodeRunResponse> nodes=nodeRuns.findByRun_IdOrderByIdAsc(run.getId()).stream().map(n->new WorkflowDtos.NodeRunResponse(n.getId(),n.getNodeId(),n.getNodeType(),n.getStatus(),nullableJson(n.getInputJson()),nullableJson(n.getOutputJson()),n.getStartedAt(),n.getEndedAt(),n.getErrorMessage())).toList();
  return new WorkflowDtos.RunResponse(run.getId(),run.getWorkflow().getId(),run.getConversation().getId(),run.getStatus(),run.getCurrentNodeId(),readJson(run.getVariablesJson()),run.getStartedAt(),run.getEndedAt(),run.getErrorMessage(),nodes);
 }
 private JsonNode nullableJson(String value){return value==null?null:readJson(value);}
 private String nodeType(JsonNode node){return node.path("type").asText("").toUpperCase(Locale.ROOT);}
 private JsonNode findNode(JsonNode definition,String id){if(!definition.path("nodes").isArray())return null;for(JsonNode node:definition.path("nodes"))if(id.equals(node.path("id").asText()))return node;return null;}
 private void validateDefinition(JsonNode definition){
  if(definition==null||!definition.isObject()||definition.path("startNodeId").asText().isBlank()||!definition.path("nodes").isArray()||definition.path("nodes").isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Definition requires startNodeId and a non-empty nodes array");
  Set<String> ids=new HashSet<>();for(JsonNode node:definition.path("nodes")){String id=node.path("id").asText("");String type=nodeType(node);if(id.isBlank()||!ids.add(id))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Node ids must be non-empty and unique");if(!NODE_TYPES.contains(type))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported node type: "+type);}
  if(!ids.contains(definition.path("startNodeId").asText()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"startNodeId does not match a node");
  JsonNode edges=definition.path("edges");if(!edges.isArray())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Definition requires an edges array");
  for(JsonNode edge:edges)if(!ids.contains(edge.path("from").asText())||!ids.contains(edge.path("to").asText()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Every edge must reference existing nodes");
 }
 private Map<String,Object> readVariables(WorkflowRunEntity run){
  JsonNode node=readJson(run.getVariablesJson());Map<String,Object> values=new LinkedHashMap<>();node.fields().forEachRemaining(e->values.put(e.getKey(),mapper.convertValue(e.getValue(),Object.class)));return values;
 }
 private String writeJson(Object value){try{return mapper.writeValueAsString(value);}catch(JsonProcessingException ex){throw new IllegalArgumentException("Unable to serialize workflow data",ex);}}
 private JsonNode readJson(String value){try{return mapper.readTree(value);}catch(JsonProcessingException ex){throw new IllegalStateException("Stored workflow JSON is invalid",ex);}}
}
