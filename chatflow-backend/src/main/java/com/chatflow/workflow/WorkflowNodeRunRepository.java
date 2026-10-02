package com.chatflow.workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface WorkflowNodeRunRepository extends JpaRepository<WorkflowNodeRunEntity,Long>{
 List<WorkflowNodeRunEntity> findByRun_IdOrderByIdAsc(Long runId);
}
