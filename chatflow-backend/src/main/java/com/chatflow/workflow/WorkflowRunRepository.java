package com.chatflow.workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface WorkflowRunRepository extends JpaRepository<WorkflowRunEntity,Long>{
 Optional<WorkflowRunEntity> findByIdAndUser_Username(Long id,String username);
}
