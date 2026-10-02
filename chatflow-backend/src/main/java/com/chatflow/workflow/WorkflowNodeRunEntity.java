package com.chatflow.workflow;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="workflow_node_runs",indexes=@Index(name="idx_workflow_node_runs_run",columnList="run_id, id"))
public class WorkflowNodeRunEntity {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="run_id",nullable=false) private WorkflowRunEntity run;
 @Column(name="node_id",nullable=false,length=100) private String nodeId;
 @Column(name="node_type",nullable=false,length=40) private String nodeType;
 @Column(nullable=false,length=20) private String status="RUNNING";
 @Lob @Column(name="input_json",columnDefinition="LONGTEXT") private String inputJson;
 @Lob @Column(name="output_json",columnDefinition="LONGTEXT") private String outputJson;
 @Column(name="started_at",nullable=false) private LocalDateTime startedAt;
 @Column(name="ended_at") private LocalDateTime endedAt;
 @Column(name="error_message",length=2000) private String errorMessage;
 protected WorkflowNodeRunEntity(){}
 public WorkflowNodeRunEntity(WorkflowRunEntity run,String nodeId,String nodeType,String inputJson){this.run=run;this.nodeId=nodeId;this.nodeType=nodeType;this.inputJson=inputJson;}
 @PrePersist void onCreate(){if(startedAt==null)startedAt=LocalDateTime.now();}
 public Long getId(){return id;} public String getNodeId(){return nodeId;} public String getNodeType(){return nodeType;} public String getStatus(){return status;} public String getInputJson(){return inputJson;} public String getOutputJson(){return outputJson;} public LocalDateTime getStartedAt(){return startedAt;} public LocalDateTime getEndedAt(){return endedAt;} public String getErrorMessage(){return errorMessage;}
 public void setStatus(String v){status=v;} public void setOutputJson(String v){outputJson=v;} public void setEndedAt(LocalDateTime v){endedAt=v;} public void setErrorMessage(String v){errorMessage=v;}
}
