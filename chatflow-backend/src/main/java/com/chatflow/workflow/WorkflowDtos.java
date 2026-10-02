package com.chatflow.workflow;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
public final class WorkflowDtos {
 private WorkflowDtos(){}
 public record CreateRequest(@NotBlank @Size(max=120) String name,@Size(max=500) String description,@NotNull JsonNode definition){}
 public record RunRequest(@NotNull Long conversationId,Map<String,Object> variables){}
 public record InputRequest(@NotNull Object input){}
 public record WorkflowResponse(Long id,String name,String description,Integer version,String status,JsonNode definition,LocalDateTime createdAt,LocalDateTime updatedAt){}
 public record NodeRunResponse(Long id,String nodeId,String nodeType,String status,JsonNode input,JsonNode output,LocalDateTime startedAt,LocalDateTime endedAt,String errorMessage){}
 public record RunResponse(Long id,Long workflowId,Long conversationId,String status,String currentNodeId,JsonNode variables,LocalDateTime startedAt,LocalDateTime endedAt,String errorMessage,List<NodeRunResponse> nodes){}
}
