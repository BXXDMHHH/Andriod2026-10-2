package com.chatflow.workflow;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1")
public class WorkflowController {
 private final WorkflowService service;public WorkflowController(WorkflowService service){this.service=service;}
 @GetMapping("/workflows") public List<WorkflowDtos.WorkflowResponse> list(@AuthenticationPrincipal String username){return service.list(username);}
 @PostMapping("/workflows") public WorkflowDtos.WorkflowResponse create(@AuthenticationPrincipal String username,@Valid @RequestBody WorkflowDtos.CreateRequest request){return service.create(username,request);}
 @GetMapping("/workflows/{id}") public WorkflowDtos.WorkflowResponse get(@AuthenticationPrincipal String username,@PathVariable Long id){return service.get(id,username);}
 @PostMapping("/workflows/{id}/run") public WorkflowDtos.RunResponse run(@AuthenticationPrincipal String username,@PathVariable Long id,@Valid @RequestBody WorkflowDtos.RunRequest request){return service.start(username,id,request);}
 @GetMapping("/workflow-runs/{id}") public WorkflowDtos.RunResponse getRun(@AuthenticationPrincipal String username,@PathVariable Long id){return service.getRun(id,username);}
 @PostMapping("/workflow-runs/{id}/input") public WorkflowDtos.RunResponse input(@AuthenticationPrincipal String username,@PathVariable Long id,@Valid @RequestBody WorkflowDtos.InputRequest request){return service.submitInput(id,username,request);}
 @PostMapping("/workflow-runs/{id}/cancel") public WorkflowDtos.RunResponse cancel(@AuthenticationPrincipal String username,@PathVariable Long id){return service.cancel(id,username);}
}
